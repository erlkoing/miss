package pl.edu.agh.miss;

import java.util.concurrent.ScheduledExecutorService;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

@Service
public class CustomAgentService implements ICustomAgentService {

	
	@ServiceComponent
	CustomAgent agent;
	
	@ServiceStart
	public void serviceStart() {
		
	}
	

	
	@Override
	public Future<Void> doSomething(final IAction action) {
		agent.scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (!agent.blocked) {
					action.doAction(agent);	
				} else {
					System.out.println(agent.getAgentName() + " jest zablokowany, czekam na polaczenie z innym agentem");
				}
				
				agent.indroduceYourself();
				
				return IFuture.DONE;
			}
		}).get();
		


		//agent.signMeForMergeList();

	return (Future<Void>) IFuture.DONE;
		
	}



	@Override
	public double getState() {
		return agent.getState();
	}
}
