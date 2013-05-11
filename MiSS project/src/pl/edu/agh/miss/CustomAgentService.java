package pl.edu.agh.miss;

import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

@Service
public class CustomAgentService implements ICustomAgentService {

	
	@ServiceComponent
	CustomAgent agent;
	
	@Override
	public IFuture<Double> getState() {
		agent.runWorkplaceService();
		
		System.out.println("!!!!!!!!!!!!!!!! " );
		return new Future<Double>(agent.state);
	}
	

}
