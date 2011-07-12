package racefix;

import java.util.HashSet;

import extra166y.Ops;
import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class Foo {
	public double x, y, m;
	Foo origin, origin1;

	public void simple() {
		ParallelArray<Foo> particles = ParallelArray.createUsingHandoff(new Foo[10],
				ParallelArray.defaultExecutor());

		particles.apply(new Ops.Procedure<Foo>() {
			@Override
			public void op(Foo b) {
			}
		});
	}
}