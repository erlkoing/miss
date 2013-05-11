package pl.edu.agh.miss;

import jadex.commons.future.IFuture;


public interface IWorkplaceAgentService {
	public IFuture<Integer> getWorkplaceStatus();
	
}
