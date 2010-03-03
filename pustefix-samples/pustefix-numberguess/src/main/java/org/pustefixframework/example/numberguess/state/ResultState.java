package org.pustefixframework.example.numberguess.state;

import org.pustefixframework.example.numberguess.context.StatisticsContext;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.StaticState;
import de.schlund.pfixxml.PfixServletRequest;

public class ResultState extends StaticState {

    private StatisticsContext guessContext;
  
    @Override
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return guessContext.getTries() > 0;
    }
 
    @Autowired
    public void setGuessContext(StatisticsContext guessContext) {
        this.guessContext = guessContext;
    }
    
}
