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
package de.schlund.pfixcore.example.webservices.chat;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InitResource;
import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

/**
 * @author ml
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ContextChatImpl implements ContextChat {

    private boolean            loggedIn;
    private String             nickName;
    private ArrayList<Message> messageCache;
    private ArrayList<Message> lastMessages;
    private int                maxCache = 100;

    @InitResource
    public void initialize() {
        reset();
    }

    public void reset() {
        loggedIn = false;
        messageCache = new ArrayList<Message>();
        lastMessages = new ArrayList<Message>();
        nickName = null;
    }

    public boolean needsData() {
        return false;
    }

    @InsertStatus
    public void serialize(ResultDocument resdoc, Element node) throws Exception {
        node.setAttribute("loggedin", Boolean.toString(loggedIn));
        if (loggedIn) node.setAttribute("nickname", nickName);
    }

    public synchronized void login(String nickName) throws Exception {
        if (loggedIn) throw new Exception("You're already logged in");
        ChatServer.getInstance().register(nickName, this);
        this.nickName = nickName;
        loggedIn = true;
    }

    public synchronized void logout() throws Exception {
        if (!loggedIn) throw new Exception("You're already logged out");
        ChatServer.getInstance().deregister(nickName);
        loggedIn = false;
    }

    public synchronized void sendMessage(String text, String[] recipients) throws Exception {
        if (!loggedIn) throw new Exception("You're not logged in");
        ChatServer.getInstance().publish(nickName, text, recipients);
    }

    public synchronized void addMessage(Message msg) {
        if (messageCache.size() == maxCache) messageCache.remove(0);
        messageCache.add(msg);
        lastMessages.add(msg);
    }

    public synchronized Message[] getMessages() {
        Message[] msgs = new Message[messageCache.size()];
        messageCache.toArray(msgs);
        return msgs;
    }

    public synchronized Message[] getLastMessages() {
        Message[] msgs = new Message[lastMessages.size()];
        lastMessages.toArray(msgs);
        lastMessages.clear();
        return msgs;
    }

    public synchronized String[] getNickNames() {
        return ChatServer.getInstance().getRegisteredNames();
    }

}
