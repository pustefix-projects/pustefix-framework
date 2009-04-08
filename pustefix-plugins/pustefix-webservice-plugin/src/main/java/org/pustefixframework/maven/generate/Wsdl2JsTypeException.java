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
package org.pustefixframework.webservices.jaxws.generate;

import javax.xml.namespace.QName;



/**
 * Exception to be thrown if a Java type isn't supported by Javascript stub generation
 * 
 * @author mleidig@schlund.de
 */
public class Wsdl2JsTypeException extends Wsdl2JsException {
    
    private static final long serialVersionUID = 7344585477068298231L;
    
    private QName typeName;
    
    public Wsdl2JsTypeException(QName typeName) {
        super("Type '"+typeName+"' isn't supported.");
        this.typeName = typeName;
    }
    
    /**
     * Returns the QName of the unsupported type.
     */
    public QName getTypeName() {
        return typeName;
    }

}
