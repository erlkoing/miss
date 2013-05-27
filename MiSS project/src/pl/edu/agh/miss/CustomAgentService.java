package pl.edu.agh.miss;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.bridge.service.search.SServiceProvider;
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
					
					Random r = new Random((new Date()).getTime() * 3571 * agent.id);
					Integer rNumber = r.nextInt(100);
					
					if (rNumber > 90) {
						agent.workplaceservice.signUpForMerge(agent.getComponentIdentifier());
					} else if (rNumber > 60) {
						IComponentIdentifier cid = agent.workplaceservice.getSomeSibling(agent.getComponentIdentifier());
						agent.knownSiblings.add(cid);
						System.out.println(agent.getAgentName() + " - I got " + cid.getLocalName());
					} else {
						if (agent.knownSiblings.size() > 0) {
							rNumber = r.nextInt(agent.knownSiblings.size());
							
							
							Iterator<IComponentIdentifier> itr = agent.knownSiblings.iterator();
							
							for (int i = 0; i < rNumber; i++, itr.next())
								;
							
							agent.workplaceservice.signUpForCompare(agent.getComponentIdentifier(), itr.next());
						} else {
							IComponentIdentifier cid = agent.workplaceservice.getSomeSibling(agent.getComponentIdentifier());
							agent.knownSiblings.add(cid);
							System.out.println(agent.getAgentName() + " - I got " + cid.getLocalName());
						}
					}
					
					
									
					
				} else {
					System.out.println(agent.getAgentName() + " jest zablokowany, czekam na polaczenie z innym agentem");
				}
						
				return IFuture.DONE;
			}
		}).get();
		
		

		//agent.signMeForMergeList();

	return (Future<Void>) IFuture.DONE;
		
	}



	@Override
	public Future<Double> getState() {
		Double result = agent.scheduleStep(new IComponentStep<Double>() {

			@Override
			public IFuture<Double> execute(IInternalAccess ia) {
				return new Future<Double>(agent.getState());
			}
		}).get();
		
		return new Future<Double>(result);
	}



	@Override
	public Future<Double> compareStates(final IComponentIdentifier cid) {
	
		
		Double result = agent.scheduleStep(new IComponentStep<Double>() {

			@Override
			public IFuture<Double> execute(IInternalAccess ia) {
				
				final double agentState1 = SServiceProvider.getService(agent.getServiceProvider(), cid, ICustomAgentService.class).get().getState().get();
				final double agentState2 = agent.getState();
				System.out.println(agent.getAgentName() + " my state: " + agentState2 + ", " + cid.getName() + " state: " + agentState1);
				
				agent.setState(agentState2 + agentState2 -agentState1 );
				Future<Double> ret = new Future<Double>(agentState1 - agentState2);
				return ret;
			}
		}).get();
		
		return new Future<Double>(result);
	}



	@Override
	public Future<Void> modifyStateBy(final Double chunk) {
		return new Future<Void>(agent.scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				agent.modifyStateBy(chunk);
				return IFuture.DONE;
			}
		}).get());
	}



	@Override
	public Future<Void> removeAgentFromKnownSiblings(
			final IComponentIdentifier agentToRemove) {
		return new Future<Void>(agent.scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				agent.knownSiblings.remove(agentToRemove);
				return IFuture.DONE;
			}
		}).get());
	}



}
