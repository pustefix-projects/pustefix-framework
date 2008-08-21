package org.pustefixframework.util.firedebug;

/**
 * In order to send JAVA objects over HTTP and display them in Firefox you
 * have to serialize those objects to JSON.
 * 
 * @author Holger Rüprich
 */

public interface JSONSerializer {
    
    String javaToJson(Object object);

}
