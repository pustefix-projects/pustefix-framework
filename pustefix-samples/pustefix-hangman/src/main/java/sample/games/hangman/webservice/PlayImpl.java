package sample.games.hangman.webservice;

import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.context.ContextPlay;

public class PlayImpl implements Play {

    private ContextPlay contextPlay;
    
    public ContextPlay guess(String ch) {

        if(!contextPlay.isCompleted()) {
            contextPlay.guess(ch.charAt(0));
        }
        return contextPlay;
    }
    
    @Autowired
    public void setContextPlay(ContextPlay contextPlay) {
        this.contextPlay = contextPlay;
    }
    
}
