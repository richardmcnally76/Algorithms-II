import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;


import java.util.LinkedList;
import java.util.List;

public class SAP {

    private final Digraph G;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        this.G = new Digraph(G);
    }

    private boolean validVertex(int v) {
        return v >= 0 && v < G.V();
    }


    // Was caching, but heap memory was exceeded.
    private BreadthFirstDirectedPaths getCachedBfs(Iterable<Integer> v) {
        return new BreadthFirstDirectedPaths(G, v);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        if (!validVertex(v))
            throw new IllegalArgumentException("Invalid v:" + v);
        if (!validVertex(w))
            throw new IllegalArgumentException("Invalid w:" + w);

        BreadthFirstDirectedPaths bfsV = getCachedBfs(List.of(v));
        BreadthFirstDirectedPaths bfsW = getCachedBfs(List.of(w));

        int shortestDistance = Integer.MAX_VALUE;
        int ancestor = -1;

        for (int i = 0; i < this.G.V(); i++) {
            int vDist = bfsV.distTo(i);
            if (vDist == Integer.MAX_VALUE) continue;
            int wDist = bfsW.distTo(i);
            if (wDist == Integer.MAX_VALUE) continue;

            int thisDistance = vDist + wDist;
            if (thisDistance < shortestDistance) {
                shortestDistance = thisDistance;
                ancestor = i;
            }
        }

        if (ancestor == -1) return -1;

        return shortestDistance;
    }


    // a common ancestor of v and w that participates in a shortest ancestral
    // path; -1 if no such path
    public int ancestor(int v, int w) {
        if (!validVertex(v))
            throw new IllegalArgumentException("Invalid v:" + v);
        if (!validVertex(w))
            throw new IllegalArgumentException("Invalid w:" + w);

        BreadthFirstDirectedPaths bfsV = getCachedBfs(List.of(v));
        BreadthFirstDirectedPaths bfsW = getCachedBfs(List.of(w));

        int shortestDistance = Integer.MAX_VALUE;
        int ancestor = -1;

        for (int i = 0; i < this.G.V(); i++) {
            int vDist = bfsV.distTo(i);
            if (vDist == Integer.MAX_VALUE) continue;
            int wDist = bfsW.distTo(i);
            if (wDist == Integer.MAX_VALUE) continue;

            int thisDistance = vDist + wDist;
            if (thisDistance < shortestDistance) {
                shortestDistance = thisDistance;
                ancestor = i;
            }
        }

        return ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any
    // vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("Null argument passed");
        boolean empty = true;
        for (Integer node : v) {
            empty = false;
            if (node == null || !this.validVertex(node))
                throw new IllegalArgumentException("Invalid v:" + node);
        }
        if  (empty) return -1;
        empty = true;
        for (Integer node : w) {
            empty = false;
            if (node == null || !this.validVertex(node))
                throw new IllegalArgumentException("Invalid v:" + node);
        }
        if (empty) return -1;

        BreadthFirstDirectedPaths bfsV = getCachedBfs(v);
        BreadthFirstDirectedPaths bfsW = getCachedBfs(w);

        int shortestDistance = Integer.MAX_VALUE;
        int ancestor = -1;

        for (int i = 0; i < this.G.V(); i++) {
            int vDist = bfsV.distTo(i);
            if (vDist == Integer.MAX_VALUE) continue;
            int wDist = bfsW.distTo(i);
            if (wDist == Integer.MAX_VALUE) continue;

            int thisDistance = vDist + wDist;
            if (thisDistance < shortestDistance) {
                shortestDistance = thisDistance;
                ancestor = i;
            }
        }

        if (ancestor == -1) return -1;
        return shortestDistance;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if
    // no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("Null argument passed");
        boolean empty = true;
        for (Integer node : v) {
            empty = false;
            if (node == null || !this.validVertex(node))
                throw new IllegalArgumentException("Invalid v:" + node);
        }
        if  (empty) return -1;
        empty = true;
        for (Integer node : w) {
            empty = false;
            if (node == null || !this.validVertex(node))
                throw new IllegalArgumentException("Invalid v:" + node);
        }
        if (empty) return -1;

        BreadthFirstDirectedPaths bfsV = getCachedBfs(v);
        BreadthFirstDirectedPaths bfsW = getCachedBfs(w);

        int shortestDistance = Integer.MAX_VALUE;
        int ancestor = -1;

        for (int i = 0; i < this.G.V(); i++) {
            int vDist = bfsV.distTo(i);
            if (vDist == Integer.MAX_VALUE) continue;
            int wDist = bfsW.distTo(i);
            if (wDist == Integer.MAX_VALUE) continue;

            int thisDistance = vDist + wDist;
            if (thisDistance < shortestDistance) {
                shortestDistance = thisDistance;
                ancestor = i;
            }
        }

        return ancestor;
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
