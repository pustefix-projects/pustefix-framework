package sample.games.hangman;

import java.io.Serializable;

public class Score implements Comparable<Score>, Serializable {

    private static final long serialVersionUID = 6905507767971493704L;

    private long time;
    private int tries;
    private DifficultyLevel level;
    private String player;
    
    public Score(long time, int tries, DifficultyLevel level, String player) {
        this.time = time;
        this.tries = tries;
        this.level = level;
        this.player = player;
    }
    
    public long getTime() {
        return time;
    }
    
    public int getTries() {
        return tries;
    }
    
    public DifficultyLevel getLevel() {
        return level;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public int compareTo(Score other) {
        if(level.compareTo(other.level) == 0) {
            if(tries == other.tries) {
                return (int)(time - other.time);
            } else return tries - other.tries;
        } else return other.level.compareTo(level);
    }
    
    @Override
    public String toString() {
        return level + "|" + tries + "|" + time + "|" + player;
    }
    
}
