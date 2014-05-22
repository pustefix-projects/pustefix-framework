package sample.games.hangman;

import java.io.Serializable;

public class Score implements Comparable<Score>, Serializable {

    private static final long serialVersionUID = 6905507767971493704L;

    private long time;
    private int misses;
    private DifficultyLevel level;
    private String player;
    private long id;
    
    public Score(long time, int misses, DifficultyLevel level, String player) {
        this.time = time;
        this.misses = misses;
        this.level = level;
        this.player = player;
    }
    
    public long getTime() {
        return time;
    }
    
    public int getMisses() {
        return misses;
    }
    
    public DifficultyLevel getLevel() {
        return level;
    }
    
    public String getPlayer() {
        return player;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int compareTo(Score other) {
        if(level.compareTo(other.level) == 0) {
            if(misses == other.misses) {
                return (int)(time - other.time);
            } else return misses - other.misses;
        } else return other.level.compareTo(level);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Score) {
            Score s = (Score)obj;
            return time == s.time && misses == s.misses && level == s.level && player.equals(s.player);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public String toString() {
        return level + "|" + misses + "|" + time + "|" + player;
    }
    
}
