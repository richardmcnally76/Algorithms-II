import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashMap;
import java.util.LinkedList;

public class BaseballElimination {

    private final int teamCount;
    private final HashMap<String, Integer> nameToIdx;
    private final String[] idxToName;
    private final int[] wins;
    private final int[] losses;
    private final int[] remaining;
    private final int[][] matchups;
    private final HashMap<Integer, LinkedList<String>> eliminatedBy;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);
        this.teamCount = in.readInt();
        this.nameToIdx = new HashMap<String, Integer>();
        this.idxToName = new String[this.teamCount];
        this.wins = new int[this.teamCount];
        this.losses = new int[this.teamCount];
        this.remaining = new int[this.teamCount];
        this.matchups = new int[this.teamCount][this.teamCount];
        this.eliminatedBy = new HashMap<>();

        for (int teamIdx = 0; teamIdx < teamCount; teamIdx++) {
            String teamName = in.readString();

            if (nameToIdx.keySet().contains(teamName))
                throw new IllegalArgumentException("Can't have duplicate team names");
            nameToIdx.put(teamName, teamIdx);
            idxToName[teamIdx] = teamName;
            this.wins[teamIdx] = in.readInt();
            this.losses[teamIdx] = in.readInt();
            this.remaining[teamIdx] = in.readInt();
            for (int matchupIdx = 0; matchupIdx < this.teamCount; matchupIdx++)
                matchups[teamIdx][matchupIdx] = in.readInt();
        }
    }

    // number of teams
    public int numberOfTeams() {
        return this.teamCount;
    }

    // all teams
    public Iterable<String> teams() {
        return this.nameToIdx.keySet();
    }

    private void validateTeam(String team) {
        if (!this.nameToIdx.containsKey(team))
            throw new IllegalArgumentException("No such team: " + team);
    }

    // number of wins for given team
    public int wins(String team) {
        validateTeam(team);
        return this.wins[this.nameToIdx.get(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        validateTeam(team);
        return this.losses[this.nameToIdx.get(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        validateTeam(team);
        return this.remaining[this.nameToIdx.get(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        validateTeam(team1);
        validateTeam(team2);

        return this.matchups[this.nameToIdx.get(team1)][this.nameToIdx.get(team2)];
    }

    private LinkedList<String> cachchedEliminationCheck(int teamIdx) {
        if ( eliminatedBy.containsKey(teamIdx))
            return eliminatedBy.get(teamIdx);
        LinkedList<String> list = new LinkedList<>();

        // First check for simple elimination
        int mostWinsPossible = this.wins[teamIdx] + this.remaining[teamIdx];
        for (int otherTeamIdx = 0; otherTeamIdx < this.teamCount; otherTeamIdx++) {
            if (otherTeamIdx == teamIdx)
                continue; // Can't eliminate yourself
            if (this.wins[otherTeamIdx] > mostWinsPossible) {
                list.add(idxToName[otherTeamIdx]);
            }
        }
        if (!list.isEmpty())
            return  list;


        // (n*(n+1)/2) with n = teamCount -1
        int matchupCount = (this.teamCount - 1) * this.teamCount / 2; 
        int vertexCount = 1 + matchupCount + this.teamCount + 1;


        int matchupIdx = 1; // 0 is the source
        FlowNetwork flowNetwork = new FlowNetwork(vertexCount);
        for (int t1 = 0; t1 < this.teamCount; t1++) {
            double capacity;
            if (t1 == teamIdx)
                capacity = Double.POSITIVE_INFINITY;
            else
                capacity = mostWinsPossible - this.wins[t1];
            // From teams to sink
            flowNetwork.addEdge(new FlowEdge(matchupCount + t1 + 1, vertexCount - 1, capacity));
            for (int t2 = t1 + 1; t2 < this.teamCount; t2++){
                 // Source to matchup
                flowNetwork.addEdge(new FlowEdge(0, matchupIdx, this.matchups[t1][t2]));
                // Matchup to the two teams involved
                flowNetwork.addEdge(new FlowEdge(matchupIdx, matchupCount + 1 + t1, Double.POSITIVE_INFINITY));
                flowNetwork.addEdge(new FlowEdge(matchupIdx, matchupCount + 1 + t2, Double.POSITIVE_INFINITY));
                matchupIdx++;
            }
        }

        FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, 0, vertexCount - 1);
        for (int t = 0; t < this.teamCount; t++) {
            if (fordFulkerson.inCut(1 + matchupCount + t))
                list.add(idxToName[t]);
        }
        
        // Save the result
        eliminatedBy.put(teamIdx, list);
        return list;

    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        validateTeam(team);

        int teamIdx = this.nameToIdx.get(team);
        return !cachchedEliminationCheck(teamIdx).isEmpty();
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        validateTeam(team);
        int teamIdx = this.nameToIdx.get(team);
        LinkedList<String> eliminatedByResult = cachchedEliminationCheck(teamIdx);
        if (eliminatedByResult.isEmpty())
            return null;
        return eliminatedByResult;
    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}