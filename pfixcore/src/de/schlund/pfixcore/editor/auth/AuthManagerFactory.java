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

/**
 * This class creates an implementation of the <code>AuthManager</code>
 * interface depending on the properties passed by <code>FactoryInit</code>.
 * <br/>
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class AuthManagerFactory {
    private static AuthManagerFactory instance = new AuthManagerFactory();
    private static final Category CAT = Category.getInstance(AuthManagerFactory.class.getName());

    private AuthManager current = null;
    private String pathToPwdFile;

    private AuthManagerFactory() {}

    private static final String PROP_UF = "editoruserfactory.userdata";
   
    /**
     * @see de.schlund.util.FactoryInit#init(java.util.Properties)
     */
    public void init(Properties props) throws Exception {
        if (CAT.isInfoEnabled())
            CAT.info(this.getClass().getName() + " init start. Doing lazy init...");

        if (!props.containsKey(PROP_UF)) {
            throw new AuthManagerException("Need property '" + PROP_UF + "' for userdata!");
        }
        pathToPwdFile = props.getProperty(PROP_UF);

    }
    
    public static AuthManagerFactory getInstance() {
        return instance;
    }

    public synchronized AuthManager getAuthManager() throws AuthManagerException {
        if (current == null) {
            long starttime = 0;
            if (CAT.isInfoEnabled()) {
                CAT.info(this.getClass().getName() + " init start. Now doing real init!");
                starttime = System.currentTimeMillis();
            }
            FileAuthManager auth = new FileAuthManager();
            auth.setPwdFile(pathToPwdFile);
            auth.init();
            current = auth;
            if (CAT.isInfoEnabled()) {
                CAT.info(this.getClass().getName() + " real init done. Duration :"+(System.currentTimeMillis() - starttime));
            
            }
        }
        return current;
    }

}
