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
package org.pustefixframework.example.numberguess.context;

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