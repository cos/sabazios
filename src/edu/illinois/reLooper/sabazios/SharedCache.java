package edu.illinois.reLooper.sabazios;

import java.util.HashMap;

import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;

public class SharedCache {
	// cache: for each context, we keep the outside parents of it's objects.
	// if an object has not been analyzed yet, there will be no key for it.
	// if it has been analyzed, then there would be a key and its value would be
	// either null (not shared) or
	// a an outside shared object.
	// so, we keep triples: (context, object_in_context,
	// outside_parent_of_object_in_context)
	private HashMap<FlexibleContext, HashMap<AllocationSiteInNode, AllocationSiteInNode>> sharedObjects = new HashMap<FlexibleContext, HashMap<AllocationSiteInNode, AllocationSiteInNode>>();

	public void putTriple(FlexibleContext context, AllocationSiteInNode sharedObject, AllocationSiteInNode outsideParent) {
		if (!sharedObjects.containsKey(context))
			sharedObjects.put(context, new HashMap<AllocationSiteInNode, AllocationSiteInNode>());

		HashMap<AllocationSiteInNode, AllocationSiteInNode> objectsInContext = sharedObjects.get(context);
		if (objectsInContext.containsKey(sharedObject))
			throw new RuntimeException("We already analyzed this!!!");
		objectsInContext.put(sharedObject, outsideParent);
	}

	public boolean alreadyAnalyzed(FlexibleContext context, AllocationSiteInNode sharedObject) {
		return (sharedObjects.containsKey(context) && sharedObjects.get(context).containsKey(sharedObject));
	}
	
	public AllocationSiteInNode getOutsideObject(FlexibleContext context, AllocationSiteInNode sharedObject) {
		return sharedObjects.get(context).get(sharedObject);
	}
}
