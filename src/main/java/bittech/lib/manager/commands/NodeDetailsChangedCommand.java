package bittech.lib.manager.commands;

import bittech.lib.protocol.Command;
import bittech.lib.protocol.common.NoDataRequest;
import bittech.lib.protocol.common.NoDataResponse;

public class NodeDetailsChangedCommand extends Command<NoDataRequest, NoDataResponse> {

	public static NodeDetailsChangedCommand createStub () {
		return new NodeDetailsChangedCommand();
	}

	public NodeDetailsChangedCommand() {
		request = new NoDataRequest();
	}

}
