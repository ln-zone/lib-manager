package bittech.lib.manager;

import java.util.Observer;

import bittech.lib.protocol.Node;
import bittech.lib.utils.Require;

public class ManagerModule {
	
	private final NodeDetailsListener nodeDetailsListener;
	
	public ManagerModule(final Node node, final String myName) {
		Require.notNull(node, "node");
		nodeDetailsListener = new NodeDetailsListener(node, myName);
		node.registerListener(nodeDetailsListener);
	}
	
	public void start() {
		nodeDetailsListener.connectToManager();
	}
	
	public void start(String ip) {
		nodeDetailsListener.connectToManager(ip);
	}
	
	public void addDetailsProvider(ManagerDataProvider detailsProvider) {
		nodeDetailsListener.addDetailsProvider(detailsProvider);
	}
	
	public void addObserver(Observer observer) {
		nodeDetailsListener.addObserver(observer);
	}

}
