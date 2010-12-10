package sample.games.hangman.context;

import sample.games.hangman.DifficultyLevel;

public class ContextSettings {

    private DifficultyLevel level;
    private boolean memorize;
    
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
    
}
