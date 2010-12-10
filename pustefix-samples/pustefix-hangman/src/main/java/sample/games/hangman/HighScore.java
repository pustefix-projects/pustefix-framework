package sample.games.hangman;

import java.util.SortedSet;
import java.util.TreeSet;

public class HighScore {
    
    private static int MAX_SIZE = 10;
    private SortedSet<Score> scores = new TreeSet<Score>();
    
    public HighScore() {
        addScore(new Score(13000,4,DifficultyLevel.BEGINNER,"foo"));
        addScore(new Score(13000,3,DifficultyLevel.BEGINNER,"foo"));
        addScore(new Score(10000,3,DifficultyLevel.BEGINNER,"foo"));
        addScore(new Score(3410400,3,DifficultyLevel.ADVANCED,"foo"));
        addScore(new Score(10333,3,DifficultyLevel.EXPERT,"foo"));
        addScore(new Score(10000,2,DifficultyLevel.EXPERT,"foo"));
    }
    
    public synchronized boolean addScore(Score score) {
        scores.add(score);
        if(scores.size() > MAX_SIZE) scores.remove(scores.last());
        return scores.contains(score);
    }
    
    public synchronized Score[] getScores() {
        return scores.toArray(new Score[scores.size()]);
    }

}
