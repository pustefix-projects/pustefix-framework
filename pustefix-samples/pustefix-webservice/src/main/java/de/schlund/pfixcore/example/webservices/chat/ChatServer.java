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

import java.util.Calendar;
import java.util.Iterator;
import java.util.WeakHashMap;


/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChatServer {
    
    private static ChatServer instance=new ChatServer();
    
    private WeakHashMap<String,ContextChat> ctxMap;
    
    public ChatServer() {
    	ctxMap=new WeakHashMap<String,ContextChat>();
    }
    
    public static ChatServer getInstance() {
    	return instance;
    }
    
    public synchronized void register(String nickName,ContextChat ctx) throws Exception {
    	if(ctxMap.containsKey(nickName)) throw new Exception("Nickname already in use");
        ctxMap.put(nickName,ctx);
        publish("Admin",nickName+" joined the chat.",null);
    }
    
    public synchronized void deregister(String nickName) throws Exception {
    	if(!ctxMap.containsKey(nickName)) throw new Exception("Nickname is not registered");
        ctxMap.remove(nickName);
        publish("Admin",nickName+" has left the chat.",null);
    }
    
    private void publish(Message msg,String[] to) {
        if(to==null || to.length==0) {
        	Iterator<ContextChat> it=ctxMap.values().iterator();
            while(it.hasNext()) {
            	ContextChat cc=it.next();
                cc.addMessage(msg);
            }
        } else {
        	for(int i=0;i<to.length;i++) {
        		ContextChat cc=(ContextChat)ctxMap.get(to[i]);
                if(cc!=null) cc.addMessage(msg);
            }
        }
    }
    
    public synchronized void publish(String from,String txt,String[] to) {
        Calendar cal=Calendar.getInstance();
    	Message msg=new Message(from,txt,cal);
        publish(msg,to);
    }
    
    public synchronized String[] getRegisteredNames() {
        String[] names=new String[ctxMap.size()];
    	ctxMap.keySet().toArray(names);
        return names;
    }

}
