package sample.games.hangman.webservice;

import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.HighScore;
import sample.games.hangman.Score;
import sample.games.hangman.context.ContextPlay;
import sample.games.hangman.context.ContextScore;
import sample.games.hangman.context.ContextUser;

public class PlayImpl implements Play {

    private ContextPlay contextPlay;
    private ContextUser contextUser;
    private ContextScore contextScore;
    private HighScore highScore;
    
    public ContextPlay guess(char ch) {

        contextPlay.guess(ch);
        if(contextPlay.isCompletedSuccessful()) {
            Score score = new Score(contextPlay.getTime(), contextPlay.getMisses(), 
                                    contextPlay.getLevel(), contextUser.getName()); 
            boolean topScore = highScore.addScore(score);
            contextScore.setLastScore(score);
            contextScore.setTopScore(topScore);
        }
        return contextPlay;
    }
    
    @Autowired
    public void setContextPlay(ContextPlay contextPlay) {
        this.contextPlay = contextPlay;
    }
    
    @Autowired
    public void setUser(ContextUser contextUser) {
        this.contextUser = contextUser;
    }
    
    @Autowired
    public void setHighScore(HighScore highScore) {
        this.highScore = highScore;
    }
    
    @Autowired
    public void setContextScore(ContextScore contextScore) {
        this.contextScore = contextScore;
    }
    
}
