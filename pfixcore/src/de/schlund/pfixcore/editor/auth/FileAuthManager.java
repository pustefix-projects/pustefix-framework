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

package de.schlund.pfixcore.editor.auth;



import de.schlund.pfixcore.util.UnixCrypt;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implementation of the <code>AuthManager</code> interface. Uses
 * a simple XML file for storing user information.<br/>
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a> 
 */

public class FileAuthManager implements AuthManager {
    private static TreeMap         knownusers    = new TreeMap();
    private static TreeMap         floatingusers = new TreeMap();
    private static Category        CAT           = Category.getInstance(FileAuthManager.class.getName());
    private static FileAuthManager instance      = new FileAuthManager();
    public Object                  LOCK          = new Object();
    private Path                   userfile;

    public static FileAuthManager getInstance() {
        return instance;
    }

    /**
     * @see de.schlund.pfixcore.editor.auth.AuthManager#commit()
     */
    public void commit() throws AuthManagerException {
        try {
            writeFile();
        } catch (IOException e) {
            throw new AuthManagerException("Caught IOException. ", e);
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.auth.AuthManager#getEditorUserById(java.lang.String)
     */
    public EditorUserInfo getEditorUserById(String id) throws AuthManagerException {
        return getEditorUser(id);
    }

    /**
         * @see de.schlund.pfixcore.editor.auth.AuthManager#login(java.lang.String, de.schlund.pfixcore.editor.auth.EditorUserInfo)
         */
    public void login(String enteredPasswd, EditorUserInfo info) throws WrongPasswordException {
        if (info == null)
            throw new IllegalArgumentException("Null is not allowed here");
        
        if (!UnixCrypt.matches(info.getPwd(), enteredPasswd)) {
            if (CAT.isDebugEnabled())
                CAT.debug("Paswords do not match. Throwing WrongPasswordException.");
            throw new WrongPasswordException("Passwords do not match!");
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.auth.AuthManager#getEditorUserInfoById(java.lang.String)
     */
    public EditorUserInfo getEditorUserInfoById(String id) throws NoSuchUserException {
        if (CAT.isDebugEnabled())
            CAT.debug("Retrieving user information for user '" + id + "'.");
        EditorUserInfo info = getEditorUser(id);
        if (info == null) {
            if (CAT.isDebugEnabled())
                CAT.debug("User '" + id + "' not found. Throwing NoSuchUserException.");
            throw new NoSuchUserException("User does not exist");
        }
        return info;
    }

    /**
     * @see de.schlund.pfixcore.editor.auth.AuthManager#getAllEditorUserInfo()
     */
    public EditorUserInfo[] getAllEditorUserInfo() throws AuthManagerException {
        return getAllEditorUsersHelp();
    }

    /**
     * @see de.schlund.pfixcore.editor.auth.AuthManager#addEditorUser(de.schlund.pfixcore.editor.auth.EditorUserInfo)
     */
    public void addEditorUser(EditorUserInfo user) throws AuthManagerException {
        addEditorUserHelp(user);
    }

    /**
     * @see de.schlund.pfixcore.editor.auth.AuthManager#delEditorUser(de.schlund.pfixcore.editor.auth.EditorUserInfo)
     */
    public void delEditorUser(EditorUserInfo user) throws AuthManagerException {
        delEditorUserHelp(user);
    }

    public void setPwdFile(String filename) {
        this.userfile = PathFactory.getInstance().createPath(filename);
    }
    //-------------------------------------------------------------------

    private void addEditorUserHelp(EditorUserInfo eu) throws AuthManagerException {
        String id = eu.getId();
        synchronized (knownusers) {
            if (CAT.isDebugEnabled()) {
                if (knownusers.containsKey(id))
                    CAT.debug("User '" + id + "' exists. Overwriting user.");
                else
                    CAT.debug("User '" + id + "' not exists. Storing user.");
            }
            knownusers.put(id, eu);
            try {
                writeFile();
            } catch (IOException e) {
                throw new AuthManagerException("Caught IOException ", e);
            }
        }
        synchronized (floatingusers) {
            if (floatingusers.get(id) != null) {
                floatingusers.remove(id);
            }
        }
    }

    private EditorUserInfo getEditorUser(String id) {
        synchronized (knownusers) {
            return (EditorUserInfo) knownusers.get(id);
        }
    }

    private EditorUserInfo[] getAllEditorUsersHelp() {
        synchronized (knownusers) {
            return (EditorUserInfo[]) knownusers.values().toArray(new EditorUserInfo[] {
            });
        }
    }

    private void delEditorUserHelp(EditorUserInfo eui) throws AuthManagerException {
        synchronized (knownusers) {
            String id = eui.getId();
            knownusers.remove(id);
            synchronized (floatingusers) {
                floatingusers.put(id, eui);
            }
        }
        try {
            writeFile();
        } catch (IOException e) {
            throw new AuthManagerException("Caught IOException", e);
        }
    }

    public void init() throws AuthManagerException {
        if(CAT.isDebugEnabled())
            CAT.debug(this.getClass().getName()+" init start.");

        try {
            readFile();
        } catch (SAXException e) {
            throw new AuthManagerException("Caught SAXException.", e);
        } catch (IOException e) {
            throw new AuthManagerException("Caught IOException.", e);
        } catch (XMLException e) {
            throw new AuthManagerException("Caught XMLException.", e);
        } catch (TransformerException e) {
            throw new AuthManagerException("Caught TransformerException", e);
        }
        
        if(CAT.isDebugEnabled())
            CAT.debug(this.getClass().getName()+" init end.");

    }

    private void readFile()
        throws SAXException, IOException, XMLException, TransformerException {
        Document doc;
        synchronized (LOCK) {
            File ufile = userfile.resolve();
            if (ufile.exists() && ufile.isFile() && ufile.canRead()) {
                doc = Xml.parseMutable(ufile);
            } else {
                throw new XMLException("Userfile " + userfile + " doesn't exist, can't be read or is no ordinary file");
            }
        }

        NodeList allusers = doc.getElementsByTagName("user");

        for (int i = 0; i < allusers.getLength(); i++) {
            Element user = (Element) allusers.item(i);
            String id = user.getAttribute("id");
            EditorUserInfo info = new EditorUserInfo(id);
            info.setName(user.getAttribute("name"));
            info.setSect(user.getAttribute("sect"));
            info.setPhone(user.getAttribute("phone"));
            info.setPwd(user.getAttribute("pwd"));

            List gl_perms = XPath.select(user, "permissions/global");
            List prj_perms = XPath.select(user, "permissions/project");

            Element gl = (Element) gl_perms.get(0);

            GlobalPermissions globalp = new GlobalPermissions();
            if (gl == null) {
                globalp.setAdmin(false);
                globalp.setEditDynIncludesDefault(false);
            } else {
                boolean b = gl.getAttribute("admin").equals("true");
                globalp.setAdmin(b);

                b = gl.getAttribute("editDefaults").equals("true");
                globalp.setEditDynIncludesDefault(b);

            }
            info.setGlobalPermissions(globalp);

            for (int j = 0; j < prj_perms.size(); j++) {
                Element e = (Element) prj_perms.get(j);
                ProjectPermissions prjp = new ProjectPermissions();
                boolean b = e.getAttribute("editDefaults").equals("true");
                prjp.setEditDynIncludes(b);
                b = e.getAttribute("editImages").equals("true");
                prjp.setEditImages(b);
                b = e.getAttribute("editIncludes").equals("true");
                prjp.setEditIncludes(b);
                info.addProjectPermission(e.getAttribute("name"), prjp);
            }

            CAT.debug(">>> Found user " + info.getName() + " (" + info.getId() + ") <<<<");
            knownusers.put(id, info);
        }
    }

    private synchronized void writeFile() throws IOException {
        Document doc = Xml.createDocument();
        Element root = doc.createElement("userinfo");
        doc.appendChild(root);
        for (Iterator i = knownusers.values().iterator(); i.hasNext();) {
            EditorUserInfo info = (EditorUserInfo) i.next();
            Element user = doc.createElement("user");
            root.appendChild(user);
            user.setAttribute("id", info.getId());
            user.setAttribute("pwd", info.getPwd());
            user.setAttribute("name", info.getName());
            user.setAttribute("sect", info.getSect());
            user.setAttribute("phone", info.getPhone());

            Element perms = doc.createElement("permissions");
            Element globalp = doc.createElement("global");
            GlobalPermissions glp = info.getGlobalPerms();
            globalp.setAttribute("admin", glp == null ? ("" + false) : ("" + glp.isAdmin()));
            globalp.setAttribute("editDefaults", glp == null ? ("" + false) : ("" + glp.isEditDynIncludesDefault()));
            perms.appendChild(globalp);

            if (!info.isAdmin()) {
                // for admin we do not need a permission node

                HashMap projectp = info.getAllProjectPerms();
                if (projectp != null && !projectp.isEmpty()) {
                    Iterator iter = projectp.keySet().iterator();
                    while (iter.hasNext()) {
                        String name = (String) iter.next();
                        Element e = doc.createElement("project");
                        e.setAttribute("name", name);
                        ProjectPermissions prjp = info.getProjectPerms(name);
                        e.setAttribute("editImages", ("" + prjp.isEditImages()));
                        e.setAttribute("editIncludes", ("" + prjp.isEditIncludes()));
                        e.setAttribute("editDefaults", ("" + prjp.isEditDynIncludes()));
                        perms.appendChild(e);
                    }
                }
            }
            user.appendChild(perms);
        }

        synchronized (LOCK) {
            Xml.serialize(doc, userfile.resolve(), true, true);
        }
    }

} // EditorUserFactory
