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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.EditorHelper;
import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.DependencyType;
/**
 * Class encapsulating user-information of the Pustefix CMS.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
final public class EditorUserInfo {
    private String id;
    private String phone;
    private String name;
    private String sect;
    private String pwd;
    private GlobalPermissions global_perm;
    private HashMap project_perm = new HashMap();
    private static Category CAT = Category.getInstance(EditorUserInfo.class.getName());

    /**
     * Create a new <code>EditorUserInfo</code>.
     * @param id the userid. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public EditorUserInfo(String id) {
        if (id == null)
            throw new IllegalArgumentException("A NP as userid is not allowed here.");
        else
            this.id = id;
    }

    /**
     * Retrieve the userid.
     * @return the userid.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the userid.
     * @param v The userid. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public void setId(String v) {
        if (v == null)
            throw new IllegalArgumentException("A NP as userid is not allowed here.");
        this.id = v;
    }

    /**
     * Retrieve the phone number.
     * @return The phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the phone number.
     * @param phone The phone number. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public void setPhone(String phone) {
        if (phone == null)
            throw new IllegalArgumentException("A NP as phone number is not allowed here.");
        this.phone = phone;
    }

    /**
     * Retrieve the name of the user.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param name. The name.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public void setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("A NP as name is not allowed here.");
        this.name = name;
    }

    /**
     * Retrieve the section of the user.
     * @return The section.
     */
    public String getSect() {
        return sect;
    }

    /**
     * Set the section of the user.
     * @param section. The section. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public void setSect(String section) {
        if (section == null)
            throw new IllegalArgumentException("A NP as section is not allowed here.");
        this.sect = section;
    }

    /**
     * Retrieve the crypted password of the user.
     * @return the password.
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Set the crypted password of the user.
     * @param pass. The crypted password of the user. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public void setPwd(String pass) {
        this.pwd = pass;
    }

    /**
     * Set the global permission of the user.
     * @param gperms the global permissions to be set. Not null.
     * @throws IllegalArgumentException when trying to pass NP as param.
     */
    public void setGlobalPermissions(GlobalPermissions gperms) {
        if (gperms == null)
            throw new IllegalArgumentException("A NP as global permissions is not allowed here.");
        this.global_perm = gperms;
    }

    /**
     * Add project permissions for a project.
     * @param projectname. The name of the project where project permissions should
     * be added.
     * @param pperms. The permissions for the project.
     * @throws IllegalArgumentException when trying to pass NPs as params.
     */
    public void addProjectPermission(String projectname, ProjectPermissions pperms) {
        if (projectname == null)
            throw new IllegalArgumentException("A NP as projectname is not allowed here.");
        if (pperms == null)
            throw new IllegalArgumentException("A NP as project permissions is not allowed here.");

        project_perm.put(projectname, pperms);
    }

    /**
     * Retrieve the global permissions of the user.
     * @return the global permissions. Never null.
     * @throws NoSuchElementException when no global permissions exist.
     */
    public GlobalPermissions getGlobalPerms() {
        if (global_perm == null)
            throw new NoSuchElementException("No global-permissions found.");
        return global_perm;
    }

    /**
     * Retrieve all project permissions.
     * @return a HashMap with the project name as key and the project permission as value.
     * @throws NoSuchElementException when no project permissions exist.
     */
    public HashMap getAllProjectPerms() {
        if (project_perm == null)
            throw new NoSuchElementException("No project-permission found.");
        return project_perm;
    }

    /**
     * Retrieve the project permission for a project.
     * @param name. The name of the project. Not null.
     * @return the permissions for the project.
     * @throws IllegalArgumentException when trying to pass NP as param. Never null.
     */
    public ProjectPermissions getProjectPerms(String name) {
        if (name == null)
            throw new IllegalArgumentException("NULL is not a suitable projectname!");

        ProjectPermissions p = null;
        if (isAdmin()) {
            p = new ProjectPermissions();
            p.setEditDynIncludes(true);
            p.setEditImages(true);
            p.setEditIncludes(true);
        } else {
            p = (ProjectPermissions) project_perm.get(name);
            if (p == null) {
                // No project permissions found? - Create a new one with default entries
                p = new ProjectPermissions();
                p.setEditDynIncludes(false);
                p.setEditImages(false);
                p.setEditIncludes(false);
                project_perm.put(name, p);
            }
        }
        return p;
    }

    /**
     * Determine if the user is admin.
     * @return true if admin, else false.
     */
    public boolean isAdmin() {
        return global_perm != null && global_perm.isAdmin();
    }

    /**
     * Check if user is allowed to edit the given image.
     * 
     * @param path_to_image the path to the image
     * @return true if edit allowed, else false
     */
    public boolean isImageEditAllowed(String path_to_image) throws XMLException {
      
        HashSet affected_products = null;
        boolean ret = true;
        try {
            affected_products = EditorHelper.getAffectedProductsForImage(path_to_image);
        } catch (Exception e) {
            throw new XMLException("Error when getting affected products for include! " + e.getMessage());
        }

        for (Iterator iter = affected_products.iterator(); iter.hasNext();) {
            String name = ((EditorProduct) iter.next()).getName();
            ProjectPermissions p = getProjectPerms(name);
            if (!p.isEditImages()) {
                ret = false;
            }
        }
      
        return ret;
    }

    /**
     * Check if user is allowed to edit dyn include for given product
     * 
     * @param product the current product
     * @return true if allowed, else false 
     */
    public boolean isDynIncludeEditAllowed(String editor_product, String incl_product) {
      
        boolean ret = true;
        GlobalPermissions gp = getGlobalPerms();
        ProjectPermissions pp = getProjectPerms(editor_product);
        if (gp.isEditDynIncludesDefault()) {
            if (pp.isEditDynIncludes()) {
            } else {
                if (incl_product.equals("default")) {
                } else {
                    //throw new XMLException("PermissionDenied! You are trying to edit a specific branch, but"+
                    //    " you do not have the proper permissions!\n"+toString());
                    ret = false;
                }
            }
        } else {
            if (pp.isEditDynIncludes()) {
                if (incl_product.equals("default")) {
                    //throw new XMLException("Permission denied! You are trying to edit the default branch, but"+
                    //   " you do not have the proper permissions!\n"+toString());
                    ret = false;
                } else {
                }
            } else {
                //throw new XMLException("PermissionDenied! You are trying to edit a specific branch, but"+
                //    " you do not have the proper permissions!\n"+toString());
                ret = false;
            }
        }
      
        return ret;
    }

    public boolean isIncludeEditAllowed(EditorSessionStatus esess, HashSet affected_products) throws XMLException {
       
        boolean ret = true;
      
        // <comment>
        // Here we must handle the case that an editoruser references a
        // new include. When he selects it from the list, it is
        // not written yet. So we must NOT call EditorHelper.getAffectedProductsForInclude!!!
        String part = esess.getCurrentInclude().getPart();
        String path = esess.getCurrentInclude().getPath();
        Element ele = null;
        
        // Test if part already exists
        try {
            ele = EditorHelper.getIncludePart(esess.getProduct().getTargetGenerator(), 
                                            AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT,
                                                    path, part,esess.getProduct().getName()));
        } catch (Exception e) {
            throw new XMLException("Error when getting include part" + e.getMessage());
        }
        
        // if ele==null a new include has been referenced, but was not written yet
        if(ele == null) {
            // Part does not exist
            ProjectPermissions p = esess.getUser().getUserInfo().getProjectPerms(esess.getProduct().getName());
            boolean allowed = p.isEditIncludes();
            return allowed;
        }
        //</comment>
        
        /*try {
            affected_products =
                EditorHelper.getAffectedProductsForInclude(
                    esess,
                    esess.getCurrentInclude().getPath(),
                    esess.getCurrentInclude().getPart());
        } catch (Exception e) {
            throw new XMLException("Error when getting affected products for include! " + e.getMessage());
        }*/

        for (Iterator iter = affected_products.iterator(); iter.hasNext();) {
            String name = ((EditorProduct) iter.next()).getName();
            ProjectPermissions p = esess.getUser().getUserInfo().getProjectPerms(name);
            if (!p.isEditIncludes()) {
                ret =  false;
            }
        }
      
        return ret;
    }

    /**
     * Retrieve String representation.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nId  = " + id).append("\n");
        sb.append("Global :\n" + global_perm.toString()).append("\n");
        Iterator iter = project_perm.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            ProjectPermissions p = (ProjectPermissions) project_perm.get(key);
            sb.append("For project \n" + key + " " + p.toString());
        }
        return sb.toString();
    }
}
