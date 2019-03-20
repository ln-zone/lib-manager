package bittech.lib.manager.commands;

import bittech.lib.protocol.Command;

public class GetCommandStubCommand extends Command<GetCommandStubRequest, GetCommandStubResponse> {
	
	public static GetCommandStubCommand createStub() {
		return new GetCommandStubCommand("");
	}
	
	public GetCommandStubCommand(final String commandClassName) {
		this.request = new GetCommandStubRequest(commandClassName);
	}
	
}
