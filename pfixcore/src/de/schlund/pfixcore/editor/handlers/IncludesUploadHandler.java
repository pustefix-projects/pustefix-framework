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
import de.schlund.pfixcore.editor.auth.EditorUserInfo;
import de.schlund.pfixcore.editor.auth.ProjectPermissions;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.URLEncoder;
import java.net.URLDecoder;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import org.apache.log4j.*;
import org.apache.oro.text.regex.*;
import org.apache.xml.serialize.*;
import org.apache.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * IncludesUploadHandler.java
 *
 *
 * Created: Wed Dec 19 12:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IncludesUploadHandler extends XMLUploadHandler {
   
    private static Category CAT = Category.getInstance(IncludesUploadHandler.class.getName());    
    
    public IncludesUploadHandler() throws MalformedPatternException {
        super();
    }

    public AuxDependency getCurrentInclude(EditorSessionStatus esess) {
        return esess.getCurrentInclude();
    }
    
   

    
    public void checkAccess(EditorSessionStatus esess) throws XMLException {
        if(CAT.isDebugEnabled())
            CAT.debug("checkAccess start");
           
        EditorUserInfo user = esess.getUser().getUserInfo();    
            
        if(! user.isIncludeEditAllowed(esess)) {
            throw new XMLException("Permission denied!");
        }
      
        if(CAT.isDebugEnabled()) 
            CAT.debug("checkAccess end. Permission granted.");    
    }

    

}// IncludesUploadHandler
