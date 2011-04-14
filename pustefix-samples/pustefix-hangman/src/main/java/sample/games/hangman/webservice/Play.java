package sample.games.hangman.webservice;

import sample.games.hangman.context.ContextPlay;

public interface Play {

    public ContextPlay guess(char ch);
    
}
