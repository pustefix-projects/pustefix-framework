/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.example.numberguess.handler;


import org.pustefixframework.example.numberguess.StatusCodes;
import org.pustefixframework.example.numberguess.context.GuessContext;
import org.pustefixframework.example.numberguess.wrapper.GuessDataWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

public class GuessDataHandler implements IHandler {

    private GuessContext guessContext;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
     
        GuessDataWrapper guessData = (GuessDataWrapper)wrapper;
        
        int number = guessData.getNumber();
        
        if(number == 1) context.addPageMessage(StatusCodes.SUCCESS, null, null);
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return true;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
      
        GuessDataWrapper guessData = (GuessDataWrapper)wrapper;
        guessData.setStringValNumber("Number between 0 and 9");
        
    }
    
    @Autowired
    public void setGuessContext(GuessContext guessContext) {
        this.guessContext = guessContext;
    }
    
}
