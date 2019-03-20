package bittech.lib.manager.commands;

import bittech.lib.protocol.Command;
import bittech.lib.protocol.common.NoDataRequest;
import bittech.lib.protocol.common.NoDataResponse;

public class AreYouAliveCommand extends Command<NoDataRequest, NoDataResponse> {
	
	public static AreYouAliveCommand createStub() {
		return new AreYouAliveCommand();
	}
	
	public AreYouAliveCommand() {
		this.request = new NoDataRequest();
	}
	
}
