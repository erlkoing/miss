package pl.edu.agh.miss;

import java.util.Date;
import java.util.Random;

public class RandomNumberAction implements IAction {

	@Override
	public void doAction(CustomAgent agent) {
		Double state = agent.getState();
		Random r = new Random((new Date()).getTime());
		int rNumber = r.nextInt(101);
		if (rNumber > 80)
			state += (double)rNumber/10.0;
		
		if (rNumber < 20) 
			state -= (100 - (double)rNumber)/10.0;
		
		System.out.println(agent.getAgentName() + "Random number " + rNumber );
		
		agent.setState(state);
	}
}