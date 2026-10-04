
public class Outcast {

    private final WordNet wordnet;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        this.wordnet = wordnet;
    }

    // given an array of WordNet nouns return an outcast
    public String outcast(String[] nouns) {
        int maxTotalDistance = 0;
        String outcast = null;

        for (String noun : nouns) {
            int totalDistance = 0;
            for (String otherNoun : nouns) {
                if (noun.equals(otherNoun)) continue;
                totalDistance += wordnet.distance(noun, otherNoun);
            }

            if (totalDistance > maxTotalDistance) {
                maxTotalDistance = totalDistance;
                outcast = noun;
            }
        }
        return outcast;
    }

    // see test client below
    public static void main(String[] args) {
        // Intentionally empty
    }
}