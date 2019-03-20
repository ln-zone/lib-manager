package bittech.lib.manager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bittech.lib.manager.commands.AreYouAliveCommand;
import bittech.lib.manager.commands.GetCommandStubCommand;
import bittech.lib.manager.commands.GetCommandStubResponse;
import bittech.lib.manager.commands.GetNodeDetailsCommand;
import bittech.lib.manager.commands.GetNodeDetailsResponse;
import bittech.lib.manager.commands.NodeDetailsChangedCommand;
import bittech.lib.protocol.Command;
import bittech.lib.protocol.Listener;
import bittech.lib.protocol.Node;
import bittech.lib.protocol.common.NoDataResponse;
import bittech.lib.utils.Require;
import bittech.lib.utils.Utils;
import bittech.lib.utils.exceptions.ExceptionManager;
import bittech.lib.utils.exceptions.StoredException;
import bittech.lib.utils.json.RawJson;
import bittech.lib.utils.logs.Logs;

public class NodeDetailsListener extends Observable implements Listener {

	private final static Logger LOGGER = LoggerFactory.getLogger(NodeDetailsListener.class);

	private final Node node;
	private final String peerName = "dae-manager";
	protected final String myName;
	private GetNodeDetailsResponse oldResp = null;
	private ChangeWatcherThread thread;

	private Set<ManagerDataProvider> detailsProviders = new LinkedHashSet<ManagerDataProvider>();
	
	private Object mutex = new Object();

	public NodeDetailsListener(final Node node, final String myName) {
		this.node = Require.notNull(node, "node");
		this.myName = Require.notNull(myName, "myName");
	
		thread = new ChangeWatcherThread(this);
	}
	
	public synchronized void addDetailsProvider(ManagerDataProvider detailsProvider) {
		detailsProviders.add(Require.notNull(detailsProvider, "detailsProvider"));
	}

	public void connectToManager() {
		node.connectAsync(peerName, "80.211.242.177", ManagerDefaults.portNumber);
	}

	public void connectToManager(String ip) {
		node.connectAsync(peerName, Require.notNull(ip, "ip"), ManagerDefaults.portNumber);
	}
	
	public void connectToManager(String ip, int port) {
		node.connectAsync(peerName, Require.notNull(ip, "ip"), port);
	}	
	
	@Override
	public Class<?>[] getListeningCommands() {
		return new Class<?>[] { GetNodeDetailsCommand.class, AreYouAliveCommand.class, GetCommandStubCommand.class };
	}

	@Override
	public String[] getListeningServices() {
		return null;
	}

	@Override
	public void commandReceived(String fromServiceName, Command<?, ?> command) {
		try {
			if ("dae-manager".equals(peerName)) {
				thread.lastManagerActivityTime.set((new Date()).getTime());

				if (command instanceof AreYouAliveCommand) {
					AreYouAliveCommand cmd = (AreYouAliveCommand) command;
					cmd.response = new NoDataResponse();
				} else if (command instanceof GetNodeDetailsCommand) {
					LOGGER.info("GetNodeDetailsCommand received");
					GetNodeDetailsCommand cmd = (GetNodeDetailsCommand) command;
					synchronized (mutex) {
						if (oldResp == null) {
							oldResp = collectNodeDetails();
							LOGGER.info("Starting new ChangeWatcherThread");
							this.thread.start();
						}

						if (oldResp.summary == null) {
							throw new StoredException("Node summary cannot be null", null);
						}

						cmd.response = oldResp;
					}
				} else if (command instanceof GetCommandStubCommand) {
					GetCommandStubCommand cmd = (GetCommandStubCommand)command;
					Command<?, ?> cmdStub = getCommandStub(cmd.getRequest().commandClassName);
					cmd.response = new GetCommandStubResponse();
					cmd.response.stub = new RawJson(cmdStub);
				} else {
					throw new RuntimeException("Unsupported comamnd: " + command.getClass().getName());
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Cannot executre command: " + command, ex);
		}
	}
	
	private Command<?, ?> getCommandStub(String commandName) throws StoredException {
		try {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(commandName);
			} catch (ClassNotFoundException ex) {
				throw new Exception("Manager do cannot recognize such command: " + commandName, ex);
			}
			Method method = null;
			try {
				method = clazz.getMethod("createStub");
			} catch (NoSuchMethodException ex) {
				throw new Exception("Command " + commandName + " do not implements static createStub method", ex);
			}
			Object o = null;
			try {
				o = method.invoke(null);
			} catch (Exception ex) {
				throw new Exception("Calling static " + commandName + ".createStub method failed", ex);
			}
			if (!(o instanceof Command)) {
				throw new Exception("This class do not extends Command: " + commandName);
			}
			return (Command<?, ?>) o;
		} catch (Exception ex) {
			throw new StoredException("Cannot get command stub for: " + commandName, ex);
		}
	}

	private synchronized GetNodeDetailsResponse collectNodeDetails() throws StoredException {
		try {
			LOGGER.debug("Collecting node details");
			GetNodeDetailsResponse response = new GetNodeDetailsResponse();

			response.name = myName;

			response.summary.put("name", myName);
			response.summary.put("status", "ok");
			response.summary.put("statusCode", "ok");
			
			for(ManagerDataProvider detailsProvider : detailsProviders) {
				detailsProvider.addCustomData(response);
			}
			
			addListeningCommands(response);
			
			response.summary.put("logs", "" + Logs.getInstance().count());
			response.logs = new RawJson(Logs.getInstance().getAsJson());
			
			addExceptions(response);

			return response;
		} catch (Throwable th) {
			throw new StoredException("Cannot collect node details", th);
		}
	}

	private void addListeningCommands(GetNodeDetailsResponse response) {
		Set<Class<?>> classes = node.getListenedCommands();
		for (Class<?> c : classes) {
			response.supportedCommands.add(c.getCanonicalName());
		}
		Collections.sort(response.supportedCommands);
	}

	private void addExceptions(GetNodeDetailsResponse response) {
		Collection<Long> ids = ExceptionManager.getInstance().getExceptionIds();
		for (Long id : ids) {
			response.exceptions.add(ExceptionManager.getInstance().get(id));
		}
		response.summary.put("exceptions", "" + ids.size());
	}

	public static class ChangeWatcherThread extends Thread {

		private final NodeDetailsListener parent;

		public AtomicLong lastManagerActivityTime = new AtomicLong();

		public ChangeWatcherThread(NodeDetailsListener parent) {
			this.parent = Require.notNull(parent, "parent");
		}

		@Override
		public void run() {
			while (true) {

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Better handling?
					e1.printStackTrace();
					return;
				}

				try {
					long now = (new Date()).getTime();
					long delta = now - lastManagerActivityTime.get();
					if (delta < 10000) {
						if (parent.node.isPeerConnected(parent.peerName)) {
							GetNodeDetailsResponse newResp = parent.collectNodeDetails();

							if (Utils.deepEquals(parent.oldResp, newResp) == false) {
								synchronized (parent.mutex) {
									parent.oldResp = newResp;
									LOGGER.info("----------------------------------------- NOTYFING OBSERVERS ------------------------");
									parent.notifyAllObservers();
								}
								NodeDetailsChangedCommand cmd = new NodeDetailsChangedCommand();
								if (parent.node.isPeerConnected(parent.peerName)) {
									parent.node.execute(parent.peerName, cmd);
								}

							}
						} else {
							LOGGER.warn("Not connected to manager with name " + parent.peerName);
						}
					} else {
						LOGGER.warn("No need to grab node data. Delta is: " + delta);
					}
				} catch (Exception e) {
					new StoredException("Getting node details error", e);
					e.printStackTrace();
				}

			}
		}

	}
	
	private synchronized void notifyAllObservers() {
		LOGGER.info("CHANGE. NOTYFING ALL OBSERVERS: " + this.countObservers());
		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public void responseSent(String serviceName, Command<?, ?> command) {
		// Nothing here
	}

}
