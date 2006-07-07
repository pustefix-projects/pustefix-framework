/*
 * Created on 09.10.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example.webservices.chat;

import java.util.Calendar;
import java.util.Date;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Message {
    
    private String from;
    private String text;
    private Calendar date;
    
    public Message() {   
    }
    
    public Message(String from,String text,Calendar date) {
    	this.from=from;
        this.text=text;
        this.date=date;
    }
    
    public String getFrom() {
    	return from;
    }
    
    public String getText() {
    	return text;
    }
    
    public Calendar getDate() {
    	return date;
    }

}
