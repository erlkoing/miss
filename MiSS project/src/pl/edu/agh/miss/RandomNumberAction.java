package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;

public class RandomNumberAction implements IAction {
	private Logger LOGGER = Logger.getLogger(getClass());

	private void changeState(CustomAgent customAgent) {
		Double state = customAgent.getState();
		Random r = new Random((new Date()).getTime());
		int rNumber = r.nextInt(101);
		if (rNumber > 80) {
			state += rNumber / 10.0;
		}

		if (rNumber < 20) {
			state -= (100 - (double) rNumber) / 10.0;
		}

		LOGGER.debug(customAgent.getAgentName() + "Random number " + rNumber + "\n");
		customAgent.setState(state);
	}
	
	private void randomBehaviour(CustomAgent customAgent) {
		Random r = new Random((new Date()).getTime() * 3571 * customAgent.id);
		Integer rNumber = r.nextInt(100);

		if (rNumber > 90) {
			customAgent.workplaceservice.signUpForMerge(customAgent.getComponentIdentifier());
		} else if (rNumber > 60) {
			IComponentIdentifier cid = customAgent.workplaceservice.getSomeSibling(customAgent.getComponentIdentifier());
			if (cid != null)
				customAgent.knownSiblings.add(cid);
			
			LOGGER.debug(customAgent.getAgentName() + " - I got " + cid.getLocalName() + "\n");
		} else if (rNumber > 40) { 
			if (customAgent.knownSiblings.size() > 0) {
				rNumber = r.nextInt(customAgent.knownSiblings.size());

				Iterator<IComponentIdentifier> itr = customAgent.knownSiblings.iterator();

				for (int i = 0; i < rNumber; i++, itr.next()) {
					;
				}

				customAgent.workplaceservice.signUpForCompare(customAgent.getComponentIdentifier(), itr.next());
			} else {
				IComponentIdentifier cid = customAgent.workplaceservice.getSomeSibling(customAgent.getComponentIdentifier());
				if (cid != null)
					customAgent.knownSiblings.add(cid);
				
				LOGGER.debug(customAgent.getAgentName() + " - I got " + cid.getLocalName() + "\n");
			}
		}
	
		customAgent.sendMessageToOthers();
	}
	
	@Override
	public void performAction(CustomAgent customAgent) {
		if (!customAgent.blocked) {
			changeState(customAgent);
			randomBehaviour(customAgent);
		} else {
			LOGGER.debug(customAgent.getAgentName() + " jest zablokowany, czekam na polaczenie z innym agentem\n");
		}
	}
}