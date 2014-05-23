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
package de.schlund.pfixcore.example.webservices;

import javax.jws.WebService;

import de.schlund.pfixcore.example.ContextCounter;
import org.pustefixframework.webservices.AbstractService;

/**
 * CounterImpl.java 
 * 
 * Created: 29.06.2004
 * 
 * @author mleidig
 */
@WebService
public class CounterImpl extends AbstractService implements Counter {

    public int getValue() throws Exception {
        ContextCounter counter=(ContextCounter)getContextResourceManager().getResource(ContextCounter.class.getName());
        return counter.getCounter();
    }
    
    public boolean setValue(int value) throws Exception {
        ContextCounter counter=(ContextCounter)getContextResourceManager().getResource(ContextCounter.class.getName());
        counter.setCounter(value);
        return true;
    }
    
    public int addValue(int value) throws Exception {
        ContextCounter counter=(ContextCounter)getContextResourceManager().getResource(ContextCounter.class.getName());
        counter.addToCounter(value);
        return counter.getCounter();
    }
    
    public int subtractValue(int value) throws Exception {
        ContextCounter counter=(ContextCounter)getContextResourceManager().getResource(ContextCounter.class.getName());
        counter.setCounter(counter.getCounter()-value);
        return counter.getCounter();
    }
    
    public boolean reset() throws Exception {
        ContextCounter counter=(ContextCounter)getContextResourceManager().getResource(ContextCounter.class.getName());
        counter.setCounter(0);
        return true;
    }
    
}
