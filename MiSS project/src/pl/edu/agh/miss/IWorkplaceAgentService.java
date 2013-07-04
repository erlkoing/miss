package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;

import java.util.List;

public interface IWorkplaceAgentService {

	public IComponentIdentifier getSomeSibling(IComponentIdentifier cid);

	public IComponentIdentifier getSomeSiblingsWithout(List<IComponentIdentifier> myKnownSiblings, IComponentIdentifier cid);
	
	public List<IComponentIdentifier> getAllSiblings(IComponentIdentifier cid);
	
	public List<IComponentIdentifier> getAllSiblingsWithout(List<IComponentIdentifier> myKnownSiblings, IComponentIdentifier cid);
	
	public int getSiblingsCount();
	
	public void signUpForMerge(IComponentIdentifier cid);

	public void signUpForCompare(IComponentIdentifier agent1, IComponentIdentifier agent2);

	public List<IComponentIdentifier> getAgentsToCommunicate();
}