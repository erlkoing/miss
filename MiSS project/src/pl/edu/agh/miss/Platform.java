package pl.edu.agh.miss;

import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ThreadSuspendable;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Platform {
	String workspaceAgentName;
	IFuture<IExternalAccess> platformFuture;
	ThreadSuspendable threadSuspendable;
	IComponentManagementService componentManagementService;

	private Logger LOGGER = Logger.getLogger(getClass());

	public IFuture<IExternalAccess> createCustomPlatform(String[] args, String workspaceAgentName) {
		this.workspaceAgentName = workspaceAgentName;

		platformFuture = Starter.createPlatform(args);

		threadSuspendable = new ThreadSuspendable();
		IExternalAccess platformExternalAccess = platformFuture.get(threadSuspendable);
		LOGGER.debug("Started platform: " + platformExternalAccess.getComponentIdentifier());

		// Get the CMS service from the platform
		componentManagementService = SServiceProvider.getService(platformExternalAccess.getServiceProvider(),
				IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM).get(threadSuspendable);

		return platformFuture;
	}

	public void createWorkplace() {
		// Start the chat componentname
		CreationInfo creationInfo = new CreationInfo();
		Map<String, Object> infoArgs = new HashMap<String, Object>();
		infoArgs.put("maxChildrenAgents", new Integer(100));
		infoArgs.put("initialChildrenAgents", new Integer(25));
		infoArgs.put("maxSteps", new Integer(100));
		infoArgs.put("action", new RandomNumberAction());
		infoArgs.put("verbose", new Boolean(true));
		creationInfo.setArguments(infoArgs);

		IComponentIdentifier componentId = componentManagementService.createComponent(workspaceAgentName,
				WorkspaceAgent.class.getName() + ".class", creationInfo, null).get(threadSuspendable);

		LOGGER.debug("Started component: " + componentId);
	}

}
