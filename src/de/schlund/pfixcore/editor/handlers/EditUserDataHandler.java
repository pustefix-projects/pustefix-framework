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

package de.schlund.pfixcore.editor.handlers;


import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.EditorProductFactory;
import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.auth.AuthManagerFactory;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.auth.ProjectPermissions;
import de.schlund.pfixcore.editor.interfaces.EditUserData;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.util.UnixCrypt;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.util.statuscodes.StatusCode;
import de.schlund.util.statuscodes.StatusCodeFactory;
import de.schlund.util.statuscodes.StatusCodeLib;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Category;

/**
 * Handler for changing user data.
 *
 * <br/>
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class EditUserDataHandler implements IHandler {
    private Category CAT = Category.getInstance(this.getClass().getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        EditUserData           data   = (EditUserData) wrapper;
        EditorUser             curr   = esess.getUser();
        EditorUserInfo         euser  = esess.getUserForEdit();
        //StatusCodeFactory      sfac   = new StatusCodeFactory("pfixcore.editor.userdata");
        boolean                commit = true;
        
        String name  = data.getName();
        String sect  = data.getSect();
        String phone = data.getPhone();

        String pass1 = data.getPass1();
        String pass2 = data.getPass2();
        
        
        
        String crypt = null;
        
        if (pass1 != null && pass2 != null && pass1.equals(pass2)) {
            crypt = UnixCrypt.crypt(pass1);
        } else if (pass1 != null && !pass1.equals(pass2)) {
            commit = false;
            data.addSCodePass1(StatusCodeLib.PFIXCORE_EDITOR_USERDATA_PWD_NO_MATCH);
        }

        if (commit) {
            euser.setName(name);
            euser.setSect(sect);
            euser.setPhone(phone);
            
            if (crypt != null) {
                euser.setPwd(crypt);
            }
            
            
            // Permission stuff can only be edited by an admin!
            if(curr.getUserInfo().isAdmin()) {
                
                if(CAT.isDebugEnabled())
                    CAT.debug("Admin is editing permissions.\n"+euser.toString()); 
                
                Boolean admin = data.getAdmin();
                Boolean editDynDefault = data.getDynInclDef();
        
                
                // global
                if(admin != null && admin.booleanValue()) {
                    if(CAT.isDebugEnabled())
                        CAT.debug("Setting "+euser.getId()+" to admin");
                    euser.getGlobalPerms().setAdmin(true);
                } else {
                    if(CAT.isDebugEnabled())
                        CAT.debug("Setting "+euser.getId()+" to NON admin");
                    euser.getGlobalPerms().setAdmin(false);
                }
                if(editDynDefault != null && editDynDefault.booleanValue()) {
                    if(CAT.isDebugEnabled())
                        CAT.debug("Setting "+euser.getId()+" to DynInclEditDefault");
                    euser.getGlobalPerms().setEditDynIncludesDefault(true);
                } else {
                    if(CAT.isDebugEnabled())
                        CAT.debug("Setting "+euser.getId()+" to NON DynInclEditDefault");
                    euser.getGlobalPerms().setEditDynIncludesDefault(false);
                }
                
                               
                // projects
                EditorProduct[] prods = EditorProductFactory.getInstance().getAllEditorProducts();
                for(int i=0; i<prods.length; i++) {
                    String n = prods[i].getName();
                     
                    //edit dynincludes
                    String ec = data.getEditcom(n);
                    if(ec!=null &&  ec.equals("true")) {
                        ProjectPermissions p = euser.getProjectPerms(n);
                        if(p!=null) {
                            p.setEditDynIncludes(true);
                        } else {
                            p = new ProjectPermissions();
                            p.setEditDynIncludes(true);
                            euser.addProjectPermission(n, p);
                        }
                    } else {
                        ProjectPermissions p = euser.getProjectPerms(n);
                        if(p!=null){
                            p.setEditDynIncludes(false);
                        } else {
                            p = new ProjectPermissions();
                            p.setEditDynIncludes(true);
                            euser.addProjectPermission(n, p);
                        }
                    }
                    
                    //edit images
                    String ei = data.getEditimg(n);
                    if(ei!= null && ei.equals("true")) {
                        ProjectPermissions p = euser.getProjectPerms(n);
                        if(p!=null) {
                            p.setEditImages(true);
                        } else {
                            p = new ProjectPermissions();
                            p.setEditImages(true);
                            euser.addProjectPermission(n, p);
                        }
                    } else {
                        ProjectPermissions p = euser.getProjectPerms(n);
                        if(p!=null){
                            p.setEditImages(false);
                        } else {
                            p = new ProjectPermissions();
                            p.setEditImages(true);
                            euser.addProjectPermission(n, p);
                        }
                    }
                    
                    //edit includes
                    String en = data.getEditincl(n);
                    if(en!=null && en.equals("true")) {
                        ProjectPermissions p = euser.getProjectPerms(n);
                        if(p!=null) {
                            p.setEditIncludes(true);
                        } else {
                        p = new ProjectPermissions();
                        p.setEditIncludes(true);
                        euser.addProjectPermission(n, p);
                                                }
                    } else {
                        ProjectPermissions p = euser.getProjectPerms(n);
                        if(p!=null){
                            p.setEditIncludes(false);
                        } else {
                            p = new ProjectPermissions();
                            p.setEditIncludes(true);
                            euser.addProjectPermission(n, p);
                       }
                    }
                }
                if(CAT.isDebugEnabled()) 
                    CAT.debug("Permission editing done. \n"+euser.toString());             
            } else {
                CAT.warn("User is NOT admin. Permissions can only be edited by an admin.");
            }
            
            
            // make sure the user is really added
            if(CAT.isDebugEnabled())
                CAT.debug("Adding user.");
            EditorUser.addUser(euser);
            
            AuthManagerFactory.getInstance().getAuthManager().commit();
            
            // reset the selected "user for editing"
            esess.setUserForEdit(null);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        EditUserData           data  = (EditUserData) wrapper;
        EditorUserInfo             euser = esess.getUserForEdit();

        data.setStringValName(euser.getName());
        data.setStringValPhone(euser.getPhone());
        data.setStringValSect(euser.getSect());
        data.setStringValAdmin(""+euser.getGlobalPerms().isAdmin());
        data.setStringValDynInclDef(""+euser.getGlobalPerms().isEditDynIncludesDefault());
        
        
        HashMap prjperms = euser.getAllProjectPerms();
        EditorProduct[] allprj = EditorProductFactory.getInstance().getAllEditorProducts();
        for(int i=0; i<allprj.length; i++) {
            String name = allprj[i].getName();
            if(prjperms.containsKey(name)) {
                ProjectPermissions p = (ProjectPermissions) prjperms.get(name);
                data.setStringValEditcom(""+p.isEditDynIncludes(), name);
                data.setStringValEditimg(""+p.isEditImages(), name);
                data.setStringValEditincl(""+p.isEditIncludes(), name);  
            } else {
                data.setStringValEditcom("false", name);
                data.setStringValEditimg("false", name);
                data.setStringValEditincl("false", name);
            }
        }
        
        
        if(CAT.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("\nGlobalPerms:  Admin="+euser.getGlobalPerms().isAdmin()+" editDefault="+euser.getGlobalPerms().isEditDynIncludesDefault()).append("\n");
            HashMap prp = euser.getAllProjectPerms();
            Iterator iter = prp.keySet().iterator();
            while(iter.hasNext()){
                String key = (String)iter.next();
                ProjectPermissions p = (ProjectPermissions) prp.get(key);
                sb.append("Projects: "+key+"  editIncludes="+p.isEditIncludes()+" editImages="+p.isEditImages()+" editDefaults="+p.isEditDynIncludes()).append("\n");
            }
            CAT.info(sb.toString());
        }
    }
        
    public boolean prerequisitesMet(Context context) {
        return true;
    }
    
    public boolean isActive(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        if (esess.getUserForEdit() != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean needsData(Context context) {
        return true;
    }
    
}// EditUserDataHandler
