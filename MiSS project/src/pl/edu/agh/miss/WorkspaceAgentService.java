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
	WorkspaceAgent agent;

	@Override
	public void deleteMe(IComponentIdentifier me) {
		agent.deleteChildAgent(me);
	}

	@Override
	public IComponentIdentifier getSomeSibling(IComponentIdentifier me) {
		IComponentIdentifier result;
		while ((result = agent.getSomeChildAgent()).getLocalName().equals(me.getLocalName())) {

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
	public void signUpForCompare(IComponentIdentifier agent1, IComponentIdentifier agent2) {
		LOGGER.debug("zapisuje agentow " + agent1.getName() + " i " + agent2.getName());
		agent.addToCompareList(agent1, agent2);
	}

	@Override
	public List<IComponentIdentifier> getAgentsToCommunicate() {
		return agent.getAgentsToCommunicate();
	}

}
