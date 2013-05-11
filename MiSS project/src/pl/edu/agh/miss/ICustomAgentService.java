package pl.edu.agh.miss;

import jadex.commons.future.IFuture;

public interface ICustomAgentService {
	public IFuture<Double> getState();
}
