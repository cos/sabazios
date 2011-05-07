package sabazios.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

import sabazios.ConcurrentAccesses;
import sabazios.domains.ConcurrentAccess;
import sabazios.domains.Loop;
import sabazios.domains.ObjectAccess;
import sabazios.tests.RaceAssert.ThreadId;
import sabazios.util.U;

public class RaceAssert {
	enum ThreadId {
		FIRST, SECOND
	};

	private HashMap<String, HashMap<String, HashMap<ThreadId, HashMap<String, Integer>>>> expectedRaces = new HashMap<String, HashMap<String, HashMap<ThreadId, HashMap<String, Integer>>>>();

	private final ConcurrentAccesses actualRaces;

	public RaceAssert(ConcurrentAccesses actualRaces) {
		this.actualRaces = actualRaces;
	}

	String currentLoop = null;

	public RaceAssert loop(String loop) {
		currentLoop = loop;
		if (!expectedRaces.containsKey(loop))
			expectedRaces.put(loop, new HashMap<String, HashMap<ThreadId, HashMap<String, Integer>>>());
		return this;
	}

	String currentObject = null;

	public RaceAssert object(String object) {
		this.currentObject = object;
		if (!expectedRaces.get(currentLoop).containsKey(object)) {
			HashMap<ThreadId, HashMap<String, Integer>> forObj = this.expectedRaces.get(currentLoop).put(object,
					new HashMap<RaceAssert.ThreadId, HashMap<String, Integer>>());
			forObj.put(ThreadId.FIRST, new HashMap<String, Integer>());
			forObj.put(ThreadId.SECOND, new HashMap<String, Integer>());
		}
		return this;
	}

	public RaceAssert first(String instruction) {
		this.expectedRaces.get(currentLoop).get(currentObject).get(ThreadId.FIRST).put(instruction, 0);
		return this;
	}

	public RaceAssert second(String instruction) {
		this.expectedRaces.get(currentLoop).get(currentObject).get(ThreadId.SECOND).put(instruction, 0);
		return this;
	}

	public void now() {
		for (Loop loop : this.actualRaces.keySet()) {
			if (this.expectedRaces.containsKey(loop.toString())) {
				HashMap<String, HashMap<ThreadId, HashMap<String, Integer>>> expectedForLoop = this.expectedRaces
						.get(loop.toString());
				TreeSet<ConcurrentAccess> actualForLoop = this.actualRaces.get(loop);

				for (ConcurrentAccess concurrentAccess : actualForLoop) {
					if (expectedForLoop.containsKey(U.tos(concurrentAccess.o))) {
						HashMap<String, Integer> expectedWritesForObject = expectedForLoop.get(
								concurrentAccess.o.toString()).get(ThreadId.FIRST);
						HashMap<String, Integer> expectedReadsForObject = expectedForLoop.get(
								concurrentAccess.o.toString()).get(ThreadId.SECOND);

						for (ObjectAccess objectAccess : concurrentAccess.writeAccesses) {
							if (expectedWritesForObject.containsKey(objectAccess.toString()))
								continue;
							else
								extraWrite(loop, concurrentAccess.o, objectAccess);
						}

						for (ObjectAccess objectAccess : concurrentAccess.writeAccesses) {
							if (expectedReadsForObject.containsKey(objectAccess.toString()))
								continue;
							else
								extraRead(loop, concurrentAccess.o, objectAccess);
						}
					} else {
						extraObject(loop, concurrentAccess.o);
					}
				}
			} else {
				extraLoop(loop);
			}
		}
	}

	private void extraRead(Loop loop, InstanceKey o, ObjectAccess objectAccess) {
		// TODO
	}

	private void extraWrite(Loop loop, InstanceKey o, ObjectAccess objectAccess) {
		// TODO Auto-generated method stub

	}

	private void extraObject(Loop loop, InstanceKey o) {
		System.out.println("Extra object:\n    Loop: "+loop+"\n    Object: "+U.tos(o));

	}

	private void extraLoop(Loop loop) {
		System.out.println("Extra loop: "+loop);
	}
}
