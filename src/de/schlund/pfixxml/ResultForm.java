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

package de.schlund.pfixxml;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import de.schlund.util.statuscodes.*;

/**
 *
 *
 */

public class ResultForm {
    protected Category LOG = Category.getInstance(ResultForm.class.getName());

    protected ResultDocument resdoc;
    protected Element        values;
    protected Element        errors;
    protected Element        hiddenvals;
    protected Document       doc;

    protected ResultForm(ResultDocument resdoc) {
        this.resdoc     = resdoc;
        this.values     = resdoc.getFormValues();
        this.errors     = resdoc.getFormErrors();
        this.hiddenvals = resdoc.getFormHiddenvals();
        doc = values.getOwnerDocument();
    }

    public void addValue(String name, String value) {
        if (value == null) return;
        Element param = doc.createElement("param");
        param.setAttribute("name", name);
        param.appendChild(doc.createTextNode(value));
        values.appendChild(param);
    }

    public void addError(String name, String value) {
        if (value == null) {
            return;
        }
        Element param = doc.createElement("error");
        param.setAttribute("name", name);
        param.appendChild(doc.createTextNode(value));
        errors.appendChild(param);
    }

    public void addHiddenValue(String name, String value) {
        if (value == null) {
            return;
        }
        Element param = doc.createElement("hidden");
        param.setAttribute("name", name);
        param.appendChild(doc.createTextNode(value));
        hiddenvals.appendChild(param);
    }

    public Element addErrorNode(String name, Element node) {
        Element  param = doc.createElement("error");
        param.setAttribute("name", name);
        param.appendChild(node);
        errors.appendChild(param);
        return param;
    }

    /**
     * class to add all ErrorIncludes for all StatusCodes found in the
     * StatusCodeEnumeration the identifiers of the status code groups
     * are translated by the idstofields hashmap into field names
     */
    // public void addStatusCodeEnum(Properties props, StatusCodeEnum scenum, HashMap idstofields ) {
    //     if (scenum == null ) return;
    //     for (Enumeration e = scenum.getIdentifierList(); e.hasMoreElements(); ) {
    //         String id = (String) e.nextElement();
    //         if (!id.equals(scenum.SCIDALL)) {
    //             String field = null;
    //             if (idstofields.get(id) != null) {
    //                 field = (String) idstofields.get(id);
    //             } else {
    //                 field = "default";
    //                 if (!id.equals(scenum.SCIDGEN))
    //                     LOG.warn( "no translation for category [" + id +"] found use \"default\" instead ...");
    //             }
    //             if (field != null) {
    //                 while (scenum.hasMoreElements(id)) {
    //                     StatusCode aSC = scenum.nextElement(id);
    //                     addStatusCode(props, aSC, field);
    //                 }
    //             }
    //         }
    //     }
    // }

    public void addStatusCode(Properties props, StatusCode code, String field) {
        Element elem = resdoc.createIncludeFromStatusCode(props, code);
        addErrorNode(field, elem);
    }

    // public void addParams(String[] names, HttpServletRequest req) {
    //     for (int i=0; i < names.length; i++) {
    //         addValue(names[i], req.getParameter(names[i]));
    //     }
    // }


    // public void addHiddenParams(String[] names, HttpServletRequest req) {
    //     for (int i=0; i < names.length; i++) {
    //         addHiddenValue(names[i], req.getParameter(names[i]));
    //     }
    // }
}

