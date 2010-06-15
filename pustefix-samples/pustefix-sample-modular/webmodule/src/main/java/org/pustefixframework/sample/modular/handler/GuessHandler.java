package org.pustefixframework.sample.modular.handler;

import org.pustefixframework.sample.modular.context.GuessContext;
import org.pustefixframework.sample.modular.context.StatisticsContext;
import org.pustefixframework.sample.modular.webmodule.StatusCodes;
import org.pustefixframework.sample.modular.wrapper.Guess;
import org.pustefixframework.samples.modular.service.RandomNumberGenerator;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class GuessHandler implements IHandler {

    private RandomNumberGenerator generator;
    private StatisticsContext statistics;
    private GuessContext guessContext;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        
        Guess guess = (Guess)wrapper;
        
        int number = guess.getNumber();
        int randomNumber = generator.generate(0, 9);
        
        guessContext.setGuess(number);
        guessContext.setRandom(randomNumber);
        
        statistics.incTries();
        
        if(number == randomNumber) {
                context.addPageMessage(StatusCodes.SUCCESS, null, null);
                statistics.incSuccess();
        } else {
                guess.addSCodeNumber(StatusCodes.FAILURE, new String[] {String.valueOf(randomNumber)}, null);
        }
        
    }
    
    public boolean isActive(Context context) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean needsData(Context context) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // TODO Auto-generated method stub
        
    }
    
    public void setRandomNumberGenerator(RandomNumberGenerator generator) {
        this.generator = generator;
    }
    
    public void setStatisticsContext(StatisticsContext statistics) {
        this.statistics = statistics;
    }
    
    public void setGuessContext(GuessContext guessContext) {
        this.guessContext = guessContext;
    }
    
}
