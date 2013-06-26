package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.fipa.SFipa;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentMessageArrived;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

@RequiredServices({
		@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)),
		@RequiredService(name = "workplaceservice", type = IWorkplaceAgentService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)) })
@ProvidedServices(@ProvidedService(type = ICustomAgentService.class, implementation = @Implementation(CustomAgentService.class)))
@Arguments({
		@Argument(name = "state", description = "Argument okresla poczatkowy stan agenta", clazz = Double.class, defaultvalue = "10.0"),
		@Argument(name = "verbose", description = "", clazz = Boolean.class, defaultvalue = "true"),
		@Argument(name = "id", description = "", clazz = Integer.class),
		@Argument(name = "blocked", description = "", clazz = Boolean.class, defaultvalue = "false") })
public class CustomAgent extends MicroAgent {

	private Logger LOGGER = Logger.getLogger(getClass());

	// argumenty ktorym zostana wstrzykniete odpowiednie wartosci
	@AgentArgument
	Boolean verbose;

	@AgentArgument
	Double state;

	@AgentArgument
	Boolean blocked;

	@AgentArgument
	Integer id;

	/** serwis workplace-u zeby nie trzeba bylo go za kazdym razem szukac itd. */
	@AgentService
	protected IWorkplaceAgentService workplaceservice;

	/**
	 * zbior zawierajacy znane "rodzenstwo" uzyskane dzieki odpytaniu
	 * workplace-a
	 */
	HashSet<IComponentIdentifier> knownSiblings = new HashSet<IComponentIdentifier>();

	/**
	 * funkcja wywolywana zaraz po stworzeniu agenta - glowne zadanie to sie
	 * przedstawic + moze ustawic jakies zmienne
	 */
	@Override
	@AgentCreated
	public IFuture<Void> agentCreated() {
		return new Future<Void>(scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					indroduceYourself();
				}

				return IFuture.DONE;
			}
		}).get());
	}

	@Override
	@AgentBody
	public IFuture<Void> executeBody() {
		return new Future<Void>();
	}

	/**
	 * zapisanie agenta na liste w celu polaczenia z innym agentem + blokada
	 * agenta
	 */
	public void signMeForMergeList() {
		LOGGER.debug(getAgentName() + "Zapisalem sie na liste agentow chcacych sie polaczyc");
		workplaceservice.signUpForMerge(getComponentIdentifier());
		blocked = true;
	}

	// jakas taka prosta funkcja
	public void indroduceYourself() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					LOGGER.debug(getAgentName() + " - Hello I'm " + getAgentName());
				}

				return IFuture.DONE;
			}
		}).get();
	}

	/** pobranie stanu */
	public Double getState() {
		return state;
	}

	/** ustawienie stanu */
	public void setState(Double state) {
		this.state = state;
	}

	public void modifyStateBy(Double chunk) {
		setState(getState() + chunk);
	}

	@Override
	@AgentMessageArrived
	public void messageArrived(Map<String, Object> message, final MessageType messageType) {
		LOGGER.debug(getAgentName() + " - Message arrived, content: ");
		LOGGER.debug(message.get(SFipa.CONTENT));
	}
}
