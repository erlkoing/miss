package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import org.apache.log4j.Logger;

@Service
public class CustomAgentService implements ICustomAgentService {

	private Logger LOGGER = Logger.getLogger(getClass());

	@ServiceComponent
	CustomAgent customAgent;

	@ServiceStart
	public void serviceStart() {

	}

	@Override
	public Future<Void> step(final IAction action) {
		customAgent.scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {				
				action.performAction(customAgent);
				return IFuture.DONE;
			}
		}).get();

		return (Future<Void>) IFuture.DONE;

	}

	@Override
	public Future<Double> getState() {
		Double result = customAgent.scheduleStep(new IComponentStep<Double>() {

			@Override
			public IFuture<Double> execute(IInternalAccess ia) {
				return new Future<Double>(customAgent.getState());
			}
		}).get();

		return new Future<Double>(result);
	}

	@Override
	public Future<Double> compareStates(final IComponentIdentifier cid) {

		Double result = customAgent.scheduleStep(new IComponentStep<Double>() {

			@Override
			public IFuture<Double> execute(IInternalAccess ia) {

				final double agentState1 = SServiceProvider
						.getService(customAgent.getServiceProvider(), cid, ICustomAgentService.class).get().getState().get();
				final double agentState2 = customAgent.getState();
				LOGGER.debug(customAgent.getAgentName() + " my state: " + agentState2 + ", " + cid.getName() + " state: "
						+ agentState1 + "\n");

				customAgent.setState(agentState2 + agentState2 - agentState1);
				Future<Double> ret = new Future<Double>(agentState1 - agentState2);
				return ret;
			}
		}).get();

		return new Future<Double>(result);
	}

	@Override
	public Future<Void> modifyStateBy(final Double chunk) {
		return new Future<Void>(customAgent.scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				customAgent.modifyStateBy(chunk);
				return IFuture.DONE;
			}
		}).get());
	}

	@Override
	public Future<Void> removeAgentFromKnownSiblings(final IComponentIdentifier agentToRemove) {
		return new Future<Void>(customAgent.scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				customAgent.knownSiblings.remove(agentToRemove);
				return IFuture.DONE;
			}
		}).get());
	}

}
