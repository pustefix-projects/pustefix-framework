package sample.games.hangman.handler;


import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.Dictionary;
import sample.games.hangman.context.ContextPlay;
import sample.games.hangman.context.User;
import sample.games.hangman.wrapper.Registration;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class RegistrationHandler implements IHandler {

	private Dictionary dictionary;
	private ContextPlay contextPlay;
    private User user;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {

        Registration registration = (Registration)wrapper;
        user.setName(registration.getName());
        
        String word = dictionary.getRandomWord();
        contextPlay.setWord(word);
        contextPlay.setDisplayWord(word.replaceAll("." , "_"));
        
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return user.getName() == null;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        if(user.getName() != null) {
            Registration registration = (Registration)wrapper;
            registration.setName(user.getName());
        }
    }

    @Autowired
    public void setUser(User user) {
        this.user = user;
    }
    
    @Autowired
    public void setDictionary(Dictionary dictionary) {
    	this.dictionary = dictionary;
    }
    
    @Autowired
    public void setContextPlay(ContextPlay contextPlay) {
    	this.contextPlay = contextPlay;
    }

}
