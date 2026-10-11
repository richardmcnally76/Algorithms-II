import java.util.HashSet;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

// import BoggleBoard;

public class BoggleSolver {

    // Trie class that can also report that the input isn't a prefix of any words
    private class MyTrie {

        public static final int MATCH_FOUND = 0;
        public static final int KEEP_SEARCHING = 1;
        public static final int DEAD_END = 2;

        public String decodeResult(int result){
            switch (result) {
                case MATCH_FOUND:
                    return "MATCH FOUND";
                case KEEP_SEARCHING:
                    return "KEEP SEARCHING";
                case DEAD_END:
                    return "DEAD END";
                default:
                    throw new IllegalArgumentException();
            }
        }

        private class MyTrieNode {
            MyTrieNode[] layer;
            Boolean terminal;

            public MyTrieNode() {
                this.layer = new MyTrieNode[26];
                this.terminal = false;
            }
        }

        MyTrieNode root;

        public MyTrie() {
            this.root = new MyTrieNode();
        }

        public void add(String word) {
            MyTrieNode currentNode = this.root;
            for (int i = 0; i < word.length(); i++) {
                int currentChar = word.charAt(i) - 'A';
                if (currentNode.layer[currentChar] == null) {
                    MyTrieNode newNode = new MyTrieNode();
                    currentNode.layer[currentChar] = newNode;
                }
                currentNode = currentNode.layer[currentChar];
            }
            currentNode.terminal = true;
        }

        public int search(char[] word, int wordLen) {
            // Note, the empty string will result in KEEP_SEARCHING
            MyTrieNode currentNode = this.root;
            for (int i = 0; i < wordLen; i++) {
                int currentChar = word[i] - 'A';
                if (currentNode.layer[currentChar] == null)
                    return DEAD_END;
                currentNode = currentNode.layer[currentChar];
            }
            if (currentNode.terminal)
                return MATCH_FOUND;
            return KEEP_SEARCHING;
        }

         public int search(String word) {
            // Note, the empty string will result in KEEP_SEARCHING
            MyTrieNode currentNode = this.root;
            for (int i = 0; i < word.length(); i++) {
                int currentChar = word.charAt(i) - 'A';
                if (currentNode.layer[currentChar] == null)
                    return DEAD_END;
                currentNode = currentNode.layer[currentChar];
            }
            if (currentNode.terminal)
                return MATCH_FOUND;
            return KEEP_SEARCHING;
        }

    }


    private MyTrie dict;
    // Initializes the data structure using the given array of strings as the
    // dictionary.
    // (You can assume each word in the dictionary contains only the uppercase
    // letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        this.dict = new MyTrie();
        for (String word : dictionary) {
            if (word.length() < 3) continue; // Words less then 3 chars long have no value
            dict.add(word);
        }
    }

   private void dfsFindWords(BoggleBoard board, int cellId, HashSet<String> words, char[] word, int wordLen,
            boolean[][] used) {
        int x = cellId % board.cols();
        int y = cellId / board.cols();
        
        if (used[x][y])
            return;

        char thisLetter = board.getLetter(y, x);
        word[wordLen++] = thisLetter;
        if (thisLetter == 'Q')
            word[wordLen++] = 'U';


        int searchResult = this.dict.search(word, wordLen);
        if (searchResult == MyTrie.DEAD_END)
            return;

        if (searchResult == MyTrie.MATCH_FOUND)
            words.add(new String(word, 0, wordLen));

        used[x][y] = true;

        // LEFT
        if (x > 0)
            dfsFindWords(board, cellId - 1, words, word, wordLen, used);
        // RIGHT
        if (x < board.cols() - 1)
            dfsFindWords(board, cellId + 1, words, word, wordLen, used);
        // UP
        if (y > 0)
            dfsFindWords(board, cellId - board.cols(), words, word, wordLen, used);
        // DOWN
        if (y < board.rows() - 1)
            dfsFindWords(board, cellId + board.cols(), words, word, wordLen, used);
        // LEFT AND UP
        if (x > 0 && y > 0)
            dfsFindWords(board, cellId - board.cols() - 1, words, word, wordLen, used);
        // RIGHT AND UP
        if (x < board.cols() - 1 && y > 0)
            dfsFindWords(board, cellId + 1 - board.cols(), words, word, wordLen, used);
        // LEFT AND DOWN
        if (x > 0 && y < board.rows() - 1)
            dfsFindWords(board, cellId - 1 + board.cols(), words, word, wordLen, used);
        // RIGHT AND DOWN
        if (x < board.cols() - 1 && y < board.rows() - 1)
            dfsFindWords(board, cellId + 1 + board.cols(), words, word, wordLen, used);
        
        used[x][y] = false;
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        HashSet<String> words = new HashSet<>();

        int width = board.cols();
        int height = board.rows();

        char[] charBuffer = new char[width*height*2]; // *2 because Q => QU
        
        for (int start = 0; start < width * height; start++){
            dfsFindWords(board, start, words, charBuffer, 0, new boolean[width][height]);
        }
        return words;
    }

    // Returns the score of the given word if it is in the dictionary, zero
    // otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        int wordLen = word.length();
        // This wouldn't be required as the solver only generates words that occur within
        // the dict, except evaluation tests include tests which expect the score to be 0
        // because the word isn't in the dict.
        if (dict.search(word) != MyTrie.MATCH_FOUND)
            return 0;
        if (wordLen < 3)
            return 0;
        if (wordLen <= 4)
            return 1;
        if (wordLen == 5)
            return 2;
        if (wordLen == 6)
            return 3;
        if (wordLen == 7)
            return 5;
        return 11;

    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        String[] dictionary = in.readAllStrings();
        BoggleSolver solver = new BoggleSolver(dictionary);

        BoggleBoard board = new BoggleBoard(args[1]);
        int score = 0;
        for (String word : solver.getAllValidWords(board)) {
            StdOut.println(word);
            score += solver.scoreOf(word);
        }
        StdOut.println("Score = " + score);
    }

}
