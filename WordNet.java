import edu.princeton.cs.algs4.In;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordNet {

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null) throw new IllegalArgumentException("");
        In synsets_in = new In(synsets);
        String[] lines = synsets_in.readAllLines();
        this.synset_values = new String[lines.length];
        for (String line : lines) {
            String[] split_line = line.split(",");
            this.nouns = new HashSet<String>();
            int id = Integer.parseInt(split_line[0]);
            assert (id >= 0 && id < this.synset_values.length);
            nouns.addAll(List.of(split_line[1].trim().split(" ")));
            this.synset_values[id] = split_line[1];
        }

        In hypernym_in = new In(hypernyms);
        lines = hypernym_in.readAllLines();
        this.hypernym_ids = new int[lines.length][];
        for (String line : lines) {
            String[] split_line = line.split(",");
            int id = Integer.parseInt(split_line[0]);
            assert (id >= 0 && id < this.synset_values.length);
            this.hypernym_ids[id] = new int[split_line.length - 1];
            for (int i = 1; i < lines.length; i++) {
                this.hypernym_ids[id][i - 1] = Integer.parseInt(split_line[i]);
            }
        }
        if (!IsDag()) {
            throw new IllegalArgumentException("");
        }
        System.out.printf(
                "Loaded with %d hypernyms, %d synsets, and %d nouns\n",
                this.hypernym_ids.length, this.synset_values.length,
                this.nouns.size());
    }

    private boolean IsDag() {
        return true;
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return this.nouns;
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        return this.nouns.contains(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        assert (isNoun(nounA));
        assert (isNoun(nounB));
        return 0;
    }

    // a synset (second field of synsets.txt) that is the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        assert (isNoun(nounA));
        assert (isNoun(nounB));

        return "";
    }


    // do unit testing of this class
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
    }

    private String[] synset_values;
    private int[][] hypernym_ids;
    private Set<String> nouns;
}
