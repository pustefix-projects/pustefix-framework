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

package de.schlund.pfixcore.editor.resources;

import java.util.*;

import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import org.w3c.dom.*;

/**
 * EditorSessionStatus.java
 *
 *
 * Created: Sat Nov 24 01:25:19 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditorSessionStatusImpl implements ContextResource, EditorSessionStatus {
    private static       boolean login_allowed = true;
    private static final String  BACKDIR_DEF   = "/tmp/editor_backup";
    private static final String  BACKDIR_PROP  = "pfixcore.editor.backupdir";
    
    private EditorUser         user               = null;
    private EditorUserInfo     userforedit        = null;
    private EditorProduct      product            = null;
    private PageInfo           currentpage        = null;
    private Target             currenttarget      = null;
    private AuxDependency      currentimage       = null;
    //private AuxDependency currentinclude        = null;
    private AuxDependency      currentcommon      = null;
    private Context            context            = null;
    private String             currentdokuid      = null;
    private CurrentIncludeInfo currentIncludeInfo = new CurrentIncludeInfo();
    private boolean            addincshown        = false;


    public void showAdditionalIncfiles(boolean doshow) {
        addincshown = doshow;
    }

    public boolean getShowAdditionalIncfiles() {
        return addincshown;
    }
    
    public void insertStatus(ResultDocument resdoc, Element root) throws Exception {
        root.setAttribute("loginallowed", "" + getLoginAllowed());
        if (user != null) {
            user.insertStatus(resdoc, root);
        }
        if (product != null) {
            product.insertStatus(resdoc, root);
        }
        
        EditorProduct[] prods = EditorProductFactory.getInstance().getAllEditorProducts();
        Node all = resdoc.createNode("allproducts");
        Element n = (Element)root.appendChild(all);
        if(prods != null) {
            for(int i=0; i<prods.length; i++) {
                prods[i].insertStatus(resdoc, n);
                // Why did we need this???
                //n.setAttribute("id", ""+i);
            }
        }
    }
    
    public void init(Context context) {
        this.context = context;
    }
    
    public void reset() {
        // user           = null;
        userforedit    = null;
        product        = null;
        currentpage    = null;    
        currenttarget  = null;    
        currentimage   = null;    
       // currentinclude = null;
        currentIncludeInfo.resetAll();    
        currentcommon  = null;
        releaseLock();
    }
    
    public boolean needsData() {
        if (user == null && product == null) {
            return true;
        } else {
            return false;
        }
    }

    public void releaseLock() {
        EditorLockFactory.getInstance().releaseLock(this);
    }
    
    public boolean getLock(AuxDependency aux) {
        return EditorLockFactory.getInstance().getLock(this, aux);
    }
    
    public boolean isOwnLock(AuxDependency aux) {
        return EditorLockFactory.getInstance().isLockedBySession(this, aux);
    }
    
    public String getEditorSessionId() {
        return "ES_" + context.getVisitId();
    }

    public String getBackupDir() {
        Properties props = context.getProperties();
        String     val   = props.getProperty(BACKDIR_PROP);
        if (val != null && !val.equals("")) {
            return val;
        } else {
            return BACKDIR_DEF;
        }
    }

    public EditorUser    getUser() {return user;}
    public EditorUserInfo    getUserForEdit() {return userforedit;}
    public EditorProduct getProduct() {return product;}
    public PageInfo      getCurrentPage() {return currentpage;}
    public Target        getCurrentTarget() {return currenttarget;}
    public AuxDependency getCurrentImage() {return currentimage;}
    public AuxDependency getCurrentInclude() {
        return currentIncludeInfo.getCurrentInclude();
    }
    public AuxDependency getCurrentCommon() {return currentcommon;}
    public String        getCurrentDocumentationId() {return currentdokuid; }
    public boolean       getLoginAllowed() {return login_allowed; }
    public void          setUser(EditorUser user) {this.user = user;}
    public void          setUserForEdit(EditorUserInfo userforedit) {this.userforedit = userforedit;}
    public void          setProduct(EditorProduct product) {this.product = product;}
    public void          setCurrentPage(PageInfo page) {currentpage = page;}
    public void          setCurrentTarget(Target target) {currenttarget = target;}
    public void          setCurrentImage(AuxDependency image) {currentimage = image;}
    public void          setCurrentInclude(AuxDependency include) {
        currentIncludeInfo.setCurrentInclude(include);
    }
    public void          setCurrentCommon(AuxDependency common) {currentcommon = common;}
    public void          setCurrentDocumentationId(String id) { currentdokuid = id; }
    public void          setLoginAllowed(boolean status) {login_allowed = status;}

    /**
     * @see de.schlund.pfixcore.editor.resources.EditorSessionStatus#getAffectedProductsforCurrentInclude()
     */
    public HashSet getAffectedProductsForCurrentInclude() throws Exception {
        return currentIncludeInfo.getAfftectedProducts(this);
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.EditorSessionStatus#resetAffectedProductsForCurrentInclude()
     */
    public void resetAffectedProductsForCurrentInclude() {
        currentIncludeInfo.resetAffectedProducts();
    }

}// EditorSessionStatusImpl
