/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.illinois.reLooper.sabazios.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.graph.Graph;

/**
 * This class searches breadth-first nodes that match some criteria. 
 * 
 * This class follows the outNodes of the graph nodes to define the graph, but this behavior can be changed by overriding the
 * getConnected method.
 * 
*/
@SuppressWarnings("deprecation")
public class NodeFinder<T> {

  private final boolean DEBUG = false;

  /**
   * The graph to search
   */
  final private Graph<T> G;

  /**
   * The Filter which defines the target set of nodes to find
   */
  final private Filter<T> filter;

  /**
   * an enumeration of all nodes to search from
   */
  final private Iterator<T> roots;

  /**
   * Construct a breadth-first enumerator starting with a particular node in a directed graph.
   * 
   * @param G the graph whose nodes to enumerate
   */
  public NodeFinder(Graph<T> G, T N, Filter<T> f) {
    if (G == null) {
      throw new IllegalArgumentException("G is null");
    }
    if (f == null) {
      throw new IllegalArgumentException("null f");
    }
    this.G = G;
    this.roots = new NonNullSingletonIterator<T>(N);
    this.filter = f;
  }

  /**
   * Construct a breadth-first enumerator starting with a particular node in a directed graph.
   * 
   * @param G the graph whose nodes to enumerate
   * @throws IllegalArgumentException if G is null
   */
  public NodeFinder(Graph<T> G, T src, final T target) throws IllegalArgumentException {
    if (G == null) {
      throw new IllegalArgumentException("G is null");
    }
    this.G = G;
    this.roots = new NonNullSingletonIterator<T>(src);
    if (!G.containsNode(src)) {
      throw new IllegalArgumentException("src is not in graph " + src);
    }
    this.filter = new Filter<T>() {
      @Override
	public boolean accepts(T o) {
        return target.equals(o);
      }
    };
  }

  /**
   * Construct a breadth-first enumerator starting with a particular node in a directed graph.
   * 
   * @param G the graph whose nodes to enumerate
   */
  public NodeFinder(Graph<T> G, T src, Iterator<T> targets) {
    if (targets == null) {
      throw new IllegalArgumentException("targets is null");
    }
    final Set<T> ts = HashSetFactory.make();
    while (targets.hasNext()) {
      ts.add(targets.next());
    }

    this.G = G;
    this.roots = new NonNullSingletonIterator<T>(src);

    this.filter = new Filter<T>() {
      @Override
	public boolean accepts(T o) {
        return ts.contains(o);
      }
    };
  }

  /**
   * Construct a breadth-first enumerator starting with any of a set of nodes in a directed graph.
   * 
   * @param G the graph whose nodes to enumerate
   */
  public NodeFinder(Graph<T> G, Iterator<T> sources, final T target) {
    if (G == null) {
      throw new IllegalArgumentException("G is null");
    }
    if (sources == null) {
      throw new IllegalArgumentException("sources is null");
    }
    this.G = G;
    this.roots = sources;
    this.filter = new Filter<T>() {
      @Override
	public boolean accepts(T o) {
        return target.equals(o);
      }
    };
  }

  /**
   * Construct a breadth-first enumerator across the (possibly improper) subset of nodes reachable from the nodes in the given
   * enumeration.
   * 
   * @param nodes the set of nodes from which to start searching
   */
  public NodeFinder(Graph<T> G, Iterator<T> nodes, Filter<T> f) {
    this.G = G;
    this.roots = nodes;
    this.filter = f;
    if (G == null) {
      throw new IllegalArgumentException("G is null");
    }
    if (roots == null) {
      throw new IllegalArgumentException("roots is null");
    }
  }

  /**
   * @return a List of nodes that specifies the first path found from a root to a node accepted by the filter. Returns null if no
   *         path found.
   */
  public Set<T> find() {

    LinkedList<T> Q = new LinkedList<T>();
    HashMap<Object, T> history = HashMapFactory.make();
    while (roots.hasNext()) {
      T next = roots.next();
      Q.addLast(next);
      history.put(next, null);
    }
    while (!Q.isEmpty()) {
      T N = Q.removeFirst();
      if (DEBUG) {
        System.err.println(("visit " + N));
      }
      if (filter.accepts(N)) {
    	 result.add(N);
      }
      Iterator<? extends T> children = getConnected(N);
      while (children.hasNext()) {
        T c = children.next();
        if (!history.containsKey(c)) {
          Q.addLast(c);
          history.put(c, N);
        }
      }
    }

    return result;
  }

  HashSet<T> result = new HashSet<T>();

  /**
   * get the out edges of a given node
   * 
   * @param n the node of which to get the out edges
   * @return the out edges
   * 
   */
  protected Iterator<? extends T> getConnected(T n) {
    return G.getSuccNodes(n);
  }
}
