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


import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.editor.EditorUser;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.Path;
import de.schlund.pfixxml.targets.Target;
import java.util.HashSet;

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

public interface EditorSessionStatus extends ContextResource {
    EditorUser     getUser();
    EditorUserInfo getUserForEdit();
    EditorProduct  getProduct();
    PageInfo       getCurrentPage();
    Target         getCurrentTarget();
    AuxDependency  getCurrentImage();
    AuxDependency  getCurrentInclude();
    AuxDependency  getCurrentCommon();
    String         getCurrentDocumentationId();
    void           setUser(EditorUser user);
    void           setUserForEdit(EditorUserInfo user);
    void           setProduct(EditorProduct product);
    void           setCurrentPage(PageInfo page);
    void           setCurrentTarget(Target target);
    void           setCurrentImage(AuxDependency bild);
    void           setCurrentInclude(AuxDependency include);
    void           setCurrentCommon(AuxDependency common);
    void           setCurrentDocumentationId(String id);
    boolean        getLock(AuxDependency aux);
    boolean        isOwnLock(AuxDependency aux);
    void           releaseLock();
    String         getEditorSessionId();
    void           setLoginAllowed(boolean status);
    boolean        getLoginAllowed();
    String         getBackupDir();
    HashSet        getAffectedProductsForCurrentInclude() throws Exception;
    void           resetAffectedProductsForCurrentInclude();
    void           showAdditionalIncfiles(boolean doshow);
    boolean        getShowAdditionalIncfiles();
}// EditorSessionStatus
