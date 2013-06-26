package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.fipa.SFipa;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.SUtil;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentKilled;
import jadex.micro.annotation.AgentMessageArrived;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

@RequiredServices({
		@RequiredService(name = "cms", type = IComponentManagementService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)),
		@RequiredService(name = "clockservice", type = IClockService.class, binding = @Binding(scope = Binding.SCOPE_PLATFORM)),
		@RequiredService(name = "customservices", type = ICustomAgentService.class, multiple = true, binding = @Binding(dynamic = true, scope = Binding.SCOPE_COMPONENT)) })
@ProvidedServices(@ProvidedService(type = IWorkplaceAgentService.class, implementation = @Implementation(WorkspaceAgentService.class)))
@Arguments({
		@Argument(name = "maxChildrenAgents", description = "Parametr okresla maksymalna liczbe workerow w workspace", clazz = Integer.class, defaultvalue = "100"),
		@Argument(name = "initialChildrenAgents", description = "Parametr okresla pocatkowa liczbe workerow", clazz = Integer.class, defaultvalue = "100"),
		@Argument(name = "maxSteps", description = "Parametr okresla liczbe krokow - 0 = neiskonczona", clazz = Integer.class, defaultvalue = "2"),
		@Argument(name = "compareParameter", description = "Parametr okresla liczbe krokow - 0 = neiskonczona", clazz = Double.class, defaultvalue = "0.2"),
		@Argument(name = "verbose", description = "", clazz = Boolean.class, defaultvalue = "true"),
		@Argument(name = "action", clazz = IAction.class) })
@Description("WorkspaceAgent")
@Agent
public class WorkspaceAgent extends MicroAgent {

	private Logger LOGGER = Logger.getLogger(WorkspaceAgent.class);

	// @formatter:off
	/* ****************************************
	 * ************** ARGUMENTS ***************
	 * ****************************************/
	// @formatter:on
	@AgentArgument
	Boolean verbose; // czy maja byc wyswietlane komunikaty

	@AgentArgument
	Integer maxChildrenAgents; // maksymalna liczba agentow

	@AgentArgument
	Integer initialChildrenAgents; // poczatkowa liczba agentow

	/** (maksymalna) liczba krokow */
	@AgentArgument
	Integer maxSteps;

	/**
	 * parametr wykorzysywany przy porownywaniu stanow agentow w celu ustalenia
	 * jaki procent roznicy ma zostac ododany/ odjety od agentow
	 */
	@AgentArgument
	Double compareParameter;

	/** akcja/e */
	@AgentArgument
	IAction action;

	long startTime;

	// pozostale - potrzebne do funkcjownowania klasy

	/**
	 * aktualna liczba agentow - modyfikiwana przy dodawaniu i usuwaniu agentow
	 */
	Integer actualChildrenCount = 0;

	/**
	 * ostatni uzyty indeks - modyfikowane przy dodawaniu nowego agenta - nigdy
	 * sie nie zmniejsza
	 */
	Integer lastPeerIndex = 0;

	/** aktualny krok */
	Integer actualStep = 0;

	HashSet<IComponentIdentifier> childrenAgents = new HashSet<IComponentIdentifier>(); // zbior
																						// agentow

	List<IComponentIdentifier> agentsToCommunicate = new ArrayList<IComponentIdentifier>();

	ArrayList<IComponentIdentifier> mergeList = new ArrayList<IComponentIdentifier>(); // lista

	ArrayList<ArrayList<IComponentIdentifier>> compareList = new ArrayList<ArrayList<IComponentIdentifier>>();

	@AgentService
	protected IComponentManagementService cms;

	@AgentService
	protected IClockService clockservice;

	// Public methods
	@Override
	@AgentCreated
	public IFuture<Void> agentCreated() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				startTime = System.currentTimeMillis();
				indroduceYourself();
				createInitialPopulation();

				for (IComponentIdentifier childAgent : childrenAgents) {
					agentsToCommunicate.add(childAgent);
				}

				sendAgentsToCommunicate();

				return IFuture.DONE;
			}
		}).get();

		return IFuture.DONE;
	}

	// @formatter:off
	/* **********************************************************************************************
	 * ************************************ WORKPLACE FUNCTIONS *************************************
	 * **********************************************************************************************/
	//@formatter:on
	/** prosta funkcja ktora sie przedstawia */
	private void indroduceYourself() {
		PlatformRegistry.getInstance().register(getComponentIdentifier());

		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					LOGGER.debug("WORKPLACEAGENT - Hello I'm " + getAgentName());
				}

				return IFuture.DONE;
			}
		}).get();
	}

	/** funkcja tworzy poczatkowa populacje agentow */
	private void createInitialPopulation() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					LOGGER.debug("WORKPLACEAGENT - Creating initial population");
					LOGGER.debug("WORKPLACEAGENT - Tworze poczatkowa populacje agentow");
				}
				for (int i = 0; i < initialChildrenAgents; ++i) {
					createChildAgent(10.0);
				}

				return IFuture.DONE;
			}
		}).get();
	}

	private void sendAgentsToCommunicate() {
		IComponentIdentifier[] platformRecivers = new IComponentIdentifier[childrenAgents.size()];

		int reciverIndex = 0;

		LOGGER.debug("Message sending to workplace");
		List<IComponentIdentifier> platformIds = new ArrayList<IComponentIdentifier>(PlatformRegistry.getInstance()
				.getRegisteredIds());
		platformIds.remove(getExternalAccess().getComponentIdentifier());
		platformRecivers = new IComponentIdentifier[platformIds.size()];
		reciverIndex = 0;
		for (IComponentIdentifier reciver : platformIds) {
			platformRecivers[reciverIndex++] = reciver;
		}
		LOGGER.debug("Ids number: " + platformRecivers.length);
		LOGGER.debug("Send from: " + getComponentIdentifier() + " to: "
				+ (platformRecivers.length == 0 ? "" : platformRecivers[0]));
		Map<String, Object> platformMessage = new HashMap<String, Object>();
		platformMessage.put(SFipa.CONTENT, new XStream().toXML(agentsToCommunicate));
		platformMessage.put(SFipa.RECEIVERS, platformRecivers);
		platformMessage.put(SFipa.CONVERSATION_ID, SUtil.createUniqueId(getAgentName()));

		if (platformRecivers.length > 0) {
			sendMessage(platformMessage, SFipa.FIPA_MESSAGE_TYPE);
		}
	}

	/** glowna petla agenta - jej zadaniem jest iteracja po kolejnych krokach */
	@Override
	@AgentBody
	public IFuture<Void> executeBody() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				LOGGER.debug("\n\n\n");
				for (actualStep = 0; actualStep < maxSteps; actualStep++) {
					if (verbose) {
						LOGGER.debug("\n\n" + getAgentName() + " - performing step " + actualStep);
					}

					performStepOnAllAgents();
				}
				return IFuture.DONE;
			}
		}).get();
		return IFuture.DONE;
	}

	/** funkcja iterujaca po child agentach */
	private void performStepOnAllAgents() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				for (final IComponentIdentifier cid : childrenAgents) {
					performStepOnSingleAgent(cid);
				}
				LOGGER.debug("\n");
				manageComparations();
				manageMergeList();

				return IFuture.DONE;
			}
		}).get();
	}

	/** funkcja wywolujaca odpowiedni serwis na wskazanym child agencie */
	private void performStepOnSingleAgent(final IComponentIdentifier cid) {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				LOGGER.debug("\n" + cid.getLocalName());
				SServiceProvider.getService(getServiceProvider(), cid, ICustomAgentService.class).get()
						.doSomething(action).get();
				printAllAgentsStates();
				return IFuture.DONE;
			}
		}).get();
	}

	@Override
	@AgentKilled
	public IFuture<Void> agentKilled() {
		LOGGER.debug(getAgentName() + " killed.");

		long endTime = System.currentTimeMillis();
		System.out.println("Execution time: " + (endTime - startTime) / (double) 1000 + " sek.");

		return IFuture.DONE;
	}

	//@formatter:off
	/* ***********************************************************************************************
	 * *************************************** COMPARE AGENTS ****************************************
	 * ***********************************************************************************************/
	// @formatter:on
	/** Porownywanie stanow agentow */
	private void compareAgents(IComponentIdentifier agent1, IComponentIdentifier agent2) {
		LOGGER.debug(getAgentName() + " - comparing " + agent1.getLocalName() + " with " + agent2.getName());

		ArrayList<IComponentIdentifier> agentsList = new ArrayList<IComponentIdentifier>();
		agentsList.add(agent1);
		agentsList.add(agent2);

		// sprawdzamy czy agenci istnieja
		if (checkIfChildAgentsExists(agentsList)) {
			// pobieramy stany agentow
			Double agentState1 = SServiceProvider.getService(getServiceProvider(), agent1, ICustomAgentService.class)
					.get().getState().get();
			Double agentState2 = SServiceProvider.getService(getServiceProvider(), agent2, ICustomAgentService.class)
					.get().getState().get();

			Double compareResult = (agentState1 - agentState2) * compareParameter;

			LOGGER.debug(getAgentName() + " - comparing agent " + agent1.getName() + " " + agentState1 + ", with "
					+ agent2.getName() + " " + agentState2 + " - " + compareResult);

			SServiceProvider.getService(getServiceProvider(), agent1, ICustomAgentService.class).get()
					.modifyStateBy(compareResult).get();
			SServiceProvider.getService(getServiceProvider(), agent2, ICustomAgentService.class).get()
					.modifyStateBy(-compareResult).get(); // wazny minus!
		}
	}

	public void manageComparations() {
		LOGGER.debug("ManageComparation");
		// uaktualniamy zbior naszych child agentow
		updateChildrenSet();
		LOGGER.debug("iteracja");
		LOGGER.debug(compareList.size());
		for (ArrayList<IComponentIdentifier> agentsToCompare : compareList) {
			if (agentsToCompare.size() == 2) {
				compareAgents(agentsToCompare.get(0), agentsToCompare.get(1));
			} else {
				LOGGER.debug(getAgentName() + " - invalid array list size with agents to compare.");
			}
		}

		compareList.clear();
	}

	public void addToCompareList(final IComponentIdentifier agent1, final IComponentIdentifier agent2) {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				LOGGER.debug("dodaje agentow do listy w celu porownania");
				LOGGER.debug(compareList.size());
				ArrayList<IComponentIdentifier> agentsList = new ArrayList<IComponentIdentifier>();
				agentsList.add(agent1);
				agentsList.add(agent2);
				compareList.add(agentsList);
				LOGGER.debug(compareList.size());
				return IFuture.DONE;
			}
		}).get();
	}

	// @formatter:off
	/* ***********************************************************************************************
	 * **************************************** MERGE AGENTS *****************************************
	 * ***********************************************************************************************/
	//@formatter:off
	public void manageMergeList() {
		LOGGER.debug(getAgentName()
						+ " - Zaczynam przegladac liste agentow chetnych do polaczenia ");
		while (mergeList.size() >= 2) {
			mergeAgents(mergeList.get(0), mergeList.get(1));
			mergeList.remove(0);
			mergeList.remove(0);
		}
	}

	public void mergeAgents(IComponentIdentifier agent1,
			IComponentIdentifier agent2) {
		double stateAgent1 = SServiceProvider
				.getService(getServiceProvider(), agent1,
						ICustomAgentService.class).get().getState().get();
		double stateAgent2 = SServiceProvider
				.getService(getServiceProvider(), agent2,
						ICustomAgentService.class).get().getState().get();
		createChildAgent(stateAgent1 + stateAgent2);
		deleteChildAgent(agent1);
		deleteChildAgent(agent2);
	}

	public void addToMergeList(final IComponentIdentifier cid) {
		if (!mergeList.contains(cid)) {
			mergeList.add(cid);
		}
	}

	//@formatter:off
	/* **********************************************************************************************
	 * ************************************** PRINT FUNCTIONS ***************************************
	 * **********************************************************************************************/
	//@formatter:on

	public void printAllAgentsStates() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				for (final IComponentIdentifier cid : childrenAgents) {
					LOGGER.debug(cid.getName()
							+ " - "
							+ SServiceProvider.getService(getServiceProvider(), cid, ICustomAgentService.class).get()
									.getState().get());
				}
				return IFuture.DONE;
			}
		}).get();
	}

	@Override
	@AgentMessageArrived
	public void messageArrived(Map<String, Object> message, final MessageType messageType) {

		List<IComponentIdentifier> remoteAgents = (List<IComponentIdentifier>) (new XStream().fromXML((String) message
				.get(SFipa.CONTENT)));

		LOGGER.debug(getAgentName() + " - message arrived for workspace, content: ");

		agentsToCommunicate.addAll(remoteAgents);
	}

	/** funkcja wypisujaca child agentow */
	private void printChildrenAgents() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {

				if (verbose) {
					LOGGER.debug("WORKPLACES CHILDREN AGENTS");
					LOGGER.debug(getAgentName() + " - moi agenci " + childrenAgents.size());
				}

				return IFuture.DONE;
			}
		}).get();
	}

	public void printAgentsOnMergeList() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				if (verbose) {
					LOGGER.debug(getAgentName() + " - merge list " + mergeList.size());
				}

				for (IComponentIdentifier a : mergeList) {
					LOGGER.debug(a.getName());
				}

				return IFuture.DONE;
			}
		}).get();
	}

	//@formatter:off
	/* **********************************************************************************************
	 * *************************************** MANAGE AGENTS ****************************************
	 * **********************************************************************************************/
	//@formatter:on

	/** Funkcja tworzy agenta */
	private void createChildAgent(final double initialState) {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				// Wypisanie informacji na konsole o akcji
				if (verbose) {
					LOGGER.debug("WORKPLACEAGENT - Creating Agent: " + getAgentName() + "#CHILD" + lastPeerIndex);
					LOGGER.debug(getAgentName() + " - Tworze agenta: " + getAgentName() + "#CHILD" + lastPeerIndex);
				}

				// Stworzenie informacji przekazywanej nowemu agentowi tj. kim
				// jest jego parent, ustawienie parametrow
				CreationInfo crInfo = new CreationInfo(getComponentIdentifier());
				Map<String, Object> infoArgs = new HashMap<String, Object>();
				infoArgs.put("verbose", verbose);
				infoArgs.put("id", new Integer(lastPeerIndex));
				infoArgs.put("state", new Double(initialState));
				crInfo.setArguments(infoArgs);

				// Właściwe stworzenie agenta
				childrenAgents.add(cms.createComponent(getAgentName() + "#CHILD" + lastPeerIndex,
						CustomAgent.class.getName() + ".class", crInfo, null).get());

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

	private void notifyAllChildAgents(final IComponentIdentifier agentToRemove) {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				for (final IComponentIdentifier cid : childrenAgents) {
					SServiceProvider.getService(getServiceProvider(), cid, ICustomAgentService.class).get()
							.removeAgentFromKnownSiblings(agentToRemove).get();
				}

				return IFuture.DONE;
			}
		}).get();
	}

	public void deleteChildAgent(IComponentIdentifier me) {
		LOGGER.debug("Deleting agent " + me);
		if (checkIfChildExists(me)) {
			cms.destroyComponent(me).get();
			actualChildrenCount--;
			childrenAgents = getChildrenAgents();
			notifyAllChildAgents(me);

		} else {
			LOGGER.debug("There is no child agent " + me);
		}
	}

	/** uaktualnia zbior agentow */
	private void updateChildrenSet() {
		scheduleStep(new IComponentStep<Void>() {

			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				childrenAgents.clear(); // czyscimy zbior
				childrenAgents = getChildrenAgents(); // przypisujemy nowa liste
				return IFuture.DONE;
			}
		}).get();
	}

	/** wyciaga child agentow z funkcji jadexowych */
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

	/** losuje sposrow wszystkich child agentow jednego */
	public IComponentIdentifier getSomeChildAgent() {
		Random r = new Random((new Date()).getTime());

		Integer rNumber = r.nextInt(childrenAgents.size());

		Iterator<IComponentIdentifier> itr = childrenAgents.iterator();
		for (int i = 0; i < rNumber; i++, itr.next()) {
			;
		}

		return itr.next();
	}

	public boolean checkIfChildExists(IComponentIdentifier me) {
		return childrenAgents.contains(me);
	}

	public boolean checkIfChildAgentsExists(ArrayList<IComponentIdentifier> agentsList) {
		boolean result = true;

		for (IComponentIdentifier agent : agentsList) {
			if (!checkIfChildExists(agent)) {
				LOGGER.debug(getAgentName() + " - child agent " + agent.getName() + " does not exist.");
				result = false;
			}
		}

		return result;
	}

	public List<IComponentIdentifier> getAgentsToCommunicate() {
		return agentsToCommunicate;
	}
}