package sample.games.hangman.handler;


import org.springframework.beans.factory.annotation.Autowired;

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
    private ContextUser user;
    private ContextSettings contextSettings;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {

        Play play = (Play)wrapper;
        
        char ch = play.getLetter().charAt(0);
        
        String word = contextPlay.getWord();
        String displayWord = contextPlay.getDisplayWord();
        StringBuilder sb = new StringBuilder();
        boolean ok = false;
        for(int i=0; i<word.length(); i++) {
        	if(Character.toUpperCase(word.charAt(i)) == ch) {
        		sb.append(ch);
        		ok = true;
        	} else {
        		sb.append(displayWord.charAt(i));
        	}
        }
        contextPlay.setDisplayWord(sb.toString());
        if(!ok) contextPlay.incTries();
        
        if(contextPlay.getTries() > 5) {
            context.addPageMessage(StatusCodes.FAILURE, null, null);
            context.prohibitContinue();
        }
        
    }

    public boolean isActive(Context context) throws Exception {
        return user.getName() != null && contextSettings.getDifficultyLevel() != null;
    }

    public boolean needsData(Context context) throws Exception {
        return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
       
    }

    @Autowired
    public void setUser(ContextUser user) {
        this.user = user;
    }

    @Autowired
    public void setContextPlay(ContextPlay contextPlay) {
    	this.contextPlay = contextPlay;
    }
    
    @Autowired
    public void setContextSettings(ContextSettings contextSettings) {
        this.contextSettings = contextSettings;
    }
    
}
