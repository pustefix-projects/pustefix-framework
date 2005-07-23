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
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.util.UnixCrypt;
import de.schlund.pfixxml.util.XPath;

public class UserPasswordAuthenticationServiceImpl implements
        UserPasswordAuthenticationService {
    private FileSystemService filesystem;
    private PathResolverService pathresolver;
    private HashMap passwords;

    public UserPasswordAuthenticationServiceImpl(FileSystemService filesystem, PathResolverService pathresolver) {
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.passwords = new HashMap();
    }
    
    public void reloadConfiguration() throws EditorIOException,
            EditorParsingException {
        File configFile = new File(this.pathresolver.resolve("common/conf/userdata.xml"));
        Document xml;
        synchronized (this.filesystem.getLock(configFile)) {
            try {
                xml = this.filesystem.readXMLDocumentFromFile(configFile);
            } catch (FileNotFoundException e) {
                String err = "File " + configFile.getAbsolutePath()
                        + " could not be found!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            } catch (SAXException e) {
                String err = "Error during parsing file "
                        + configFile.getAbsolutePath() + "!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorParsingException(err, e);
            } catch (IOException e) {
                String err = "File " + configFile.getAbsolutePath()
                        + " could not be read!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            } catch (ParserConfigurationException e) {
                String err = "Error during initialization of XML parser!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new RuntimeException(err, e);
            }
        }
        synchronized (this.passwords) {
            this.passwords.clear();
            try {
                for (Iterator i = XPath.select(xml, "/userinfo/user").iterator(); i.hasNext();) {
                    Element userNode = (Element) i.next();
                    String userName = userNode.getAttribute("id");
                    String userPassword = userNode.getAttribute("pwd");
                    if (userName == null) {
                        String err = "<user> has to have id attribute!";
                        Logger.getLogger(this.getClass()).error(err);
                        throw new EditorParsingException(err);
                    }
                    if (userPassword == null) {
                        userPassword = "x";
                    }
                    this.passwords.put(userName, userPassword);
                }
            } catch (TransformerException e) {
                // Should never happen as a DOM document is always well-formed!
                String err = "XPath error!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new RuntimeException(err, e);
            }
        }
        
    }

    public Principal getPrincipalForUser(String username, String password) {
        synchronized (this.passwords) {
            if (!this.passwords.containsKey(username)) {
                return null;
            }
            String storedPassword = (String) this.passwords.get(username);
            if (!UnixCrypt.matches(storedPassword, password)) {
                return null;
            }
        }
        return new PrincipalImpl(username);
    }
    
    private class PrincipalImpl implements Principal {
        private String username;
        
        private PrincipalImpl(String username) {
            this.username = username;
        }
        
        public String getName() {
            return this.username;
        } 
    }
}
