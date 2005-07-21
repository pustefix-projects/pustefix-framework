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

import java.util.Properties;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.util.statuscodes.StatusCode;

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

    public void addStatusCode(Properties props, StatusCode code, String field) {
        addStatusCode(props, code, null, field);
    }

    public void addStatusCode(Properties props, StatusCode code, String[] args, String field) {
        Element elem = ResultDocument.createIncludeFromStatusCode(doc, props, code, args);
        addErrorNode(field, elem);
    }
}

