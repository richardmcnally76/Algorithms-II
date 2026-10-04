import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class SAP {

    private final Digraph G;
    private final HashMap<Integer, HashMap<Integer, Integer>> reachableCache;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        this.G = G;
        this.reachableCache = new HashMap<>();
    }

    private boolean validVertex(int v) {
        return v >= 0 && v < G.V();
    }


    private HashMap<Integer, Integer> reachable(int from) {
        if (this.reachableCache.containsKey(from))
            return this.reachableCache.get(from);

        BreadthFirstDirectedPaths dp = new BreadthFirstDirectedPaths(G, from);
        HashMap<Integer, Integer> result = new HashMap<>();
        for (int vertex = 0; vertex < G.V(); vertex++) {
            int distance = dp.distTo(vertex);
            if (distance == Integer.MAX_VALUE) continue;
            result.put(vertex, distance);
        }
        this.reachableCache.put(from, result);
        return result;
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (!validVertex(v))
            throw new IllegalArgumentException("Invalid v:" + v);
        if (!validVertex(w))
            throw new IllegalArgumentException("Invalid w:" + w);

        HashMap<Integer, Integer> reachableV = reachable(v);
        HashMap<Integer, Integer> reachableW = reachable(w);

        HashSet<Integer> inBoth = new HashSet<>(reachableV.keySet());
        inBoth.retainAll(reachableW.keySet());

        if (inBoth.isEmpty()) return -1;

        int result = Integer.MAX_VALUE;
        for (int shared : inBoth) {
            result = Math.min(result,
                    reachableV.get(shared) + reachableW.get(shared));
        }
        return result;
    }

    // a common ancestor of v and w that participates in a shortest ancestral
    // path; -1 if no such path
    public int ancestor(int v, int w) {
        if (!validVertex(v))
            throw new IllegalArgumentException("Invalid v:" + v);
        if (!validVertex(w))
            throw new IllegalArgumentException("Invalid w:" + w);

        HashMap<Integer, Integer> reachableV = reachable(v);
        HashMap<Integer, Integer> reachableW = reachable(w);

        HashSet<Integer> inBoth = new HashSet<>(reachableV.keySet());
        inBoth.retainAll(reachableW.keySet());

        int minDistance = Integer.MAX_VALUE;
        int result = -1;

        for (int shared : inBoth) {
            int thisDistance = reachableV.get(shared) + reachableW.get(shared);
            if (thisDistance < minDistance) {
                minDistance = thisDistance;
                result = shared;
            }
        }
        return result;
    }

    // length of shortest ancestral path between any vertex in v and any
    // vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("Null argument passed");

        int minDistance = Integer.MAX_VALUE;
        boolean matchFound = false;

        for (Integer vNode : v) {
            if (vNode == null || !validVertex(vNode))
                throw new IllegalArgumentException();
            for (Integer wNode : w) {
                if (wNode == null || !validVertex(wNode))
                    throw new IllegalArgumentException();

                int thisLength = length(vNode, wNode);
                if (thisLength != -1) {
                    minDistance = Math.min(minDistance, thisLength);
                    matchFound = true;
                }
            }
        }

        if (!matchFound) return -1;
        return minDistance;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if
    // no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("Null argument passed");

        int minDistance = Integer.MAX_VALUE;
        int minV = -1;
        int minW = -1;

        for (Integer vNode : v) {
            if (vNode == null || !validVertex(vNode))
                throw new IllegalArgumentException();
            for (Integer wNode : w) {
                if (wNode == null || !validVertex(wNode))
                    throw new IllegalArgumentException();

                int thisLength = length(vNode, wNode);
                if (thisLength < minDistance) {
                    minDistance = thisLength;
                    minV = vNode;
                    minW = wNode;
                }
            }
        }

        if (minV == -1) return -1;
        return ancestor(minV, minW);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        // 0 -> 1 -> 2
        //      3 /
        // 4 -> 5
        Digraph G = new Digraph(6);
        G.addEdge(0, 1);
        G.addEdge(1, 2);
        G.addEdge(3, 2);
        G.addEdge(4, 5);

        SAP sap = new SAP(G);

        HashMap<Integer, Integer> reachable = sap.reachable(0);
        assert reachable.containsKey(0);
        assert reachable.containsKey(1);
        assert reachable.containsKey(2);
        assert !reachable.containsKey(3);
        assert !reachable.containsKey(4);
        assert !reachable.containsKey(5);

        assert sap.length(0, 3) == 3;
        assert sap.length(1, 3) == 2;
        assert sap.length(0, 4) == -1;

        assert sap.ancestor(0, 3) == 2;
        assert sap.ancestor(1, 1) == 1;
        assert sap.ancestor(1, 4) == -1;

        assert sap.length(List.of(0, 1), List.of(3)) == 2;
        assert sap.length(List.of(0), List.of(1, 3)) == 1;
        assert sap.length(List.of(0, 1, 3), List.of(4, 5)) == -1;
        assert sap.length(List.of(0, 1), List.of(0)) == 0;

        try {
            LinkedList<Integer> badInput = new LinkedList<>();
            badInput.add(4);
            badInput.add(5);
            badInput.add(null);
            sap.length(List.of(0, 1, 3), badInput);
        } catch (IllegalArgumentException expected) {
            // Test passed
        }

        assert sap.ancestor(List.of(0, 1), List.of(3)) == 2;
        assert sap.ancestor(List.of(0, 1, 3), List.of(4, 5)) == -1;
        assert sap.ancestor(List.of(0, 1), List.of(0)) == 0;
    }
}
