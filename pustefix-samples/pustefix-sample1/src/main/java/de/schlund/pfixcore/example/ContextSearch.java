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
package de.schlund.pfixcore.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;

public class ContextSearch {
    
    private String searchTerm;
    
    public void setSearchTerm(String searchTerm) {
        
        this.searchTerm = searchTerm;
    }
    
    @InsertStatus
    public void getResult(Element root) {

        if(searchTerm != null && !searchTerm.trim().equals("")) {
            //TODO: find better example, e.g. search wikipedia using REST API
            BufferedReader in = null;
            try {
                URL url = new URL("https://en.wikipedia.org");
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(5000);
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if(inputLine.contains(searchTerm)) {
                        Element elem = root.getOwnerDocument().createElement("match");
                        elem.setTextContent(inputLine);
                        root.appendChild(elem);
                    }
                }
                in.close();
            } catch(Exception x) {
                x.printStackTrace();
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch(IOException x) {
                        //ignore exception while closing
                    }
                }
            }
        }
    }

}
