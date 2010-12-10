package sample.games.hangman.handler;


import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.DifficultyLevel;
import sample.games.hangman.context.ContextSettings;
import sample.games.hangman.context.ContextUser;
import sample.games.hangman.wrapper.Settings;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class SettingsHandler implements IHandler {

	private ContextUser user;
    private ContextSettings contextSettings;
	

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Settings settings = (Settings)wrapper;
        contextSettings.setDifficultyLevel(DifficultyLevel.valueOf(settings.getLevel()));
        contextSettings.setMemorizeCharacters(settings.getMemory());
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return contextSettings.getDifficultyLevel() == null;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return user.getName() != null;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
       Settings settings = (Settings)wrapper;
       if(contextSettings.getDifficultyLevel() != null) settings.setLevel(contextSettings.getDifficultyLevel().name());
       settings.setMemory(contextSettings.getMemorizeCharacters());
    }

    @Autowired
    public void setContextSettings(ContextSettings contextSettings) {
    	this.contextSettings = contextSettings;
    }
    
    @Autowired
    public void setUser(ContextUser user) {
        this.user = user;
    }

}
