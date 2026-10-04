import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


public class WordNet {

    private final String[] synsetValues;
    private final HashMap<String, LinkedList<Integer>> theNouns;
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
        this.theNouns = new HashMap<String, LinkedList<Integer>>();
        for (String line : lines) {
            String[] splitLine = line.split(",");
            int id = Integer.parseInt(splitLine[0]);
            assert (id >= 0 && id < this.synsetValues.length);
            for (String noun : splitLine[1].trim().split(" ")) {
                if (!this.theNouns.containsKey(noun)) {
                    this.theNouns.put(noun, new LinkedList<>());
                }
                this.theNouns.get(noun).add(id);
            }
            this.synsetValues[id] = splitLine[1];
        }

        Digraph digraph = new Digraph(this.synsetValues.length);
        In hypernymIn = new In(hypernyms);
        lines = hypernymIn.readAllLines();
        for (String line : lines) {
            String[] splitLine = line.split(",");
            int id = Integer.parseInt(splitLine[0]);
            assert (id >= 0 && id < this.synsetValues.length);
            for (int i = 1; i < splitLine.length; i++) {
                digraph.addEdge(id, Integer.parseInt(splitLine[i]));
            }
        }
        DirectedCycle directedCycle = new DirectedCycle(digraph);
        if (directedCycle.hasCycle()) {
            throw new IllegalArgumentException("Inputs do not define a DAG");
        }

        int roots = 0;
        for (int i = 0; i < digraph.V(); i++)
            if (digraph.outdegree(i) == 0)
                roots++;

        if (roots != 1)
            throw new IllegalArgumentException("Too many roots: " + roots);

        this.sap = new SAP(digraph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return this.theNouns.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("word is null");
        return this.theNouns.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA)) throw new IllegalArgumentException();
        if (!isNoun(nounB)) throw new IllegalArgumentException();

        return this.sap.length(this.theNouns.get(nounA), this.theNouns.get(nounB));
    }

    // a synset (second field of synsets.txt) that is the common ancestor of
    // nounA and nounB in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA)) throw new IllegalArgumentException();
        if (!isNoun(nounB)) throw new IllegalArgumentException();

        int id = sap.ancestor(this.theNouns.get(nounA), this.theNouns.get(nounB));
        if (id == -1) return null;

        return this.synsetValues[id];
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Iterator<String> it = wordnet.nouns().iterator();
        String a =  it.next();
        String b = it.next();
        wordnet.sap(a, b);
    }
}
