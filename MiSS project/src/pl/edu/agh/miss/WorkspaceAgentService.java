package pl.edu.agh.miss;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;

@Service
public class WorkspaceAgentService implements IWorkplaceAgentService {

	@ServiceComponent
	WorkspaceAgent agent;

	@Override
	public void deleteMe(IComponentIdentifier me) {
		agent.deleteChildAgent(me);
	}

	@Override
	public IComponentIdentifier getSomeSibling(IComponentIdentifier me) {
		IComponentIdentifier result;
		while ( (result = agent.getSomeChildAgent()).getLocalName().equals(me.getLocalName())) {
			
		}

		return result;
	}

	@Override
	public void signUpForMerge(IComponentIdentifier me) {
		agent.addToMergeList(me);
	}

	@Override
	public void killMe(IComponentIdentifier me) {
		agent.deleteChildAgent(me);
	}

	@Override
	public void signUpForCompare(IComponentIdentifier agent1,
			IComponentIdentifier agent2) {
		System.out.println("zapisuje agentow " + agent1.getName() + " i " + agent2.getName());
		agent.addToCompareList(agent1, agent2);
	}
	
	

}
