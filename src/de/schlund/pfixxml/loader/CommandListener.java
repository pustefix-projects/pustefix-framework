/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.loader;

import java.net.*;
import java.io.*;
import org.apache.log4j.Category;

/**
 * CommandListener.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class CommandListener implements Runnable {

    private Category CAT=Category.getInstance(getClass().getName());

    int DEFAULT_PORT=8888;
    int port;
    boolean interrupted;
    CommandProcessor cmdProc=new CommandProcessor();
    Thread thread;

    CommandListener() {
    	port=DEFAULT_PORT;
    }

    CommandListener(int port) {
	   this.port=port;
    }
    
    public void start() {
        thread=new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            ServerSocket serverSocket=new ServerSocket(port);
            CAT.info("Listen on port '"+port+"' for new commands.");  
            Thread theThread=Thread.currentThread();
            while(thread==theThread) {
                try {
                    Socket clientSocket=serverSocket.accept();
                    PrintWriter out=new PrintWriter(clientSocket.getOutputStream(),true);
                    BufferedReader in=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String inLine=in.readLine();
                    if(inLine!=null) {
                        try {
                            CAT.info("Process command '"+inLine+"' from '"+clientSocket.getInetAddress().getHostName()+"'.");
                            String response=cmdProc.process(inLine);
                            if(response==null) {
                                out.println("OK"); 
                            } else {
                                out.println(response);
                            }
                        } catch(IllegalCommandException x) {
                            CAT.error(x);
                            out.println("ERROR ["+x.getMessage()+"]");
                        } 
                    }
                    out.println(".");
                    out.close();
                    in.close();
                    clientSocket.close();
                } catch (IOException e) {
                    CAT.error("Accept failed.",e);
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            CAT.error("Can't listen on port "+port+".",e);
        }
    }
    
    public void stop() {
        thread=null;
    }
    
}
