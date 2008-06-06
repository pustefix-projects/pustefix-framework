/*
 * de.schlund.pfixcore.example.webservices.CounterImpl
 */
package de.schlund.pfixcore.example.webservices;

import de.schlund.pfixcore.example.ContextCounter;
import de.schlund.pfixcore.webservice.AbstractService;

/**
 * CounterImpl.java 
 * 
 * Created: 29.06.2004
 * 
 * @author mleidig
 */
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
