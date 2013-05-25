package pl.edu.agh.miss;

import jadex.commons.future.Future;


public interface ICustomAgentService {
	
	public Future<Void> doSSomething(final IAction action);
	
	public double getState();
}
