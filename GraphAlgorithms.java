package TheLondonTube;
import java.util.Set;
import java.util.HashSet;

public class GraphAlgorithms {
	public static <V, E> void DFS(Graph<V, E> g, Vertex<V> u, Set<Vertex<V>> known, Map<Vertex<V>, Edge<E>> forest) {
		known.add(u); // u has been discovered
		for (Edge<E> e : g.outgoingEdges(u)) { // for every outgoing edge from u
			Vertex<V> v = g.opposite(u, e);
			if (!known.contains(v)) {
				forest.put(v, e); // e is the tree edge that discovered v
				DFS(g, v, known, forest); // recursively explore from v
			}
		}
	}

	public static <V, E> PositionalList<Edge<E>> constructPath(Graph<V, E> g, Vertex<V> u, Vertex<V> v,
			Map<Vertex<V>, Edge<E>> forest) {
		PositionalList<Edge<E>> path = new LinkedPositionalList<>();
		if (forest.get(v) != null) { // v was discovered during the search
			Vertex<V> walk = v; // we construct the path from back to front
			while (walk != u) {
				Edge<E> edge = forest.get(walk);
				path.addFirst(edge); // add edge to *front* of path
				walk = g.opposite(walk, edge); // repeat with opposite endpoint
			}
		}
		return path;
	}

	public static <V, E> Map<Vertex<V>, Edge<E>> DFSComplete(Graph<V, E> g) {
		Set<Vertex<V>> known = new HashSet<>();
		Map<Vertex<V>, Edge<E>> forest = new ProbeHashMap<>();
		for (Vertex<V> u : g.vertices())
			if (!known.contains(u))
				DFS(g, u, known, forest); // (re)start the DFS process at u
		return forest;
	}

	public static <V, E> void BFS(Graph<V, E> g, Vertex<V> s, Set<Vertex<V>> known, Map<Vertex<V>, Edge<E>> forest) {
		PositionalList<Vertex<V>> level = new LinkedPositionalList<>();
		known.add(s);
		level.addLast(s); // first level includes only s
		while (!level.isEmpty()) {
			PositionalList<Vertex<V>> nextLevel = new LinkedPositionalList<>();
			for (Vertex<V> u : level)
				for (Edge<E> e : g.outgoingEdges(u)) {
					Vertex<V> v = g.opposite(u, e);
					if (!known.contains(v)) {
						known.add(v);
						forest.put(v, e); // e is the tree edge that discovered v
						nextLevel.addLast(v); // v will be further considered in next pass
					}
				}
			level = nextLevel; // relabel 'next' level to become the current
		}
	}

	public static <V, E> Map<Vertex<V>, Edge<E>> BFSComplete(Graph<V, E> g) {
		Map<Vertex<V>, Edge<E>> forest = new ProbeHashMap<>();
		Set<Vertex<V>> known = new HashSet<>();
		for (Vertex<V> u : g.vertices())
			if (!known.contains(u))
				BFS(g, u, known, forest);
		return forest;
	}

	@SuppressWarnings({ "unchecked" })
	public static <V, E> void transitiveClosure(Graph<V, E> g) {
		for (Vertex<V> k : g.vertices())
			for (Vertex<V> i : g.vertices())
				// verify that edge (i,k) exists in the partial closure
				if (i != k && g.getEdge(i, k) != null)
					for (Vertex<V> j : g.vertices())
						// verify that edge (k,j) exists in the partial closure
						if (i != j && j != k && g.getEdge(k, j) != null)
							// if (i,j) not yet included, add it to the closure
							if (g.getEdge(i, j) == null)
								g.insertEdge(i, j, null);
	}

  public static <V,E> PositionalList<Vertex<V>> topologicalSort(Graph<V,E> g) {
    // list of vertices placed in topological order
    PositionalList<Vertex<V>> topo = new LinkedPositionalList<>();
    // container of vertices that have no remaining constraints
    Stack<Vertex<V>> ready = new LinkedStack<>();
    // map keeping track of remaining in-degree for each vertex
    Map<Vertex<V>, Integer> inCount = new ProbeHashMap<>();
    for (Vertex<V> u : g.vertices()) {
      inCount.put(u, g.inDegree(u));           // initialize with actual in-degree
      if (inCount.get(u) == 0)                 // if u has no incoming edges,
        ready.push(u);                         // it is free of constraints
    }
    while (!ready.isEmpty()) {
      Vertex<V> u = ready.pop();
      topo.addLast(u);
      for (Edge<E> e : g.outgoingEdges(u)) {   // consider all outgoing neighbors of u
        Vertex<V> v = g.opposite(u, e);
        inCount.put(v, inCount.get(v) - 1);    // v has one less constraint without u
        if (inCount.get(v) == 0)
          ready.push(v);
      }
    }
    return topo;
  }

/*
 * public static <V> Map<Vertex<V>, Integer>
 * shortestPathLengths(Graph<V,Integer> g, Vertex<V> src) { // d.get(v) is upper
 * bound on distance from src to v Map<Vertex<V>, Integer> d = new
 * ProbeHashMap<>(); // map reachable v to its d value Map<Vertex<V>, Integer>
 * cloud = new ProbeHashMap<>(); // pq will have vertices as elements, with
 * d.get(v) as key AdaptablePriorityQueue<Integer, Vertex<V>> pq; pq = new
 * HeapAdaptablePriorityQueue<>(); // maps from vertex to its pq locator
 * Map<Vertex<V>, Entry<Integer,Vertex<V>>> pqTokens; pqTokens = new
 * ProbeHashMap<>();
 * 
 * // for each vertex v of the graph, add an entry to the priority queue, with
 * // the source having distance 0 and all others having infinite distance for
 * (Vertex<V> v : g.vertices()) { if (v == src) d.put(v,0); else d.put(v,
 * Integer.MAX_VALUE); pqTokens.put(v, pq.insert(d.get(v), v)); // save entry
 * for future updates } // now begin adding reachable vertices to the cloud
 * while (!pq.isEmpty()) { Entry<Integer, Vertex<V>> entry = pq.removeMin(); int
 * key = entry.getKey(); Vertex<V> u = entry.getValue(); cloud.put(u, key); //
 * this is actual distance to u pqTokens.remove(u); // u is no longer in pq for
 * (Edge<Integer> e : g.outgoingEdges(u)) { Vertex<V> v = g.opposite(u,e); if
 * (cloud.get(v) == null) { // perform relaxation step on edge (u,v) int wgt =
 * e.getElement(); if (d.get(u) + wgt < d.get(v)) { // better path to v?
 * d.put(v, d.get(u) + wgt); // update the distance
 * pq.replaceKey(pqTokens.get(v), d.get(v)); // update the pq entry } } } }
 * return cloud; // this only includes reachable vertices }
 */

/*
 * public static <V> Map<Vertex<V>,Edge<Integer>> spTree(Graph<V,Integer> g,
 * Vertex<V> s, Map<Vertex<V>,Integer> d) { Map<Vertex<V>, Edge<Integer>> tree =
 * new ProbeHashMap<>(); for (Vertex<V> v : d.keySet()) if (v != s) for
 * (Edge<Integer> e : g.incomingEdges(v)) { // consider INCOMING edges Vertex<V>
 * u = g.opposite(v, e); int wgt = e.getElement(); if (d.get(v) == d.get(u) +
 * wgt) tree.put(v, e); // edge is is used to reach v } return tree; }
 */

/*
 * public static <V> PositionalList<Edge<Integer>> MST(Graph<V,Integer> g) { //
 * tree is where we will store result as it is computed
 * PositionalList<Edge<Integer>> tree = new LinkedPositionalList<>(); // pq
 * entries are edges of graph, with weights as keys PriorityQueue<Integer,
 * Edge<Integer>> pq = new HeapPriorityQueue<>(); // union-find forest of
 * components of the graph Partition<Vertex<V>> forest = new Partition<>(); //
 * map each vertex to the forest position Map<Vertex<V>,Position<Vertex<V>>>
 * positions = new ProbeHashMap<>();
 * 
 * for (Vertex<V> v : g.vertices()) positions.put(v, forest.makeCluster(v));
 * 
 * for (Edge<Integer> e : g.edges()) pq.insert(e.getElement(), e);
 * 
 * int size = g.numVertices(); // while tree not spanning and unprocessed edges
 * remain... while (tree.size() != size - 1 && !pq.isEmpty()) { Entry<Integer,
 * Edge<Integer>> entry = pq.removeMin(); Edge<Integer> edge = entry.getValue();
 * Vertex<V>[] endpoints = g.endVertices(edge); Position<Vertex<V>> a =
 * forest.find(positions.get(endpoints[0])); Position<Vertex<V>> b =
 * forest.find(positions.get(endpoints[1])); if (a != b) { tree.addLast(edge);
 * forest.union(a,b); } }
 * 
 * return tree; } 
 */ 

}