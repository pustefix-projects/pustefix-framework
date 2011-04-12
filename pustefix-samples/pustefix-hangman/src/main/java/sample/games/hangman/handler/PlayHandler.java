package sample.games.hangman.handler;


import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.Dictionary;
import sample.games.hangman.HighScore;
import sample.games.hangman.Score;
import sample.games.hangman.StatusCodes;
import sample.games.hangman.context.ContextPlay;
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
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {

        Play play = (Play)wrapper;
        if(!contextPlay.isCompleted()) {
            char ch = play.getLetter().charAt(0);
            contextPlay.guess(ch);
            if(contextPlay.isCompletedFaulty()) {
                context.addPageMessage(StatusCodes.FAILURE, new String[] {contextPlay.getWord()}, null);
                context.prohibitContinue();
            } else if(contextPlay.isCompletedSuccessful()) {
                int rank = highScore.addScore(new Score(contextPlay.getTime(), contextPlay.getMisses(),
                        dictionary.getDifficultyLevel(contextPlay.getWord()), contextUser.getName()));
                if(rank > -1) context.addPageMessage(StatusCodes.SUCCESS_HIGH, null, null);
                else context.addPageMessage(StatusCodes.SUCCESS, null , null);
                context.prohibitContinue();
            }
        }
    }

    public boolean isActive(Context context) throws Exception {
        return contextUser.getName() != null && contextSettings.getDifficultyLevel() != null;
    }

    public boolean needsData(Context context) throws Exception {
        return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        if(contextPlay.getWord() == null) {
            String word = dictionary.getRandomWord(contextSettings.getLocale(), contextSettings.getDifficultyLevel());
            contextPlay.setWord(word);
            contextPlay.start();
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
    
}
