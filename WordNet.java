import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class WordNet {

    private final Digraph digraph;
    private final String[] synsetValues;
    private final HashMap<String, LinkedList<Integer>> nouns;
    private final SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) {
            throw new IllegalArgumentException("Two args required, " +
                    "<synsets-file> and <hypernyms-file>");
        }
        In synsetsIn = new In(synsets);
        String[] lines = synsetsIn.readAllLines();
        this.synsetValues = new String[lines.length];
        this.nouns = new HashMap<String, LinkedList<Integer>>();
        for (String line : lines) {
            String[] splitLine = line.split(",");
            int id = Integer.parseInt(splitLine[0]);
            assert (id >= 0 && id < this.synsetValues.length);
            for (String noun : splitLine[1].trim().split(" ")) {
                if (!this.nouns.containsKey(noun)) {
                    this.nouns.put(noun, new LinkedList<>());
                }
                this.nouns.get(noun).add(id);
            }
            this.synsetValues[id] = splitLine[1];
        }

        this.digraph = new Digraph(this.synsetValues.length);
        In hypernymIn = new In(hypernyms);
        lines = hypernymIn.readAllLines();
        for (String line : lines) {
            String[] splitLine = line.split(",");
            int id = Integer.parseInt(splitLine[0]);
            assert (id >= 0 && id < this.synsetValues.length);
            for (int i = 1; i < splitLine.length; i++) {
                this.digraph.addEdge(id, Integer.parseInt(splitLine[i]));
            }
        }
        if (hasCycles()) {
            throw new IllegalArgumentException("Inputs do not define a DAG");
        }

        int roots = 0;
        for (int i = 0; i < this.digraph.V(); i++)
            if (this.digraph.outdegree(i) == 0)
                roots++;

        if (roots != 1)
            throw new IllegalArgumentException("Too many roots: " + roots);

        this.sap = new SAP(this.digraph);
    }

    private boolean hasCycles() {
        LinkedList<Integer> path = new LinkedList<>();
        HashSet<Integer> toVisit = new HashSet<>();
        LinkedList<Integer> queue = new LinkedList<>();
        for (int i = 0; i < this.synsetValues.length; i++) {
            toVisit.add(i);
        }

        while (!toVisit.isEmpty()) {
            path.clear();
            int firstNode = toVisit.iterator().next();
            toVisit.remove(firstNode);

            path.add(firstNode);
            for (int childId : this.digraph.adj(firstNode)) {
                queue.add(childId);
            }

            while (!queue.isEmpty()) {
                int next = queue.removeLast();
                if (path.contains(next)) {
                    return true;
                }
                toVisit.remove(next);
                for (int childId : this.digraph.adj(next)) {
                    queue.add(childId);
                }
            }
        }
        return false;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return this.nouns.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("word is null");
        return this.nouns.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA)) throw new IllegalArgumentException();
        if (!isNoun(nounB)) throw new IllegalArgumentException();

        return this.sap.length(this.nouns.get(nounA), this.nouns.get(nounB));
//        LinkedList<Integer> nounIdsA = this.nouns.get(nounA);
//        LinkedList<Integer> nounIdsB = this.nouns.get(nounB);
//        int ancestor = sap.ancestor(nounIdsA, nounIdsB);
//        if (ancestor == -1) return -1;
//        return sap.length(nounIdsA, List.of(ancestor)) + sap.length(nounIdsB,
//                List.of(ancestor));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of
    // nounA and nounB in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA)) throw new IllegalArgumentException();
        if (!isNoun(nounB)) throw new IllegalArgumentException();

        int id = sap.ancestor(this.nouns.get(nounA), this.nouns.get(nounB));
        if (id == -1) return null;

        return this.synsetValues[id];
    }

    // do unit testing of this class
    public static void main(String[] args) {
//        WordNet wordnet = new WordNet(args[0], args[1]);
//        wordnet.distance("cat", "dog");
//        wordnet.distance("house", "domicile");
//        wordnet.distance("fridge", "cooler");
//        wordnet.sap("cat", "dog");
//        wordnet.sap("house", "domicile");
//        wordnet.sap("fridge", "cooler");
//        System.out.printf("calls to constructor: %d\n",
//                wordnet.sap.constructorCalls);
//        System.out.printf("calls to length: %d\n", wordnet.sap.lengthCalls);
//        System.out.printf("calls to ancestor: %d\n", wordnet.sap.ancestorCalls);
    }
}
