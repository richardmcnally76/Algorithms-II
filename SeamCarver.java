import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.Stopwatch;

import java.awt.Color;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;


public class SeamCarver {

    private int adjCalls = 0;
    private int[][] buf;
    private int width;
    private int height;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException();

        this.width = picture.width();
        this.height = picture.height();
        this.buf = new int[this.width][this.height];
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                buf[x][y] = picture.getRGB(x, y);
//                buf[x][y] = picture.getARGB(x, y);
            }
        }
    }

    // current picture
    public Picture picture() {
        Picture picture = new Picture(width, height);
        for (int x = 0; x < this.width; x++)
            for (int y = 0; y < this.height; y++)
                picture.setRGB(x, y, this.buf[x][y]);
//                picture.setARGB(x, y, this.buf[x][y]);
        return picture;
    }

    // width of current picture
    public int width() {
        return this.width;
    }

    // height of current picture
    public int height() {
        return this.height;
    }

    // The graph generated will have virtual vertices for the source and sink.
    // Finding a seam becomes equivalent to finding the shortest path between
    // those two virtual vertices.
    private final static int VIRTUAL_VERTEX_COUNT = 2;
    private final static int VIRTUAL_SOURCE_VERTEX = 0;
    private final static int VIRTUAL_SINK_VERTEX = 1;

    private int V() {
        return this.width * this.height + VIRTUAL_VERTEX_COUNT;
    }

    private int xyToV(int x, int y) {
        assert x >= 0 : "x must be > 0";
        assert y >= 0 : "y must be > 0";
        assert x < this.width : "x must be <= width";
        assert y < this.height : "y must be <= height";

        return y * this.width + x + VIRTUAL_VERTEX_COUNT;
    }

    private int vToX(int v) {
        assert v >= VIRTUAL_VERTEX_COUNT : "No x for virtual vertex: " + v;
        assert v < V() : "Vertex > " + V() + ": " + v;
        return (v - VIRTUAL_VERTEX_COUNT) % this.width;
    }

    private int vToY(int v) {
        assert v >= VIRTUAL_VERTEX_COUNT : "No x for virtual vertex: " + v;
        assert v < V() : "Vertex > " + V() + ": " + v;
        return (v - VIRTUAL_VERTEX_COUNT) / this.width;
    }

    private int[] vsToXs(int[] vs) {
        assert vs != null : "vs must not be null";
        int[] xs = new int[vs.length];
        for (int i = 0; i < vs.length; i++)
            xs[i] = vToX(vs[i]);
        return xs;
    }

    private int[] vsToYs(int[] vs) {
        assert vs != null : "vertices must not be null";
        int[] ys = new int[vs.length];
        for (int i = 0; i < vs.length; i++)
            ys[i] = vToY(vs[i]);
        return ys;
    }

    private DirectedEdge getDirectedEdge(int from, int toX, int toY,
                                         boolean td) {
        int v = xyToV(toX, toY);
        double weight = 1000.0; // Default for borders.

        // Using default for the last row/column is inefficient. It will cause
        // more exploration trying to avoid what is in reality a fixed cost.
        // Removing the "if (...) {...} else" will restore all borders to 1000.
        if ((td && toY == this.height - 1) || (!td && toX == this.width - 1)) {
            weight = 0;
        } else if (toX > 0 && toY > 0 && toX < this.width - 1 && toY < this.height - 1) {
            weight = energy(toX, toY);
        }
        return new DirectedEdge(from, xyToV(toX, toY), weight);
    }

    // Dynamically generate adjacency data as needed.
    private Iterable<DirectedEdge> adj(int v, boolean topDown) {
        adjCalls++;
        LinkedList<DirectedEdge> edges = new LinkedList<DirectedEdge>();

        // The source node has edges to each top/left vertex as appropriate.
        if (v == VIRTUAL_SOURCE_VERTEX) {
            if (topDown) for (int i = 0; i < this.width; i++)
                edges.add(new DirectedEdge(0, xyToV(i, 0), 1000.0));
            else for (int i = 0; i < this.height; i++)
                edges.add(new DirectedEdge(0, xyToV(0, i), 1000.0));
            return edges;
        }

        // There are no edge from the sink.
        if (v == VIRTUAL_SINK_VERTEX) return edges;

        int x = vToX(v);
        int y = vToY(v);

        if (topDown) {
            // Edge from the bottom row to the sink
            if (y == this.height - 1) {
                edges.add(new DirectedEdge(v, 1, 0.0));
                return edges;
            }
            if (x > 0) edges.add(getDirectedEdge(v, x - 1, y + 1, topDown));
            edges.add(getDirectedEdge(v, x, y + 1, topDown));
            if (x < this.width - 1)
                edges.add(getDirectedEdge(v, x + 1, y + 1, topDown));
        } else {
            // Edge from the right column to the sink
            if (x == this.width - 1) {
                edges.add(new DirectedEdge(v, 1, 0.0));
                return edges;
            }
            if (y > 0) edges.add(getDirectedEdge(v, x + 1, y - 1, topDown));
            edges.add(getDirectedEdge(v, x + 1, y, topDown));
            if (y < this.height - 1)
                edges.add(getDirectedEdge(v, x + 1, y + 1, topDown));
        }
        return edges;
    }


    private void validateCoords(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
            throw new IllegalArgumentException("Bad coord: " + x + "," + y);
    }

    private int[] dijkstraSingleSourceAndSink(int source, int sink,
                                              boolean topDown) {
        boolean[] seen = new boolean[V()];
        double[] cost = new double[V()];
        int[] parent = new int[V()];
        PriorityQueue<Integer> pq =
                new PriorityQueue<>((a, b) -> Double.compare(cost[a], cost[b]));


        for (int v = 0; v < V(); v++) {
            seen[v] = false;
            cost[v] = -1.0;
            parent[v] = -1;
        }

        pq.add(source);
        seen[source] = true;
        cost[source] = 0.0;

        while (!pq.isEmpty()) {
            int v = pq.poll();

            if (v == sink) {
                LinkedList<Integer> reversePath = new LinkedList<>();
                while (v != -1) {
                    reversePath.add(v);
                    v = parent[v];
                }
                int[] path = new int[reversePath.size()];
                for (int i = 0; i < reversePath.size(); i++)
                    path[i] = reversePath.get(path.length - i - 1);
                return path;
            }

            for (DirectedEdge e : adj(v, topDown)) {
                if (seen[e.to()]) continue;
                seen[e.to()] = true;
                cost[e.to()] = cost[v] + e.weight();
                parent[e.to()] = v;
                pq.add(e.to());
            }
        }
        return null;
    }

    static private int rFromRBG(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    static private int gFromRBG(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    static private int bFromRBG(int rgb) {
        return (rgb) & 0xFF;
    }


    private double gradientPart(int a, int b) {
        double deltaRSquired = Math.pow(rFromRBG(a) - rFromRBG(b), 2);
        double deltaGSquared = Math.pow(gFromRBG(a) - gFromRBG(b), 2);
        double deltaBSquared = Math.pow(bFromRBG(a) - bFromRBG(b), 2);
        return deltaRSquired + deltaGSquared + deltaBSquared;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        validateCoords(x, y);
        if (x == 0 || y == 0 || x == this.width - 1 || y == this.height - 1)
            return 1000;
        Color c = new Color(buf[x][y]);
        double partX = gradientPart(this.buf[x - 1][y], this.buf[x + 1][y]);
        double partY = gradientPart(this.buf[x][y - 1], this.buf[x][y + 1]);
        return Math.sqrt(partX + partY);
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        int[] seamVs = dijkstraSingleSourceAndSink(0, 1, false);
        assert seamVs != null;
        return vsToYs(Arrays.copyOfRange(seamVs, 1, seamVs.length - 1));
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int[] seamVs = dijkstraSingleSourceAndSink(0, 1, true);
        assert seamVs != null;
        return vsToXs(Arrays.copyOfRange(seamVs, 1, seamVs.length - 1));
    }

    private void validateSeam(int[] seam, int length, int maxValue) {
        if (seam == null) throw new IllegalArgumentException("Null seam");
        if (seam.length != length)
            throw new IllegalArgumentException("Invalid seam length: " +
                    "should be" + " " + length + ", got " + seam.length);
        int last = seam[0];
        for (int s : seam) {
            if (s < 0) throw new IllegalArgumentException("Seam value < 0");
            if (s > maxValue)
                throw new IllegalArgumentException("Seam value > max");
            if (Math.abs(s - last) > 1)
                throw new IllegalArgumentException("Seam jumps");
            last = s;
        }
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        validateSeam(seam, this.width, this.height - 1);
        for (int x = 0; x < this.width; x++) {
            // Everything remains the same from before the seam is hit, then
            // move everything forward 1 pixel.
            for (int y = seam[x]; y < this.height - 1; y++) {
                buf[x][y] = buf[x][y + 1];
            }
        }
        this.height--;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        validateSeam(seam, this.height, this.width - 1);
        for (int y = 0; y < this.height; y++) {
            // Everything remains the same from before the seam is hit, then
            // move everything forward 1 pixel.
            for (int x = seam[y]; x < this.width - 1; x++) {
                buf[x][y] = buf[x + 1][y];
            }
        }
        this.width--;
    }

    //  unit testing (optional)


    private boolean t(int v, boolean topDown, String expected) {
        String result = this.adj(v, topDown).toString();
        if (result.equals(expected)) return true;
        System.out.println("doAdjTest returning false");
        System.out.printf("  Expected: %s\n", expected);
        System.out.printf("  Got:      %s\n", result);
        return false;
    }

    public static void main(String[] args) {
        Picture picture = new Picture("seam/6x5.png");
        assert picture.width() == 6;
        assert picture.height() == 5;

        SeamCarver sc = new SeamCarver(picture);
        assert sc.t(2, true, "[2->8 1000.00, 2->9 237.35]");
        assert sc.t(3, true, "[3->8 1000.00, 3->9 237.35, 3->10 151.02]");
        assert sc.t(4, true, "[4->9 237.35, 4->10 151.02, 4->11 234.09]");
        assert sc.t(5, true, "[5->10 151.02, 5->11 234.09, 5->12 107.89]");
        assert sc.t(6, true, "[6->11 234.09, 6->12 107.89, 6->13 1000.00]");
        assert sc.t(7, true, "[7->12 107.89, 7->13 1000.00]");
        assert sc.t(8, true, "[8->14 1000.00, 8->15 138.69]");
        assert sc.t(9, true, "[9->14 1000.00, 9->15 138.69, 9->16 228.10]");
        assert sc.t(10, true, "[10->15 138.69, 10->16 228.10, 10->17 133.07]");
        assert sc.t(11, true, "[11->16 228.10, 11->17 133.07, 11->18 211.51]");
        assert sc.t(12, true, "[12->17 133.07, 12->18 211.51, 12->19 1000.00]");
        assert sc.t(13, true, "[13->18 211.51, 13->19 1000.00]");
        assert sc.t(14, true, "[14->20 1000.00, 14->21 153.88]");
        assert sc.t(15, true, "[15->20 1000.00, 15->21 153.88, 15->22 174.01]");
        assert sc.t(16, true, "[16->21 153.88, 16->22 174.01, 16->23 284.01]");
        assert sc.t(17, true, "[17->22 174.01, 17->23 284.01, 17->24 194.50]");
        assert sc.t(18, true, "[18->23 284.01, 18->24 194.50, 18->25 1000.00]");
        assert sc.t(19, true, "[19->24 194.50, 19->25 1000.00]");
        // These are accurate without the optimization in getDirectedEdge.
        // assert sc.t(20, true, "[20->26 1000.00, 20->27 1000.00]");
        // assert sc.t(21, true, "[21->26 1000.00, 21->27 1000.00, 21->28
        // 1000.00]");
        // assert sc.t(22, true, "[22->27 1000.00, 22->28 1000.00, 22->29
        // 1000.00]");
        // assert sc.t(23, true, "[23->28 1000.00, 23->29 1000.00, 23->30
        // 1000.00]");
        // assert sc.t(24, true, "[24->29 1000.00, 24->30 1000.00, 24->31
        // 1000.00]");
        // assert sc.t(25, true, "[25->30 1000.00, 25->31 1000.00]");

        assert sc.t(20, true, "[20->26  0.00, 20->27  0.00]");
        assert sc.t(21, true, "[21->26  0.00, 21->27  0.00, 21->28  0.00]");
        assert sc.t(22, true, "[22->27  0.00, 22->28  0.00, 22->29  0.00]");
        assert sc.t(23, true, "[23->28  0.00, 23->29  0.00, 23->30  0.00]");
        assert sc.t(24, true, "[24->29  0.00, 24->30  0.00, 24->31  0.00]");
        assert sc.t(25, true, "[25->30  0.00, 25->31  0.00]");

        assert sc.t(26, true, "[26->1  0.00]");
        assert sc.t(27, true, "[27->1  0.00]");
        assert sc.t(28, true, "[28->1  0.00]");
        assert sc.t(29, true, "[29->1  0.00]");
        assert sc.t(30, true, "[30->1  0.00]");
        assert sc.t(31, true, "[31->1  0.00]");

        int[] seam = sc.findVerticalSeam();
        assert seam != null;
        assert List.of(seam[1], seam[1] + 1, seam[1] - 1).contains(seam[0]) :
                "" + seam[0];
        assert seam[1] == 4 : "" + seam[1];
        assert seam[2] == 3 : "" + seam[2];
        assert seam[3] == 2 : "" + seam[3];
        assert List.of(seam[3], seam[3] + 1, seam[3] - 1).contains(seam[4]) :
                "" + seam[3];


        seam = sc.findHorizontalSeam();
        assert seam != null;
        assert List.of(seam[1], seam[1] + 1, seam[1] - 1).contains(seam[0]);
        assert seam[1] == 2;
        assert seam[2] == 1;
        assert seam[3] == 2;
        assert seam[4] == 1;
        assert List.of(seam[4], seam[4] + 1, seam[4] - 1).contains(seam[5]);

        System.out.printf("Total calls to adj: %d\n", sc.adjCalls);

        int[][] coords = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {0,
                1}, {1, 1}, {2, 1}, {3, 1}, {4, 1}, {5, 1}, {0, 2}, {1, 2},
                {2, 2}, {3, 2}, {4, 2}, {5, 2}, {0, 3}, {1, 3}, {2, 3}, {3,
                3}, {4, 3}, {5, 3}, {0, 4}, {1, 4}, {2, 4}, {3, 4}, {4, 4},
                {5, 4}};
        int[] vs = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};

        assert vs.length == sc.V() - 2;
        for (int i = 0; i < vs.length; i++) {
            assert sc.vToX(vs[i]) == coords[i][0] :
                    "vToX(" + vs[i] + ") != " + coords[i][0];
            assert sc.vToY(vs[i]) == coords[i][1] :
                    "vToY(" + vs[i] + ") != " + coords[i][1];
            assert sc.xyToV(coords[i][0], coords[i][1]) == vs[i] :
                    "xyToV(" + coords[i][0] + ", " + coords[i][1] + ") != " + vs[i];
        }

        picture = new Picture("seam/6x5.png");
        int[] verticalSeam = {3, 4, 3, 2, 1};
        sc = new SeamCarver(picture);
        sc.removeVerticalSeam(verticalSeam);

        picture = new Picture("seam/10x12.png");
        verticalSeam = new int[]{5, 6, 7, 8, 7, 7, 6, 7, 6, 5, 6, 5};
        sc = new SeamCarver(picture);
        sc.removeVerticalSeam(verticalSeam);

        picture = new Picture("seam/6x5.png");
        verticalSeam = new int[]{5, 4, 3, 4, 4};
        sc = new SeamCarver(picture);
        sc.removeVerticalSeam(verticalSeam);

        boolean correctExceptionThrown = false;
        try {
            picture = new Picture("seam/6x5.png");
            sc = new SeamCarver(picture);
            sc.energy(6, 4);
        }
        catch (IllegalArgumentException e) {
            correctExceptionThrown = true;
        }
        finally{
            assert correctExceptionThrown : "Wrong exception type";
        }

        picture = new Picture("seam/chameleon.png");
        sc = new SeamCarver(picture);

        Stopwatch stopwatch = new Stopwatch();
        for (int i = 0; i < 50; i++) {
            verticalSeam = sc.findVerticalSeam();
            sc.removeVerticalSeam(verticalSeam);
        }
        System.out.printf("Removing 200 vertical seams took %01.2f seconds\n",
                stopwatch.elapsedTime());
    }
}
