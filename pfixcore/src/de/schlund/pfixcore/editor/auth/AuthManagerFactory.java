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

import java.util.Properties;

import org.apache.log4j.Category;

import de.schlund.util.FactoryInit;

/**
 * This class creates an implementation of the <code>AuthManager</code>
 * interface depending on the properties passed by <code>FactoryInit</code>.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class AuthManagerFactory implements FactoryInit {
    private static AuthManagerFactory instance = new AuthManagerFactory(); 
    private static Category CAT = Category.getInstance(AuthManagerFactory.class.getName());
    
    AuthManager current = null;
    
    private AuthManagerFactory() {
    }
    
    private static final String PROP_UF = "editoruserfactory.userdata";
    /**
     * @see de.schlund.util.FactoryInit#init(java.util.Properties)
     */
    public void init(Properties arg0) throws Exception {
        if(CAT.isDebugEnabled())
            CAT.debug(this.getClass().getName()+" init start");
        FileAuthManager auth = new FileAuthManager();
        auth.setPwdFile(arg0.getProperty(PROP_UF));
        auth.init();
        current = auth;
        if(CAT.isDebugEnabled())
            CAT.debug(this.getClass().getName()+" init end");
    }
    
    public static AuthManagerFactory getInstance() {
        return instance;
    }
    
    public AuthManager getAuthManager() {
        if(current == null)
            throw new IllegalStateException("AuthManger implementation is null! Init failed?");
        return current;
    }

}
