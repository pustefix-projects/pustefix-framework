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

import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import org.apache.oro.text.regex.*;

/**
 * SearchIncludeHandler.java
 *
 *
 * Created: Tue Mar 12 20:33:05 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class SearchIncludeHandler extends EditorStdHandler {

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.lang.Exception <description>
     */
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSearch           es      = EditorRes.getEditorSearch(crm);
        Search                 search  = (Search) wrapper;
        Perl5Pattern           pattern = search.getRegexp();
        Boolean                full    = search.getFullSource();
        Boolean                reset   = search.getReset();
        es.reset();
        if (reset != null && reset.booleanValue()) {
            search.setStringValRegexp("");
            search.setStringValFullSource("false");
            return; // we already did the reset, don't do anything more.
        }
        
        es.setPattern(pattern);
        if (full == null) full = Boolean.FALSE;
        es.setFullSource(full);
        es.startSearch();
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.lang.Exception <description>
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSearch           es      = EditorRes.getEditorSearch(crm);
        Search                 search  = (Search) wrapper;

        Perl5Pattern           pattern = es.getPattern();
        Boolean                full    = es.getFullSource();
        
        if (pattern != null) {
            search.setStringValRegexp(pattern.getPattern());
        }
        search.setStringValFullSource(full.toString());
    }

    
}// SearchIncludeHandler
