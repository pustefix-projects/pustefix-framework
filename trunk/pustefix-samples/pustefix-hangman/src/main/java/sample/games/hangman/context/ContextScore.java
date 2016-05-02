package sample.games.hangman.context;

import sample.games.hangman.Score;

public class ContextScore {
    
    private Score lastScore;
    private boolean topScore;
    
    public void reset() {
        lastScore = null;
        topScore = false;
    }
    
    public void setLastScore(Score lastScore) {
        this.lastScore = lastScore;
    }
    
    public void setTopScore(boolean topScore) {
        this.topScore = topScore;
    }
    
    public Score getLastScore() {
        return lastScore;
    }
    
    public boolean isTopScore() {
        return topScore;
    }

}
