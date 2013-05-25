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
	
	@AgentArgument
	Boolean verbose;
	
	@AgentArgument
	Double state;
	
	@AgentArgument
	Boolean blocked;
	
	public Double getState() {
		return state;
	}
	
	public void setState(Double state) {
		this.state = state;
	}
	
	@AgentService
	protected IClockService clockservice;
	
	@AgentService
	protected IWorkplaceAgentService workplaceservice;
	
	@AgentCreated
	public IFuture<Void> agentCreated() {
//		System.out.println(getAgentName() + " with initial state " + state);
		System.out.println(getAgentName() + " z poczatkowym stanem " + state);
		return IFuture.DONE;
	}
	
	@AgentBody
	public IFuture<Void> executeBody() {
		return new Future<Void>();
	}
	
	public void signMeForMergeList() {
		System.out.println(getAgentName() + "Zapisalem sie na liste agentow chcacych sie polaczyc");
		workplaceservice.signUpForMerge(getComponentIdentifier());
		blocked = true;
	}
	
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
}
