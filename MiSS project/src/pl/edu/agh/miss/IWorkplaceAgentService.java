package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;


public interface IWorkplaceAgentService {
	public void deleteMe(IComponentIdentifier me);
	
	public IComponentIdentifier getSomeSibling(IComponentIdentifier me);
	
	public void signUpForMerge(IComponentIdentifier me);

	public void killMe(IComponentIdentifier me);
}

