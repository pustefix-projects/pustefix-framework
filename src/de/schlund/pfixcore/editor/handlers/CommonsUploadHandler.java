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

import org.apache.log4j.*;
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import org.apache.oro.text.regex.*;


/**
 * CommonsUploadHandler.java
 *
 *  Handler responsible for uploading commons.
 *
 * Created: Wed Dec 19 12:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class CommonsUploadHandler extends XMLUploadHandler {
    private static Category CAT = Category.getInstance(CommonsUploadHandler.class.getName());

    public CommonsUploadHandler() throws MalformedPatternException {
        super();
    }

    public AuxDependency getCurrentInclude(EditorSessionStatus esess) {
        return esess.getCurrentCommon();
    }

    /**
     * @see de.schlund.pfixcore.editor.handlers.XMLUploadHandler#checkAccess(de.schlund.pfixcore.editor.auth.EditorUserInfo, java.lang.String)
     */
    public void checkAccess(EditorSessionStatus esess) throws XMLException {
        if(CAT.isDebugEnabled())
            CAT.debug("checkAccess start");
        EditorUserInfo user = esess.getUser().getUserInfo();
        
        String prod = getCurrentInclude(esess).getProduct();
        
        if(! user.isDynIncludeEditAllowed(esess.getProduct().getName(), prod)) {
            throw new XMLException("Permission denied!");
        }  
      
        if(CAT.isDebugEnabled())
            CAT.debug("checkAccess end. Permission granted.");
    }

}
