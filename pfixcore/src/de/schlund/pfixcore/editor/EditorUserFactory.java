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

package de.schlund.pfixcore.editor;
import de.schlund.pfixcore.util.*;
import de.schlund.util.FactoryInit;
import de.schlund.pfixxml.*;
import java.util.*;
import org.apache.log4j.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import java.io.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 * EditorUserFactory.java
 *
 *
 * Created: Fri Nov 23 23:08:28 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditorUserFactory implements FactoryInit {
    private static DocumentBuilderFactory dbfac         = DocumentBuilderFactory.newInstance();
    private static TreeMap                knownusers    = new TreeMap();
    private static TreeMap                floatingusers = new TreeMap();
    private static Category               LOG           = Category.getInstance(EditorUserFactory.class.getName());
    private static EditorUserFactory      instance      = new EditorUserFactory();

    private static final String PROP_UF  = "editoruserfactory.userdata";
    public               Object LOCK     = new Object();
    private              String userfile;
    
    public static EditorUserFactory getInstance() {
        return instance;
    }

    public EditorUser createEditorUser(String id) {
        EditorUser user = getEditorUser(id);
        if (user == null) {
            synchronized (floatingusers) {
                user = new EditorUser(id);
                floatingusers.put(id, user);
            }
        }
        return user;
    }
    
    public void init(Properties properties) throws Exception {
        userfile = properties.getProperty(PROP_UF);
        if (userfile == null) {
            throw new XMLException("Need property " + PROP_UF + " set!");
        }
        synchronized (knownusers) {
            readFile();
        }
    }

    public EditorUser getEditorUser(String id) {
        synchronized (knownusers) {
            return (EditorUser) knownusers.get(id); 
        }
    }
        
    public EditorUser[] getAllEditorUsers() {
        synchronized (knownusers) {
            return (EditorUser[]) knownusers.values().toArray(new EditorUser[] {});
        }
    }

    public void addEditorUser(EditorUser eu) throws Exception {
        String id = eu.getId();
        synchronized (knownusers) {
            knownusers.put(id, eu);
            writeFile();
        }
        synchronized (floatingusers) {
            if (floatingusers.get(id) != null) {
                floatingusers.remove(id);
            }
        }
    }

    public void delEditorUser(EditorUser[] eu) throws Exception {
        synchronized (knownusers) {
            for (int i = 0; i < eu.length; i++) {
                String id = eu[i].getId();
                knownusers.remove(id);
                synchronized (floatingusers) {
                    floatingusers.put(id, eu[i]); 
                }
            }
            writeFile();
        }
    }
    
    private void readFile() throws Exception {
        DocumentBuilder domp = dbfac.newDocumentBuilder();
        Document        doc;
        synchronized (LOCK) {
            File ufile = new File(userfile);
            if (ufile.exists() && ufile.isFile() && ufile.canRead()) {
                doc = domp.parse(userfile);
            } else {
                throw new XMLException("Userfile " + userfile + " doesn't exist, can't be read or is no ordinary file");
            }
        }

        NodeList allusers = doc.getElementsByTagName("user");
        
        for (int i = 0; i < allusers.getLength(); i++) {
            Element    user = (Element) allusers.item(i); 
            String     id   = user.getAttribute("id"); 
            EditorUser eu   = new EditorUser(id);
            eu.setName(user.getAttribute("name"));
            eu.setSect(user.getAttribute("sect"));
            eu.setPhone(user.getAttribute("phone"));
            eu.setPwd(user.getAttribute("pwd"));
            eu.setGroup(user.getAttribute("group"));
            LOG.debug(">>> Found user " + eu.getName() + " (" + eu.getId() + ") <<<<");
            knownusers.put(id, eu);
        }
    }

    public synchronized void writeFile() throws Exception {
        Document doc  = dbfac.newDocumentBuilder().newDocument();
        Element  root = doc.createElement("userinfo");
        doc.appendChild(root);
        for (Iterator i = knownusers.values().iterator(); i.hasNext(); ) {
            EditorUser eu   = (EditorUser) i.next();
            Element    user = doc.createElement("user");
            root.appendChild(user);
            user.setAttribute("id", eu.getId());
            user.setAttribute("pwd", eu.getPwd());
            user.setAttribute("name", eu.getName());
            user.setAttribute("sect", eu.getSect());
            user.setAttribute("group", eu.getGroup());
            user.setAttribute("phone", eu.getPhone());
        }
            
        FileWriter    output = new FileWriter(userfile);
        OutputFormat  outfor = new OutputFormat("xml","ISO-8859-1",true);
        XMLSerializer ser    = new XMLSerializer(output, outfor);
        outfor.setLineWidth(0);
        
        synchronized (LOCK) {
            ser.serialize(doc);
        }
    }

}// EditorUserFactory
