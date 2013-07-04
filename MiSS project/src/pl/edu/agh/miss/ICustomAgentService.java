package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.future.Future;

public interface ICustomAgentService {

	/** serwis wywoluje akcje przeslana jako parametr */
	public Future<Void> step(final IAction action);

	/** serwis zwraca stan agenta */
	public Future<Double> getState();

	public Future<Double> compareStates(IComponentIdentifier cid);

	public Future<Void> modifyStateBy(Double chunk);

	public Future<Void> removeAgentFromKnownSiblings(IComponentIdentifier agentToRemove);
}
