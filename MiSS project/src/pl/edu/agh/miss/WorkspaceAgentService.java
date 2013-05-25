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
		Random r = new Random((new Date()).getTime());
		
		Iterator<IComponentIdentifier> itr = agent.childrenAgents.iterator();
		for (int i = 0; i < r.nextInt(agent.childrenAgents.size()); i++) {
			itr.next();
		}

		return itr.next();
	}

	@Override
	public void signUpForMerge(IComponentIdentifier me) {
		agent.addToMergeList(me);
	}

	@Override
	public void killMe(IComponentIdentifier me) {
		agent.deleteChildAgent(me);
	}
	
	

}
