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

import java.io.*;
import java.net.*;

/**
 * CommandClient.java 
 * 
 * Created: 29.04.2003
 * 
 * @author mleidig
 */
public class CommandClient {

    String DEFAULT_HOST="localhost";
    int DEFAULT_PORT=8888;
    String host;
    int port;

    CommandClient() {
        host=DEFAULT_HOST;
        port=DEFAULT_PORT;
    }

    CommandClient(String host,int port) {
        this.host=host;
	    this.port=port;
    }

    public void sendCommand(String cmd) {
        Socket socket=null;
        PrintWriter out=null;
        BufferedReader in=null;
        try {
            socket=new Socket(host,port);
            out=new PrintWriter(socket.getOutputStream(),true);
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        out.println(cmd);
	        String line;
            while((line=in.readLine())!=null) {
                if(line.trim().equals(".")) break;
	           System.out.println(line);
            }
	        out.close();
	        in.close();
	        socket.close();
        } catch (UnknownHostException e) {
            System.err.println("ERROR: Host '"+host+"' is unknown.");
            System.err.println(e);
        } catch (IOException e) {
            System.err.println("ERROR: Can't get IO for '"+host+":"+port+"'.");
            System.err.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String script=System.getProperty("apploader.script");
        if(script==null || script.trim().equals("")) script="CommandClient";
        String usage="Usage: "+script+" [host:port] command [argument]";
	    if(args.length>0) {
            if(args[0].indexOf(':')>0) {
                if(args.length<2 || args.length>3) {
                    System.err.println(usage);
                } else {
                    String adr=args[0];
		            String cmd=args[1];
		            if(args.length==3) cmd+=" "+args[2];
		            try {
                        int ind=adr.indexOf(':');
			            String hostname=adr.substring(0,ind);
			            int portno=Integer.parseInt(adr.substring(ind+1));
			            CommandClient cc=new CommandClient(hostname,portno);
			            cc.sendCommand(cmd);
                    } catch(NumberFormatException x) {
                        System.err.println(usage);
                    }
                }
            } else {
                if(args.length<1 || args.length>2) {
                    System.err.println(usage);
                } else {
		            String cmd=args[0];
		        if(args.length==2) cmd+=" "+args[1];
                    CommandClient cc=new CommandClient();
		            cc.sendCommand(cmd);
                }
            }
        } else {
	       System.err.println(usage);   
        }
    }
}
