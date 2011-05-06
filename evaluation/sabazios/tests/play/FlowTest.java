package sabazios.tests.play;

import java.io.IOException;
import java.util.Iterator;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

import edu.illinois.reLooper.sabazios.WalaAnalysis;

public class FlowTest extends WalaAnalysis {
	public static void main(String[] args) throws ClassHierarchyException, IllegalArgumentException, CancelException,
			IOException {
		FlowTest walaAnalysis = new FlowTest();
		walaAnalysis.addBinaryDependency("sandbox");
		walaAnalysis.setup("Lsandbox/Flow",  MAIN_METHOD);
		walaAnalysis.compute();
	}

	private void compute() {
		HeapGraph heapGraph = this.pointerAnalysis.getHeapGraph();

		Iterator<Object> iteratorH = heapGraph.iterator();
		while (iteratorH.hasNext()) {
			Object object = (Object) iteratorH.next();
			if (!(object instanceof LocalPointerKey))
				continue;
			LocalPointerKey p = (LocalPointerKey) object;
			if(!p.getNode().toString().contains("bla"))
				continue;
			
			Iterator<Object> succNodes = heapGraph.getSuccNodes(p);
			while (succNodes.hasNext()) {
				Object object2 = (Object) succNodes.next();
				System.out.println(object2);
			}
		}
	}
}
