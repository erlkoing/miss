package pl.edu.agh.miss;

import jadex.commons.future.Future;


public interface ICustomAgentService {
	
	// serwis wywoluje akcje przeslana jako parametr
	public Future<Void> doSomething(final IAction action);
	
	// serwis zwraca stan agenta
	public double getState();
}
