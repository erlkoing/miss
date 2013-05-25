package pl.edu.agh.miss;

import java.util.HashMap;
import java.util.Map;

import jadex.base.Starter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.IFuture;
import jadex.commons.future.ThreadSuspendable;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[]	defargs	= new String[]
		{
			"-gui", "false",
			"-welcome", "true",
			"-cli", "true",
			"-printpass", "false",
			"-logging", "false"
		};
		String[]	newargs	= new String[defargs.length+args.length];
		System.arraycopy(defargs, 0, newargs, 0, defargs.length);
		System.arraycopy(args, 0, newargs, defargs.length, args.length);
		
		// TODO Auto-generated method stub
		IFuture<IExternalAccess>	platfut	= Starter.createPlatform(newargs);
		
		ThreadSuspendable	sus	= new ThreadSuspendable();
		IExternalAccess	platform	= platfut.get(sus);
		System.out.println("Started platform: "+platform.getComponentIdentifier());

		// Get the CMS service from the platform
		IComponentManagementService	cms	= SServiceProvider.getService(platform.getServiceProvider(),
			IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM).get(sus);
		
		// Start the chat componentname
		CreationInfo ci = new CreationInfo();
		Map<String, Object> infoArgs = new HashMap<String, Object>();
		infoArgs.put("maxChildrenAgents", new Integer(5000));
		infoArgs.put("initialChildrenAgents", new Integer(100));
		infoArgs.put("maxSteps", new Integer(4));
		infoArgs.put("action", new RandomNumberAction());
		infoArgs.put("verbose", new Boolean(true));
		ci.setArguments(infoArgs);
		
		IComponentIdentifier cid	= cms.createComponent("WorkspaceAgent", WorkspaceAgent.class.getName()+".class", ci, null).get(sus);
		System.out.println("Started component: "+cid);	
	}

}