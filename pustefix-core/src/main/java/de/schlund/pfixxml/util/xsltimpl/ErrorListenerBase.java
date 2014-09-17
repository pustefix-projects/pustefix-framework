/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixxml.util.xsltimpl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

/**
 * ErrorListener logging out all TransformerExceptions.
 *
 */
public class ErrorListenerBase implements ErrorListener {

    private Logger LOG = Logger.getLogger(ErrorListenerBase.class);
    
    private List<TransformerException> errors = new ArrayList<TransformerException>();
    
    public List<TransformerException> getErrors() {
        return errors;
    }
    
    public void warning(TransformerException e) throws TransformerException {
        LOG.error("WARNING: " + e.getMessageAndLocation());
    }

    public void error(TransformerException e) throws TransformerException {
        LOG.error("ERROR: " + e.getMessageAndLocation());
        errors.add(e);
        throw e;
    }

    public void fatalError(TransformerException e) throws TransformerException {
        LOG.error("FATAL ERROR: " + e.getMessageAndLocation());
        errors.add(e);
        throw e;
    }

}
