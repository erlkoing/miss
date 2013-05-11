package pl.edu.agh.miss;

import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

@Service
public class WorkspaceAgentService implements IWorkplaceAgentService {

	@ServiceComponent
	WorkspaceAgent agent;
	
	@Override
	public IFuture<Integer> getWorkplaceStatus() {
		return new Future<Integer>(43);
	}

}
