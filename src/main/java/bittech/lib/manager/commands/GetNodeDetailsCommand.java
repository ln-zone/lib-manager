package bittech.lib.manager.commands;

import bittech.lib.protocol.Command;
import bittech.lib.protocol.common.NoDataRequest;

public class GetNodeDetailsCommand extends Command<NoDataRequest, GetNodeDetailsResponse> {

	public static GetNodeDetailsCommand createStub () {
		return new GetNodeDetailsCommand();
	}
	
	public GetNodeDetailsCommand() {
		this.request = new NoDataRequest();
	}

}
