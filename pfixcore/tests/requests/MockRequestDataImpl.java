/*
 * Created on Jul 15, 2005
 *
 */
package ro.schlund.test.pustefix.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.schlund.pfixcore.generator.RequestData;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.SimpleRequestParam;
/**
 * @author Dan Dumitrescu
 *
 * Class usage: RequestData interface implemetation for test cases. Helps register IWrapper parameters
 */
public class MockRequestDataImpl implements RequestData {

    private Map<String, RequestParam[]> dataMap;
    private Map<String, String[]> cmdMap;
    
    /**
     * Class constructor.
     * @param context
     */
    public MockRequestDataImpl(Context context) {
        dataMap = new HashMap<String, RequestParam[]>();
        cmdMap = new HashMap<String, String[]>();
    }
    
    /**
     * Register a command
     * @param name - command name
     * @param cmd - command itself
     * @throws <code>IllegalArgumentException</code> if name is <code>null</code> or an empty string
     */
    public void registerCommand(String name, String cmd) throws IllegalArgumentException {
        
        if ((name == null) || ("".equals(name))) 
            throw new IllegalArgumentException("Name cannot be null or empty string");
        
        cmdMap.put(name, new String[] {cmd});
    }
    
    /**
     * Register a parameter with a string value. Also the indexed parameters can be registered with this function<br>
     * @param name - the parameter's name
     * @param value -  the parameter's string value
     * @throws <code>IllegalArgumentException</code> if the name is <code>null</code> or emtpy string
     */
    public void registerParam(String name, String value) throws IllegalArgumentException {
        registerParam(name, new Object[] {value});
    }
    
    /**
     * Register a parameter with an array of values. If the parameter's value is not a string <br>
     * then this is the proper function for the parameter registration.<br>
     * Also the indexed parameters can be registered with this function.<br> 
     * @param name - the parameter's name
     * @param values - parameter's value - <code>Object[]</code>
     * @throws <code>IllegalArgumentException</code> if the parameter name is <code>null</code> or empty string
     */
    public void registerParam(String name, Object[] values) throws IllegalArgumentException {
        
        if ((name == null) || ("".equals(name)))
            throw new IllegalArgumentException("Name cannot be null or empty string");
        SimpleRequestParam[] srp = null;
        
        if ((values != null) && (values.length > 0)) {
            srp = new SimpleRequestParam[values.length];
            
            for (int i=0;i<values.length;i++) {
                srp[i] = new SimpleRequestParam((values[i] != null) ? values[i].toString() : null);
            }
        }
        
        dataMap.put(name, srp);
    }
    
    /**
     * Get the values of the paramenter that matches <code>name</code>
     * @return <code>RequestParam[]</code> array 
     */
    public RequestParam[] getParameters(String name) {
        return dataMap.get(name);
    }

    /**
     * Get all the registered parameteres names
     * @return <code>java.util.Iterator</code>
     */
    public Iterator getParameterNames() {
        return dataMap.keySet().iterator();
    }

    /**
     * Get the values of a command that matches <code>cmd</code>
     * @return <code>String[]</code> - array
     */
    public String[] getCommands(String cmd) {
        return cmdMap.get(cmd);
    }

    /**
     * Get all the registered commands names
     * @return <code>java.util.Iterator</code>
     */
    public Iterator getCommandNames() {
        return cmdMap.keySet().iterator();
    }

}
