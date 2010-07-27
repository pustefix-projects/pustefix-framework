package org.pustefixframework.webservices.fault;

/**
 * Interface implemented by configuration objects supporting parameters
 * 
 * @author mleidig@schlund.de
 *
 */
public interface Parameterizable {

    public void addParam(String name, String value);
    public String getParam(String name);
    
}
