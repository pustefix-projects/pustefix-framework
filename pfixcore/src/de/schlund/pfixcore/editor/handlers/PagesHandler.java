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
import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.util.statuscodes.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;

import org.apache.log4j.Category;

/**
 * PagesHandler.java
 *
 *
 * Created: Fr Nov 30 13:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class PagesHandler extends EditorStdHandler {
    private static Category CAT = Category.getInstance(PagesHandler.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        EditorProduct          eprod    = esess.getProduct();
        TargetGenerator        tgen     = eprod.getTargetGenerator();
        Pages                  pages    = (Pages) wrapper;
        PageInfo               newpage  = PageInfoFactory.getInstance().getPage(tgen, pages.getPage());  
        PageTargetTree         ptree    = tgen.getPageTargetTree();
        TreeSet                allpages = ptree.getPageInfos();

        if (allpages.contains(newpage)) {
            Target target = ptree.getTargetForPageInfo(newpage);
            if (target != null) {
                try {
                    target.getValue(); // to force an update
                } catch(Exception e) {
                    CAT.warn("Exception when forcing update!", e);
                }
            }
            esess.setCurrentPage(newpage);
        } else {
            StatusCodeFactory sfac  = new StatusCodeFactory("pfixcore.editor.pages");
            StatusCode        scode = sfac.getStatusCode("PAGE_UNDEF");
            pages.addSCodePage(scode);
        }
    }
    
}// PagesHandler
