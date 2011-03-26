package edu.illinois.reLooper.sabazios;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;

@SuppressWarnings("deprecation")
final class PathToSharedFinder extends BFSPathFinder<Object> {

	static final class InstanceKeyIsSharedFilter implements Filter<Object> {
		private final FlexibleContext currentContext;
		private final SharedCache sharedObjects;

		private InstanceKeyIsSharedFilter(FlexibleContext currentContext, SharedCache sharedObjects) {
			this.currentContext = currentContext;
			this.sharedObjects = sharedObjects;
		}

		@Override
		public boolean accepts(Object iK) {
			if (iK instanceof PointerKey)
				return false;
			AllocationSiteInNode currentObject = (AllocationSiteInNode) iK;

			// if found a shared outside object, stop
			if (sharedObjects.alreadyAnalyzed(currentContext, currentObject)
					&& sharedObjects.getOutsideObject(currentContext, currentObject) != null)
				return true;

			return Analysis.isAllocationSharedInContext(currentObject, currentContext);
		}
	}
	
	private final Set<InstanceKey> currentElement;
	private final FlexibleContext currentContext;
	private final SharedCache sharedObjects;

	PathToSharedFinder(Graph<Object> G, Object startNode, Set<InstanceKey> currentElement,
			final FlexibleContext currentContext, SharedCache sharedObjects) {
		super(G, startNode, new InstanceKeyIsSharedFilter(currentContext, sharedObjects));
		this.currentElement = currentElement;
		this.currentContext = currentContext;
		this.sharedObjects = sharedObjects;
	}

	public AllocationSiteInNode outsideSharedObject = null;

	public AllocationSiteInNode result() {
		List<Object> found = this.find();
		if (outsideSharedObject != null)
			return outsideSharedObject;

		if (found != null)
			return (AllocationSiteInNode) found.get(0);

		return null;
	}

	@Override
	protected Iterator<? extends Object> getConnected(Object n) {
		// stop traversing when reaching a current element
		if (currentElement.contains(n) || (n instanceof AllocationSiteInNode && sharedObjects.alreadyAnalyzed(currentContext, (AllocationSiteInNode) n)))
			return (new HashSet<Object>()).iterator();
		else
			return super.getConnected(n);
	}
}