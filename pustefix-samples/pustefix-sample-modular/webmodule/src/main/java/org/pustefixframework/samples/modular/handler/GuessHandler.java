package org.pustefixframework.samples.modular.handler;

import org.pustefixframework.samples.modular.context.GuessContext;
import org.pustefixframework.samples.modular.context.StatisticsContext;
import org.pustefixframework.samples.modular.registration.User;
import org.pustefixframework.samples.modular.service.RandomNumberGenerator;
import org.pustefixframework.samples.modular.webmodule.StatusCodes;
import org.pustefixframework.samples.modular.wrapper.Guess;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class GuessHandler implements IHandler {

    private RandomNumberGenerator generator;
    private StatisticsContext statistics;
    private GuessContext guessContext;
    private User user;
    
    
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
        return user.getName() != null;
    }
    
    public boolean needsData(Context context) throws Exception {
        return guessContext.getGuess() == null;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        Guess guess = (Guess)wrapper;
        guess.setStringValNumber("0");
    }
    
    @Autowired
    public void setRandomNumberGenerator(RandomNumberGenerator generator) {
        this.generator = generator;
    }
    
    @Autowired
    public void setStatisticsContext(StatisticsContext statistics) {
        this.statistics = statistics;
    }
    
    @Autowired
    public void setGuessContext(GuessContext guessContext) {
        this.guessContext = guessContext;
    }
    
    @Autowired
    public void setUser(User user) {
        this.user = user;
    }
    
}
