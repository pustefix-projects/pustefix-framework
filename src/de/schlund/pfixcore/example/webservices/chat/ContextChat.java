/*
 * Created on 09.10.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example.webservices.chat;

import de.schlund.pfixcore.workflow.ContextResource;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ContextChat extends ContextResource {
    
    public void login(String nickName) throws Exception;
    public void logout() throws Exception;
    public void sendMessage(String txt,String[] recipients) throws Exception;
    public void addMessage(Message msg);
    public Message[] getMessages();
    public Message[] getLastMessages();
    public String[] getNickNames();
    
}
