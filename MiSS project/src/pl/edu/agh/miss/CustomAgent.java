package pl.edu.agh.miss;

import java.util.ArrayList;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.AgentBody;
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
	@Argument(name="initial_state", description= "Argument okresla poczatkowy stan agenta", clazz=Double.class, defaultvalue="10.0"),
	@Argument(name="verbose", description= "", clazz=Boolean.class, defaultvalue="true")
})
//@Agent
public class CustomAgent extends MicroAgent {
	Boolean verbose;
	Double state;
	
	@AgentService
	protected IClockService clockservice;
	
	private void setUpArguments() {
		state = (Double)getArgument("initial_state");
		verbose = (Boolean)getArgument("verbose");
	}

	@Override
	public IFuture<Void> agentCreated() {
		setUpArguments();
		indroduceYourself();
		
		return IFuture.DONE;
	}
	
	private void indroduceYourself() {
		if (verbose) {
			System.out.println("CUSTOMAGENT - Hello I'm " + getAgentName());
		}
	}

	@AgentBody
	public IFuture<Void> executeBody() {
		//System.out.println("Jestem CustomAgent " + getAgentName());
		//System.out.println("CustomAgent: czas pobrany z clockservice =  " + new Date(clockservice.getTime()));
		//System.out.println("CustomAgent: moje id = " + getServiceContainer().getId().getLocalName());
//		IFuture<IServiceProvider> fut = getServiceContainer().getParent();
//		fut.addResultListener(new DefaultResultListener<IServiceProvider>() {
//
//			@Override
//			public void resultAvailable(IServiceProvider arg0) {
//				System.out.println("CustomAgent: moim parentem jest = " + arg0.getId().getName());						
//			}
//		});
//		Future<Void> ret = new Future<>();
		
		return IFuture.DONE;
	}

	public void runWorkplaceService() {
		IFuture<IWorkplaceAgentService> f = SServiceProvider.getService(getServiceProvider(), getComponentIdentifier().getParent(), IWorkplaceAgentService.class);
		f.addResultListener(new DefaultResultListener<IWorkplaceAgentService>() {
			public void resultAvailable(IWorkplaceAgentService agentService) {
				
				IFuture<Integer> fut = agentService.getWorkplaceStatus();
				
				
				fut.addResultListener(new DefaultResultListener<Integer>() {

					@Override
					public void resultAvailable(Integer arg0) {
						
						System.out.println("Workplace state = " + arg0);						
					}
				});
				
				
			}


		});
		
	}
}
