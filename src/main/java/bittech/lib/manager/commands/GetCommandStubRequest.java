package bittech.lib.manager.commands;

import bittech.lib.protocol.Request;
import bittech.lib.utils.Require;

public class GetCommandStubRequest implements Request {
	
	public final String commandClassName;
	
	public GetCommandStubRequest(final String commandClassName) {
		this.commandClassName = Require.notNull(commandClassName, "commandClassName");
	}

}
