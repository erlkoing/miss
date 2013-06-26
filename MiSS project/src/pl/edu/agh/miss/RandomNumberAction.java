package pl.edu.agh.miss;

import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

public class RandomNumberAction implements IAction {
	private Logger LOGGER = Logger.getLogger(getClass());

	@Override
	public void doAction(CustomAgent agent) {
		Double state = agent.getState();
		Random r = new Random((new Date()).getTime());
		int rNumber = r.nextInt(101);
		if (rNumber > 80) {
			state += rNumber / 10.0;
		}

		if (rNumber < 20) {
			state -= (100 - (double) rNumber) / 10.0;
		}

		LOGGER.debug(agent.getAgentName() + "Random number " + rNumber);

		agent.setState(state);
	}
}