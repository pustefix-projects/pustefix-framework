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

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.auth.AuthManager;
import de.schlund.pfixcore.editor.auth.AuthManagerException;
import de.schlund.pfixcore.editor.auth.AuthManagerFactory;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.auth.ProjectPermissions;
import de.schlund.pfixcore.editor.auth.NoSuchUserException;
import de.schlund.pfixcore.editor.auth.WrongPasswordException;
import de.schlund.pfixxml.ResultDocument;

/**
 * This class uses the singleton pattern and returns instances
 * of <code>EditorUser</code> only after succesfull authorization.
 * All work on non-authorized users has to be done on <code>
 * EditorUserInfo</code>. Additionally it provides other
 * static helper methods. 
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */

final public class EditorUser {
    
    private EditorUserInfo userinfo;
   // private static HashMap registry = new HashMap();
   
    
    private static Category CAT = Category.getInstance(EditorUser.class.getName());
    
    private EditorUser(EditorUserInfo info) {
        this.userinfo = info;
    }

    /** 
     * Get an instance of EditorUser. This instance will only
     * be returned after a successfull login.
     * @param login The id of the user. Not null.
     * @param passwd The password entered by the user in plain text.
     * (all crypto stuff will be handled inside this class). Not null.
     * @return an instance of <code>EditorUser</code>after successfull
     * login, otherwise null. 
     * @throws IllegalArgumentException when trying to pass NPs as params.
     */
    public static synchronized EditorUser logIn(String login, String passwd) throws WrongPasswordException, EditorException, NoSuchUserException, AuthManagerException {
        if(login == null)
            throw new IllegalArgumentException("A NP as login name is not allowed here.");
     
        if(passwd == null)
            throw new IllegalArgumentException("A NP as entered password is not allowed here.");
        
    
        if(CAT.isDebugEnabled()) 
            CAT.debug("Retrieving user information for user '"+login+"'.");
        
            
        EditorUserInfo info = getUserInfoByLogin(login);
       
        AuthManager auth = AuthManagerFactory.getInstance().getAuthManager();
        
        
        if(CAT.isDebugEnabled())
            CAT.debug("Doing login for user '"+login+"'.");
                
        auth.login(passwd, info);
       
       
        if(CAT.isDebugEnabled())
            CAT.debug("Login for user '"+login+"' successfull. Returing an instance of EditorUser.");
            
        EditorUser user = new EditorUser(info);
        
        return user;
       
    }
    
    
    /**
     * Add a new user.
     * 
     * @param info An <code>EditorUserInfo</code> containing
     * all user information. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public static synchronized void addUser(EditorUserInfo info) throws EditorException, AuthManagerException {
        if(info == null)
            throw new IllegalArgumentException("A NP as EditorUserInfo is not allowed here.");
        
        if(CAT.isDebugEnabled())
            CAT.debug("Trying to add user '"+info.getId()+"'.");
        
        AuthManager auth = AuthManagerFactory.getInstance().getAuthManager();
        try {
            auth.addEditorUser(info);
        } catch (AuthManagerException e) {
            throw new EditorException("Caught AuthManagerException when trying to add user '"+info.getId()+"'.", e);
        }
        if(CAT.isDebugEnabled())
            CAT.debug("User '"+info.getId()+"' added successfully.");
    }
    
    
    /**
     * Remove a user.
     * 
     * @param info An <code>EditorUserInfo</code> containing
     * all user information. Not null.
     * @throws IllegalArgumentExcption when trying to pass NP as param.
     */
    public static synchronized void removeUser(EditorUserInfo info) throws EditorException, AuthManagerException {
        if(info == null)
            throw new IllegalArgumentException("A NP as EditorUserInfo is not allowed here.");
        
        if(CAT.isDebugEnabled())
            CAT.debug("Trying to remove user '"+info.getId()+"'.");
        
        AuthManager auth = AuthManagerFactory.getInstance().getAuthManager();
       
        try {
            auth.delEditorUser(info);
        } catch (AuthManagerException e) {
            throw new EditorException("Caught AuthManagerException when trying to remove user '"+info.getId()+"'.",e);
        }
        if(CAT.isDebugEnabled())
            CAT.debug("User '"+info.getId()+"' removed successfully.");
    }
     
    /**
     * Retrieve all possible EditorUsers.
     * @return an array of <code>EditorUserInfo</code>.
     */
    public static EditorUserInfo[] getAllEditorUserInfo() throws EditorException, AuthManagerException {
        AuthManager auth = AuthManagerFactory.getInstance().getAuthManager();
        EditorUserInfo[] infos = null;
        
        if(CAT.isDebugEnabled())
            CAT.debug("Trying to retrieve all EditorUserInfo.");
                
        try {
            infos = auth.getAllEditorUserInfo();
        } catch (AuthManagerException e) {
            throw new EditorException("Caught AuthManagerException when trying to get all user information.",e);
        }
        
        if(CAT.isDebugEnabled())
            CAT.debug(infos.length+" EditorUserInfos retrieved successfully.");
        
        return infos;
    }

    /**
     * Create a new EditorUserInfo.
     * @param login. The id of the user. Not null.
     * @return the new EditorUser.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
   /* public static EditorUserInfo createEditorUserInfo(String login) {
        if(login == null)
            throw new IllegalArgumentException("A NP as userid is not allowed here.");
        
        EditorUserInfo info = new EditorUserInfo(login);
        return info;
    }*/

    /**
     * Retrieve all information of a user identified
     * by its userid. 
     * 
     * @param login. The id of the user. Not null.
     * @return an <code>EditorUserInfo</code> containing all information.
     * @throws IllegalArgumentException when trying to pass NP as param.   
     */
    public static synchronized EditorUserInfo getUserInfoByLogin(String login) throws EditorException, NoSuchUserException, AuthManagerException {
        if(login == null)
            throw new IllegalArgumentException("A NP as userid is not allowed here.");
        
        if(CAT.isDebugEnabled())
            CAT.debug("Trying to retrieve EditorUserInfo by id '"+login+"'.");
        
        AuthManager auth = AuthManagerFactory.getInstance().getAuthManager();
        EditorUserInfo info = null;
       
        info = auth.getEditorUserInfoById(login);
       
        if(CAT.isDebugEnabled())
            CAT.debug("Retrievement of EditorUserInfo for id '"+login+"' sucessfully.");
        return info;
    }

    /**
     * Retrieve String representation of the <code>EditorUser</code>.
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return userinfo.getId() + ": " + userinfo.getName();
    }

    /**
     * Render the interal data of this class in a XML tree.
     * 
     * @param resdoc The XML document.
     * @param root The root node.
     * @throws IllegalArgumentException when trying to pass NPs as param.
     */
    public void insertStatus(ResultDocument resdoc, Element root) {
        if(resdoc == null)
            throw new IllegalArgumentException("A NP as ResultDocument is not allowed here.");
        if(root == null)
            throw new IllegalArgumentException("A NP as root-node is not allowed here.");
        
        Element user = resdoc.createSubNode(root, "user");
        user.setAttribute("id", userinfo.getId());
        user.setAttribute("name", userinfo.getName());
        user.setAttribute("phone", userinfo.getPhone());
        user.setAttribute("sect", userinfo.getSect());
        
        Element perms = resdoc.createSubNode(user, "permissions");
        Element global = resdoc.createSubNode(perms, "global");
        global.setAttribute("admin", ""+userinfo.getGlobalPerms().isAdmin());
        
        global.setAttribute("editDefaults", ""+userinfo.getGlobalPerms().isEditDynIncludesDefault());
        
        if(userinfo.getGlobalPerms().isAdmin()) 
            return;
                    
        HashMap prp = userinfo.getAllProjectPerms();
        Iterator iter = prp.keySet().iterator();
        while(iter.hasNext()) {
            Element prj = resdoc.createSubNode(perms, "project");
            String key = (String) iter.next(); 
            ProjectPermissions p =(ProjectPermissions) prp.get(key);
            prj.setAttribute("name", key);
            prj.setAttribute("editImages", ""+p.isEditImages());
            prj.setAttribute("editIncludes", ""+p.isEditIncludes());
            prj.setAttribute("editDefaults", ""+p.isEditDynIncludes());
        } 
    }
        
    
  /*  public boolean equals(Object o) {
        if (o instanceof EditorUser && ((EditorUser) o).userinfo.getId().equals(userinfo.getId())) {
            return true;
        } else {
            return false;
        }
    } */
    
    /**
     * Calculate the hash code of an <code>EditorUser</code>
     * by using its userid.
     * @return the hashcode.
     * 
     */    
    public int hashCode() {
        return userinfo.getId().hashCode();
    }
    
    
    /**
     * Retrieve all user information of an <code>EditorUser</code>. 
     * @return an <code>EditorUserInfo</code> containing the information. Never null.
     * @throws IllegalStateException when no user information exists.
     */
    public EditorUserInfo getUserInfo() {
        if(userinfo == null)
            throw new IllegalStateException("EditorUserInfo for user is NULL. This can not be.");
        return userinfo;
    }
    
    /**
     * Retrieve the userid of an <code>EditorUser</code>.
     * @return the userid.
     */
    public String getId() {
        return userinfo.getId();
    }
    
}
