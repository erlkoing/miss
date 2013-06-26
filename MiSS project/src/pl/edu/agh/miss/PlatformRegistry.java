package pl.edu.agh.miss;

import jadex.bridge.IComponentIdentifier;

import java.util.ArrayList;
import java.util.List;

public class PlatformRegistry {
	private static PlatformRegistry platformRegistry;

	private List<IComponentIdentifier> registeredIds = new ArrayList<IComponentIdentifier>();

	private PlatformRegistry() {
	}

	public static PlatformRegistry getInstance() {
		if (platformRegistry == null) {
			platformRegistry = new PlatformRegistry();
		}
		return platformRegistry;
	}

	public List<IComponentIdentifier> getRegisteredIds() {
		return registeredIds;
	}

	public void register(IComponentIdentifier platformId) {
		registeredIds.add(platformId);
	}

	public void unregister(IComponentIdentifier platformId) {
		registeredIds.remove(platformId);
	}
}
