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
package de.schlund.pfixcore.oxm.impl;

/**
 * @author mleidig@schlund.de
 * @author Stephan Schmid <schst@stubbles.net>
 */
public interface XMLWriter {

    public void writeStartElement(String localName);
    public void writeCharacters(String text);
    public void writeEndElement();
    public void writeAttribute(String localName,String value);
    
    /**
     * Writes a character data section
     * 
     * @param cdata
     */
    public void writeCDataSection(String cdata);
    
    /**
     * Writes an xml fragment to the document.
     * 
     * The fragment does not need a root element, but it must
     * be well-formed xml.
     * 
     * @param   xmlFragment     The fragment to be written to the document.
     */
    public void writeFragment(String xmlFragment);
    public XPathPosition getCurrentPosition();
}
