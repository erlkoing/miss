package pl.edu.agh.miss;


import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentService;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RequiredServices({
	@RequiredService(name="cms", type=IComponentManagementService.class, binding=@Binding(scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)),
	@RequiredService(name="customservices", type=ICustomAgentService.class, multiple=true, binding=@Binding(dynamic=true, scope=Binding.SCOPE_COMPONENT))
})
@ProvidedServices(@ProvidedService(type=IWorkplaceAgentService.class, implementation=@Implementation(WorkspaceAgentService.class)))
@Arguments({
	@Argument(name="max_child_workers", description= "Parametr okresla maksymalna liczbe workerow w workspace", clazz=Integer.class, defaultvalue="100"),
	@Argument(name="initial_workers_count", description= "Parametr okresla pocatkowa liczbe workerow", clazz=Integer.class, defaultvalue="100"),
	@Argument(name="steps_count", description= "Parametr okresla liczbe krokow - 0 = neiskonczona", clazz=Integer.class, defaultvalue="2"),
	@Argument(name="verbose", description= "", clazz=Boolean.class, defaultvalue="true")
})
@Description("WorkspaceAgent")
public class WorkspaceAgent extends MicroAgent {
	// Skladniki klasy inicjalizowane w metodzie setUpArguments wartosciami przeslanymi jako argumenty 
	Boolean verbose;
	Integer maxChildrenAgents;
	Integer initialChildrenAgents;
	Integer maxSteps;
	
	// pozostale skladniki klasy
	Integer currentStep = 0;
	Integer actualChildrenCount = 0;
	Integer lastPeerIndex = 0;
	
	ArrayList<IComponentIdentifier> childrenAgents = new ArrayList<IComponentIdentifier>();
	
	@AgentService
	protected IComponentManagementService cms;	
	
	@AgentService
	protected IClockService clockservice;
		
	private void setUpArguments() {
		maxChildrenAgents = (Integer)getArgument("max_child_workers");
		initialChildrenAgents = (Integer)getArgument("initial_workers_count");
		maxSteps = (Integer)getArgument("steps_count");
		verbose = (Boolean)getArgument("verbose");
		
		// TODO sprawdzic poprawnosc przeslanych argumentow (zakres, wartosci itp.) + jesli mozliwe korekta
		
		if (initialChildrenAgents > maxChildrenAgents)
			initialChildrenAgents = maxChildrenAgents;
	}
	
	private void createInitialPopulation() {
		if (verbose) {
			System.out.println("WORKPLACEAGENT - Creating initial population");
		}
		for (actualChildrenCount = 0; actualChildrenCount < initialChildrenAgents; ++actualChildrenCount) {
			createChildAgent();
		}
	}
	
	private void createChildAgent() {
		if (verbose) {
			System.out.println("WORKPLACEAGENT - Creating Agent: " + getAgentName() + "#CHILD" + lastPeerIndex);	
		}
		CreationInfo crInfo = new CreationInfo(getComponentIdentifier());
		Map<String, Object> infoArgs = new HashMap<String, Object>();
		infoArgs.put("verbose", verbose);
		crInfo.setArguments(infoArgs);
		childrenAgents.add(cms.createComponent(getAgentName() + "#CHILD" + lastPeerIndex++, CustomAgent.class.getName()+".class", crInfo, null).get());
	}
	
	private void printChildrenAgents() {
		System.out.println("WORKPLACES CHILDREN AGENTS");
		if (childrenAgents != null) {
			for (IComponentIdentifier ci : childrenAgents) {
				System.out.println(ci.getName());
			}
		}
	}

	@Override
	public IFuture<Void> agentCreated() {
		setUpArguments();
		indroduceYourself();
		createInitialPopulation();
		
		return IFuture.DONE;
	}
	
	private void indroduceYourself() {
		if (verbose) {
			System.out.println("WORKPLACEAGENT - Hello I'm " + getAgentName());
		}
	}

	@Override
	public IFuture<Void> executeBody() {
		printChildrenAgents();
		/*waitFor(1000, new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				System.out.println("WORKSPACEAGENT - STEP");
				return null;
			}
		});*/
		//System.out.println("Jestem WorkspaceAgent " + getAgentName());
		//System.out.println("WorkspaceAgent: czas pobrany z clockservice =  " + new Date(clockservice.getTime()));
//		System.out.println("Tworze CustomAgenta");
//		
//		final IComponentIdentifier ci = cms.createComponent(getAgentName() + "#PEER_1", CustomAgent.class.getName()+".class", crInfo, null).get();
//		System.out.println("TYPE " + getServiceContainer().getType());
//			
//		IFuture<Collection<ICustomAgentService>>	chatservices	= getServiceContainer().getRequiredServices("customservices");
//		chatservices.addResultListener(new DefaultResultListener<Collection<ICustomAgentService>>()
//		{
//			public void resultAvailable(Collection<ICustomAgentService> result)
//			{
//				for(Iterator<ICustomAgentService> it=result.iterator(); it.hasNext(); )
//				{
//					ICustomAgentService cs = it.next();
//					IFuture<Double> fut = cs.getState();
//					fut.addResultListener(new DefaultResultListener<Double>() {
//
//						@Override
//						public void resultAvailable(Double arg0) {
//							
//							System.out.println("STATEEEE = " + arg0);
//						}
//					});
//				}
//			}
//		});		
			
		return IFuture.DONE;
	}
}