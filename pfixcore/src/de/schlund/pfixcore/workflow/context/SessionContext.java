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
 */

package de.schlund.pfixcore.workflow.context;

import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixxml.Variant;

/**
 * Keeps context information that is bound to a user session. Implementations
 * have to be thread safe as there may be several request that access this 
 * class concurrently.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface SessionContext {
    /**
     * Returns the context resource manager that keeps the 
     * {@link de.schlund.pfixcore.workflow.ContextResource} instances
     * for the user session.
     * 
     * @return manager used to access context resources
     */
    ContextResourceManager getContextResourceManager();
    
    /**
     * Sets the language being used for output to the user.
     * This is used when the page is rendered and if there are different
     * branches for different languages.
     * 
     * @param langcode a code identifying the language that should be used
     */
    void setLanguage(String langcode);
    
    /**
     * Returns the language being used to render output.
     * 
     * @return language code
     */
    String getLanguage();
    
    /**
     * Returns the variant being used in this session
     * 
     * @return current variant or <code>null</code> if no variant is set
     */
    Variant getVariant();
    
    /**
     * Sets the variant to use for the current session
     * 
     * @param variant variant to use for the current session or
     *        <code>null</code> to reset the variant
     */
    void setVariant(Variant variant);
    
    /**
     * Returns an unique identifier for the current user session.
     * This identifier is kept even when the session id changes (e.g.
     * because of a switch to SSL).
     * 
     * @return unique identfier for the user session
     */
    String getVisitId();
}
