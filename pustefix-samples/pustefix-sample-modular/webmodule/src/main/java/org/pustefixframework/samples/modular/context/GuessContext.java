package org.pustefixframework.samples.modular.context;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

public class GuessContext {
    
    private Integer guess;
    private Integer random;
    
    public Integer getGuess() {
        return guess;
    }
    
    public void setGuess(Integer guess) {
        this.guess = guess;
    }
    
    public int getRandom() {
        return random;
    }
    
    public void setRandom(Integer random) {
        this.random = random;
    }
    
    @InsertStatus
    public void insertStatus(ResultDocument document, Element element) throws Exception {
        if(guess != null) element.setAttribute("guess", String.valueOf(guess));
        if(random != null) element.setAttribute("random", String.valueOf(random));
    }
    
}
