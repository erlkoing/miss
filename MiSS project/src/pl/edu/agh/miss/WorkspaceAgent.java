package pl.edu.agh.miss;


import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentKilled;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RequiredServices({
	@RequiredService(name="cms", type=IComponentManagementService.class, binding=@Binding(scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)),
	@RequiredService(name="customservices", type=ICustomAgentService.class, multiple=true, binding=@Binding(dynamic=true, scope=Binding.SCOPE_COMPONENT))
})
@ProvidedServices(@ProvidedService(type=IWorkplaceAgentService.class, implementation=@Implementation(WorkspaceAgentService.class)))
@Arguments({
	@Argument(name="maxChildrenAgents", description= "Parametr okresla maksymalna liczbe workerow w workspace", clazz=Integer.class, defaultvalue="100"),
	@Argument(name="initialChildrenAgents", description= "Parametr okresla pocatkowa liczbe workerow", clazz=Integer.class, defaultvalue="100"),
	@Argument(name="maxSteps", description= "Parametr okresla liczbe krokow - 0 = neiskonczona", clazz=Integer.class, defaultvalue="2"),
	@Argument(name="verbose", description= "", clazz=Boolean.class, defaultvalue="true"),
	@Argument(name="action", clazz=IAction.class)
})
@Description("WorkspaceAgent")
@Agent
public class WorkspaceAgent extends MicroAgent {
	
	/*****************************************
	 *************** ARGUMENTS ***************
	 *****************************************/
	@AgentArgument
	Boolean verbose;	// czy maja byc wyswietlane komunikaty
	
	@AgentArgument
	Integer maxChildrenAgents;	// maksymalna liczba agentow
	
	@AgentArgument
	Integer initialChildrenAgents;	// poczatkowa liczba agentow
	
	@AgentArgument
	Integer maxSteps;	// (maksymalna) liczba krokow
	
	@AgentArgument
	IAction action;		// akcja/e
	
	// pozostale - potrzebne do funkcjownowania klasy
	Integer actualChildrenCount = 0;	// aktualna liczba agentow - modyfikiwana przy dodawaniu i usuwaniu agentow
	Integer lastPeerIndex = 0;			// ostatni uzyty indeks - modyfikowane przy dodawaniu nowego agenta - nigdy sie nie zmniejsza 
	Integer actualStep = 0;				// aktualny krok
	
	HashSet<IComponentIdentifier> childrenAgents = new HashSet<IComponentIdentifier>();	// zbior agentow 
	
	ArrayList<IComponentIdentifier> mergeList = new ArrayList<IComponentIdentifier>();	// lista 
	
	
	
	@AgentService
	protected IComponentManagementService cms;	
	
	@AgentService
	protected IClockService clockservice;
		
	
	// Public methods
	@AgentCreated
	public IFuture<Void> agentCreated() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				indroduceYourself();
			    createInitialPopulation();
				return IFuture.DONE;
			}
		}).get();
		
		
		return IFuture.DONE;
	}

	
	
	// Private methods
	// prosta funkcja ktora sie przedstawia
	private void indroduceYourself() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					System.out.println("WORKPLACEAGENT - Hello I'm " + getAgentName());
				}
				
				return IFuture.DONE;
			}
		}).get();
	}
	
	// funkcja tworzy poczatkowa populacje agentow
	private void createInitialPopulation() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					//System.out.println("WORKPLACEAGENT - Creating initial population");
					System.out.println("WORKPLACEAGENT - Tworze poczatkowa populacje agentow");
				}
				for (int i= 0; i < initialChildrenAgents; ++i) {
					createChildAgent(10.0);
				}
				
				return IFuture.DONE;
			}
		}).get();
	}
	
	// Funkcja tworzy agenta
	private void createChildAgent(final double initialState) {
		scheduleStep(new IComponentStep<Void>() {
			
			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				// Wypisanie informacji na konsole o akcji
				if (verbose) {
					//System.out.println("WORKPLACEAGENT - Creating Agent: " + getAgentName() + "#CHILD" + lastPeerIndex);	
					System.out.println(getAgentName() + " - Tworze agenta: " + getAgentName() + "#CHILD" + lastPeerIndex);
				}
				
				// Stworzenie informacji przekazywanej nowemu agentowi tj. kim jest jego parent, ustawienie parametrow		
				CreationInfo crInfo = new CreationInfo(getComponentIdentifier());
				Map<String, Object> infoArgs = new HashMap<String, Object>();
				infoArgs.put("verbose", verbose);
				infoArgs.put("state", new Double(initialState));
				crInfo.setArguments(infoArgs);
				
				// Właściwe stworzenie agenta
				childrenAgents.add(cms.createComponent(getAgentName() + "#CHILD" + lastPeerIndex, CustomAgent.class.getName()+".class", crInfo, null).get());
				
				// uaktualniamy wlasny zbior child agentow
				updateChildrenSet();
				
				// Aktualizacja odpowiednich zmiennych
				lastPeerIndex++;
				actualChildrenCount++;
			
				// Powrót
				return IFuture.DONE;
			}
		}).get();
	}
	
	// funkcja wypisujaca child agentow
	private void printChildrenAgents() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {

				if (verbose) {
					//System.out.println("WORKPLACES CHILDREN AGENTS");
					System.out.println(getAgentName() + " - moi agenci " + childrenAgents.size());	
				}
				
				if (childrenAgents != null) {
					for (IComponentIdentifier ci : childrenAgents) {
						System.out.println(ci.getName());
					}
				}
				return IFuture.DONE;
			}
		}).get();
	}
	
	// uaktualnia zbior agentow
	private void updateChildrenSet() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				childrenAgents.clear();	// czyscimy zbior
				childrenAgents = getChildrenAgents();	// przypisujemy nowa liste
				return IFuture.DONE;
			}
		}).get();
	}
	
	// wyciaga child agentow z funkji jadexowych
	private HashSet<IComponentIdentifier> getChildrenAgents() {
		final HashSet<IComponentIdentifier> result = new HashSet<IComponentIdentifier>();
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				
				for (IServiceProvider sp : getServiceContainer().getChildren().get()) {
					result.add(sp.getId());
				}		
				return IFuture.DONE;
			}
		}).get();

		return result;
	}
	
	// glowna petla agenta - jej zadaniem jest iteracja po kolejnych krokach 
	@AgentBody
	public IFuture<Void> executeBody() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				System.out.println("\n\n\n");
				for (actualStep = 0; actualStep < maxSteps; actualStep++) {
					if (verbose) {
						System.out.println("\n\n" + getAgentName() + " - performing step " + actualStep);
					}
					
					performStepOnAllAgents();
				}
				return IFuture.DONE;
			}
		}).get();
		return IFuture.DONE;
	}
	
	// funkcja iterujaca po child agentach 
	private void performStepOnAllAgents() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				for(final IComponentIdentifier cid:  childrenAgents) {
					performStepOnSingleAgent(cid);
				}
				return IFuture.DONE;
			}
		}).get();
	}
	
	// funkcja wywolujaca odpowiedni serwis na wskazanym child agencie
	private void performStepOnSingleAgent(final IComponentIdentifier cid) {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				System.out.println("\n" + cid.getLocalName());
				SServiceProvider.getService(getServiceProvider(), cid, ICustomAgentService.class).get().doSomething(action).get();
				return IFuture.DONE;
			}
		}).get();
	}
											
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void printAgentsOnMergeList() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					System.out.println(getAgentName() + " - merge list " + mergeList.size());	
				}
				
				for (IComponentIdentifier a : mergeList) {
					System.out.println(a.getName());
				}
				
				return IFuture.DONE;
			}
		}).get();
	}
		

		

		
	public void manageMergeList() {
		System.out.println(getAgentName() + " - Zaczynam przegladac liste agentow chetnych do polaczenia ");
		while (mergeList.size() >= 2) {
			mergeAgents(mergeList.get(0), mergeList.get(1));
			mergeList.remove(0);
			mergeList.remove(0);
		}
	}

	
	@AgentKilled
	public IFuture<Void> agentKilled() {
		System.out.println(getAgentName() + " killed.");
		return IFuture.DONE;
	}
	
//	private void doSomethingOnAllChildren() {
//		for (final IComponentIdentifier child : childrenAgents) {
//			
//			if (verbose) {
//				//action.doAction();
//				System.out.println(getAgentName() + " - performing step on " + child.getLocalName());
//			}
//			
//			
//			IFuture<ICustomAgentService> f = SServiceProvider.getService(getServiceProvider(), child, ICustomAgentService.class);
//			f.addResultListener(new DefaultResultListener<ICustomAgentService>() {
//				@Override
//				public void resultAvailable(ICustomAgentService arg0) {
//					IFuture<Void> fut = arg0.doSomething(action);
//					
//					fut.addResultListener(new DefaultResultListener<Void>() {
//						@Override
//						public void resultAvailable(Void arg0) {
//							
//						}
//					});
//					
//					
//					IFuture<Double> f = arg0.getState();
//					
//					System.out.println(getAgentName() + " -" + child.getLocalName() + " state: " + f.get());
//					
//					arg0.getState().addResultListener(new DefaultResultListener<Double>() {
//
//						@Override
//						public void resultAvailable(Double arg0) {
//							System.out.println(getAgentName() + " -" + child.getLocalName() + " state: " + arg0);
//							
//						}
//					});
//				}
//			});		
				



	
	public boolean checkIfChildExists(IComponentIdentifier me) {
		return childrenAgents.contains(me);
	}

	public void deleteChildAgent(IComponentIdentifier me) {
		System.out.println("Deleting agent " + me);
		if (checkIfChildExists(me)) {
			cms.destroyComponent(me).get();
			actualChildrenCount--;
			childrenAgents = getChildrenAgents();
		}  else {
			System.out.println("There is no child agent " + me);
		}
	}
	
	
	
	public void mergeAgents(IComponentIdentifier agent1, IComponentIdentifier agent2) {
		double stateAgent1 = SServiceProvider.getService(getServiceProvider(), agent1, ICustomAgentService.class).get().getState();
		double stateAgent2 = SServiceProvider.getService(getServiceProvider(), agent2, ICustomAgentService.class).get().getState();
		createChildAgent(stateAgent1 + stateAgent2);
		deleteChildAgent(agent1);
		deleteChildAgent(agent2);
	}
	
	public void addToMergeList(final IComponentIdentifier cid) {
		if (!mergeList.contains(cid))
			mergeList.add(cid);	
	}
}