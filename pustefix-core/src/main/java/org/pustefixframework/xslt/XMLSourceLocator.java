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
package org.pustefixframework.xslt;

import javax.xml.transform.SourceLocator;

/**
 * Source locator implementation for XSL transformation errors reporting
 * the XML source location.
 */
public class XMLSourceLocator implements SourceLocator {

    private final int col;
    private final int line;
    private final String publicId;
    private final String systemId;
    
    public XMLSourceLocator(SourceLocator locator) {
        col = locator.getColumnNumber();
        line = locator.getLineNumber();
        publicId = locator.getPublicId();
        systemId = locator.getSystemId();
    }
    
    public XMLSourceLocator(String systemId, int line) {
        this.col = -1;
        this.line = line;
        this.publicId = null;
        this.systemId = systemId;
    }
    
    @Override
    public int getColumnNumber() {
        return col;
    }
    
    @Override
    public int getLineNumber() {
        return line;
    }
    
    @Override
    public String getPublicId() {
        return publicId;
    }
    
    @Override
    public String getSystemId() {
        return systemId;
    }

}
