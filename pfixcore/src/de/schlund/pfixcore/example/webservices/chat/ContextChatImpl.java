/*
 * Created on 09.10.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example.webservices.chat;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ContextChatImpl implements ContextResource,ContextChat {

    private boolean loggedIn;
    private String nickName;
    private ArrayList<Message> messageCache;
    private ArrayList<Message> lastMessages;
    private int maxCache=100;
    
    public void init(Context context) {
    	reset();
     }
   
    public void reset() {
        loggedIn=false;
        messageCache=new ArrayList<Message>();
        lastMessages=new ArrayList<Message>();
        nickName=null;
    }

    public boolean needsData() {
        return false;
    }

    public void insertStatus(ResultDocument resdoc,Element node) throws Exception {
        node.setAttribute("loggedin",Boolean.toString(loggedIn));
        if(loggedIn) node.setAttribute("nickname",nickName);
    }
    
    public synchronized void login(String nickName) throws Exception {
        if(loggedIn) throw new Exception("You're already logged in");
        ChatServer.getInstance().register(nickName,this);
        this.nickName=nickName;
        loggedIn=true;
    }
    
    public synchronized void logout() throws Exception {
        if(!loggedIn) throw new Exception("You're already logged out");
        ChatServer.getInstance().deregister(nickName);
        loggedIn=false;
    }
    
    public synchronized void sendMessage(String text,String[] recipients) throws Exception {
        if(!loggedIn) throw new Exception("You're not logged in");
        ChatServer.getInstance().publish(nickName,text,recipients);
    }
    
    public synchronized void addMessage(Message msg) {
    	if(messageCache.size()==maxCache) messageCache.remove(0);
        messageCache.add(msg);
        lastMessages.add(msg);
    }
    
    public synchronized Message[] getMessages() {
        Message[] msgs=new Message[messageCache.size()];
        messageCache.toArray(msgs);
    	return msgs;
    }
    
    public synchronized Message[] getLastMessages() {
    	Message[] msgs=new Message[lastMessages.size()];
        lastMessages.toArray(msgs);
        lastMessages.clear();
        return msgs;
    }
    
    public synchronized String[] getNickNames() {
        return ChatServer.getInstance().getRegisteredNames();
    }
    
}
