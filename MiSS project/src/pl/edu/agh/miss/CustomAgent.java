package pl.edu.agh.miss;

import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

@RequiredServices({
	@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)),
	@RequiredService(name = "workplaceservice", type = IWorkplaceAgentService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM))
})
@ProvidedServices(@ProvidedService(type=ICustomAgentService.class, implementation=@Implementation(CustomAgentService.class)))
@Arguments({
	@Argument(name="state", description= "Argument okresla poczatkowy stan agenta", clazz=Double.class, defaultvalue="10.0"),
	@Argument(name="verbose", description= "", clazz=Boolean.class, defaultvalue="true"),
	@Argument(name="blocked", description= "", clazz=Boolean.class, defaultvalue="false")
})

public class CustomAgent extends MicroAgent {
	
	// argumenty ktorym zostana wstrzykniete odpowiednie wartosci 
	@AgentArgument
	Boolean verbose;
	
	@AgentArgument
	Double state;
	
	@AgentArgument
	Boolean blocked;
	
	// przykladowy serwis - mozna usunac w przyszlosci
	@AgentService
	protected IClockService clockservice;
	
	// serwis workplace-u zeby nie trzeba bylo go za kazdym razem szukac itd.
	@AgentService
	protected IWorkplaceAgentService workplaceservice;
	
	// funkcja wywolywana zaraz po stworzeniu agenta
	@AgentCreated
	public IFuture<Void> agentCreated() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					indroduceYourself();
				}
				
				return IFuture.DONE;
			}
		}).get();
		return IFuture.DONE;
	}
	
	@AgentBody
	public IFuture<Void> executeBody() {
		return new Future<Void>();
	}
	
	// zapisanie agenta na liste w celu polaczenia z innym agentem + blokada agenta
	public void signMeForMergeList() {
		System.out.println(getAgentName() + "Zapisalem sie na liste agentow chcacych sie polaczyc");
		workplaceservice.signUpForMerge(getComponentIdentifier());
		blocked = true;
	}
	
	// jakas taka prosta funkcja 
	public void indroduceYourself() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					System.out.println(getAgentName() + " - Hello I'm " + getAgentName());
				}
				
				return IFuture.DONE;
			}
		}).get();
	}
	
	// pobranie stanu
	public Double getState() {
		return state;
	}
	
	// ustawienie stanu
	public void setState(Double state) {
		this.state = state;
	}
}
