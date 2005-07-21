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

import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.statuscodes.*;
import java.util.*;
import org.apache.oro.text.regex.*;


/**
 * EditorSearch.java
 *
 *
 * Created: Tue Mar 12 21:54:41 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public interface EditorSearch extends ContextResource {
    StatusCode SCODE_RESET   = StatusCodeFactory.getInstance().getStatusCode("pfixcore.editor.search.S_RESET");
    StatusCode SCODE_RUNNING = StatusCodeFactory.getInstance().getStatusCode("pfixcore.editor.search.S_RUNNING");
    StatusCode SCODE_OK      = StatusCodeFactory.getInstance().getStatusCode("pfixcore.editor.search.S_OK");
    StatusCode SCODE_INT     = StatusCodeFactory.getInstance().getStatusCode("pfixcore.editor.search.S_INT");
    String     INCLUDE       = "include";
    String     COMMON        = "common";
    
    void                  setPattern(Perl5Pattern pattern);
    Perl5Pattern          getPattern();
    StatusCode            getStatus();
    TreeSet               getResultSet();
    TreeSet               getDynResultSet();
    EditorSearchContext[] getSearchContexts(AuxDependency result);
    EditorSearchContext[] getDynSearchContexts(AuxDependency result);
    void                  setFullSource(Boolean full);
    Boolean               getFullSource();
    void                  startSearch() throws Exception;
}

// EditorSearch
