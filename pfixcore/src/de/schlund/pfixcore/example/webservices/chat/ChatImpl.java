/*
 * Created on 09.10.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example.webservices.chat;

import org.pustefixframework.webservices.AbstractService;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChatImpl extends AbstractService implements Chat {

    public void login(String nickName) throws Exception {
        ContextChat cc=(ContextChat)this.getContextResource(ContextChat.class.getName());
        cc.login(nickName);
    }
    
    public void logout() throws Exception {
        ContextChat cc=(ContextChat)this.getContextResource(ContextChat.class.getName());
        cc.logout();
    }
    
    public void sendMessage(String txt,String[] recipients) throws Exception {
        ContextChat cc=(ContextChat)this.getContextResource(ContextChat.class.getName());
        cc.sendMessage(txt,recipients);
    }
    
    public Message[] getMessages() {
        ContextChat cc=(ContextChat)this.getContextResource(ContextChat.class.getName());
        return cc.getMessages();
    }
    
    public Message[] getLastMessages() {
        ContextChat cc=(ContextChat)this.getContextResource(ContextChat.class.getName());
        return cc.getLastMessages();
    }
    
    public String[] getNickNames() {
        ContextChat cc=(ContextChat)this.getContextResource(ContextChat.class.getName());
        return cc.getNickNames();
    }

}
