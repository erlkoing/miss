package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;

import java.util.List;

import org.apache.log4j.Logger;

@Service
public class WorkspaceAgentService implements IWorkplaceAgentService {

	private Logger LOGGER = Logger.getLogger(getClass());

	@ServiceComponent
	WorkspaceAgent workspace;

	@Override
	public IComponentIdentifier getSomeSibling(IComponentIdentifier cid) {
		if (workspace.getChildrenCount() == 1)
			return null;
		
		IComponentIdentifier someSibling;
		
		while ((someSibling = workspace.getSomeChildAgent()).getLocalName().equals(cid.getLocalName())) 
			;

		return someSibling;
	}
	
	@Override
	public IComponentIdentifier getSomeSiblingsWithout(List<IComponentIdentifier> myKnownSiblings, IComponentIdentifier cid) {
		myKnownSiblings.add(cid);
		if (myKnownSiblings.size() == workspace.getChildrenCount())
			return null;
		
		IComponentIdentifier someSibling;
		
		do {
			someSibling = workspace.getSomeChildAgent();
		} while(myKnownSiblings.contains(someSibling));
		
		return someSibling;
	}
	
	@Override
	public List<IComponentIdentifier> getAllSiblings(IComponentIdentifier cid) {
		if (workspace.getChildrenCount() == 1)
			return null;
		
		List<IComponentIdentifier> allSiblingsList = workspace.getChildAgentsList();
		allSiblingsList.remove(cid);
		
		return allSiblingsList;
	}
	
	@Override
	public List<IComponentIdentifier> getAllSiblingsWithout(List<IComponentIdentifier> myKnownSiblings, IComponentIdentifier cid) {
		myKnownSiblings.add(cid);
		if (myKnownSiblings.size() == workspace.getChildrenCount())
			return null;
		
		List<IComponentIdentifier> allSiblingsList = workspace.getChildAgentsList();
		
		allSiblingsList.removeAll(myKnownSiblings);
		return allSiblingsList;
	}

	@Override
	public int getSiblingsCount() {
		return workspace.getChildrenCount() - 1;
	}

	@Override
	public void signUpForMerge(IComponentIdentifier cid) {
		workspace.addToMergeList(cid);
	}

	@Override
	public void signUpForCompare(IComponentIdentifier agent1, IComponentIdentifier agent2) {
		LOGGER.debug("zapisuje agentow " + agent1.getName() + " i " + agent2.getName() + "\n");
		workspace.addToCompareList(agent1, agent2);
	}

	@Override
	public List<IComponentIdentifier> getAgentsToCommunicate() {
		return workspace.getAgentsToCommunicate();
	}
}
