package org.pustefixframework.sample.modular.context;

public class GuessContext {

    private int randomNumber;
    private int tries;
    
    public void incrementTries() {
        tries++;
    }
    
    public int getTries() {
        return tries;
    }
    
    public void setRandomNumber(int randomNumber) {
        this.randomNumber = randomNumber;
    }
    
    public int getRandomNumber() {
        return randomNumber;
    }
    
}
