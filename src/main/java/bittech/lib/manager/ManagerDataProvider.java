package bittech.lib.manager;

import bittech.lib.manager.commands.GetNodeDetailsResponse;

public interface ManagerDataProvider {

	public void addCustomData(GetNodeDetailsResponse details);
}
