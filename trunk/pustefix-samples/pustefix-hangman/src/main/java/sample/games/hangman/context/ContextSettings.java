package sample.games.hangman.context;

import java.util.Locale;

import sample.games.hangman.DifficultyLevel;

public class ContextSettings {

    private DifficultyLevel level;
    private boolean memorize;
    private Locale locale;
    
    public void setDifficultyLevel(DifficultyLevel level) {
        this.level = level;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return level;
    }
    
    public void setMemorizeCharacters(boolean memorize) {
        this.memorize = memorize;
    }
    
    public boolean getMemorizeCharacters() {
        return memorize;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
}
