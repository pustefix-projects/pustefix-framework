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
import de.schlund.pfixxml.util.Path;
import java.util.*;

import org.apache.log4j.Category;

/**
 * ImagesHandler.java
 *
 *  Handler responsible for selecting images.
 *
 * Created: Mon Dec 03 23:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ImagesHandler extends EditorStdHandler {
    
    private static Category CAT = Category.getInstance(ImagesHandler.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        Images                 images = (Images) wrapper;
        EditorProduct          prod   = esess.getProduct();
        TargetGenerator        gen    = prod.getTargetGenerator();
        TreeSet                allimg = gen.getDependencyRefCounter().getDependenciesOfType(DependencyType.IMAGE);
        Path                   path   = Path.create(gen.getDocroot(), images.getPath());
        AuxDependency          image  =
            AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.IMAGE, path, null, null);
        
        if (allimg.contains(image)) {
            esess.setCurrentImage(image);
        } else {
            StatusCodeFactory sfac  = new StatusCodeFactory("pfixcore.editor.images");
            StatusCode        scode = sfac.getStatusCode("IMAGE_UNDEF");
            images.addSCodePath(scode);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        AuxDependency          currimg = esess.getCurrentImage();
        
        
        if (currimg != null) {
            boolean allowed = esess.getUser().getUserInfo().isImageEditAllowed(currimg.getPath());
            if(allowed)
                esess.getLock(currimg);
            else {
                if(CAT.isDebugEnabled())
                    CAT.debug("User is not allowed to edit this image. No lock required.");
            }
        }
    }
    
}// ImagesHandler
