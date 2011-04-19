package sample.games.hangman.handler;


import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.Dictionary;
import sample.games.hangman.HighScore;
import sample.games.hangman.Score;
import sample.games.hangman.context.ContextPlay;
import sample.games.hangman.context.ContextScore;
import sample.games.hangman.context.ContextSettings;
import sample.games.hangman.context.ContextUser;
import sample.games.hangman.wrapper.Play;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class PlayHandler implements IHandler {

	private ContextPlay contextPlay;
    private ContextUser contextUser;
    private ContextSettings contextSettings;
    private Dictionary dictionary;
    private HighScore highScore;
    private ContextScore contextScore;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {

        Play play = (Play)wrapper;
        char ch = play.getLetter().charAt(0);
        contextPlay.guess(ch);

        if(contextPlay.isCompletedSuccessful()) {
            Score score = new Score(contextPlay.getTime(), contextPlay.getMisses(), 
                                        contextPlay.getLevel(), contextUser.getName());
            boolean topScore = highScore.addScore(score);
            contextScore.setLastScore(score);
            contextScore.setTopScore(topScore);
        }
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return contextUser.getName() != null && contextSettings.getDifficultyLevel() != null;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        if(contextPlay.getWord() == null) {
            String word = dictionary.getRandomWord(contextSettings.getLocale(), contextSettings.getDifficultyLevel());
            contextPlay.setWord(word);
            contextPlay.setLevel(contextSettings.getDifficultyLevel());
            contextPlay.start();
            contextScore.reset();
        }
    }

    @Autowired
    public void setUser(ContextUser contextUser) {
        this.contextUser = contextUser;
    }

    @Autowired
    public void setContextPlay(ContextPlay contextPlay) {
    	this.contextPlay = contextPlay;
    }
    
    @Autowired
    public void setContextSettings(ContextSettings contextSettings) {
        this.contextSettings = contextSettings;
    }
    
    @Autowired
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
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
