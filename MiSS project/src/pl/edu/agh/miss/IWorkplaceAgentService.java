package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;

import java.util.List;

public interface IWorkplaceAgentService {
	public void deleteMe(IComponentIdentifier me);

	public IComponentIdentifier getSomeSibling(IComponentIdentifier me);

	public void signUpForMerge(IComponentIdentifier me);

	public void killMe(IComponentIdentifier me);

	public void signUpForCompare(IComponentIdentifier agent1,
			IComponentIdentifier agent2);

	public List<IComponentIdentifier> getAgentsToCommunicate();
}