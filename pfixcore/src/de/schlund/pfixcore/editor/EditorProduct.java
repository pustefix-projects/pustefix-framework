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

package de.schlund.pfixcore.editor;
import de.schlund.pfixcore.workflow.*;
import de.schlund.util.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;
import org.apache.log4j.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xpath.*;

/**
 * EditorProduct.java
 *
 *
 * Created: Sat Nov 24 01:52:26 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class EditorProduct {
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private static Category               CAT   = Category.getInstance(EditorProduct.class.getName());
    private        String                 name;
    private        String                 comment;
    private        TargetGenerator        generator;
    private        Navigation             navigation;
    private        PfixcoreServlet[]      servlets;
    private        PfixcoreNamespace[]    nspaces;
    private		   EditorDocumentation	  editdoku;
    
    public EditorProduct (String name, String comment, TargetGenerator generator,
                          Navigation navigation, PfixcoreServlet[] servlets, PfixcoreNamespace[] nspaces, EditorDocumentation editdoku) {
        this.name       = name;
        this.comment    = comment;
        this.generator  = generator;
        this.navigation = navigation;
        this.servlets   = servlets;
        this.nspaces    = nspaces;
        this.editdoku   = editdoku;
    }


    public String getName() {return name;}
    public String getComment() {return comment;}
    public TargetGenerator getTargetGenerator() {return generator;}
    public Navigation getNavigation() {return navigation;}
    public PfixcoreServlet[] getPfixcoreServlets() {return servlets;}
    public PfixcoreNamespace[] getPfixcoreNamespace() {return nspaces;}
    public EditorDocumentation getDocumentation() {return editdoku;}
    
    public String toString() {
        return "[" + name + ": " + comment + "; " + generator.toString() + "]";
    }

    public void insertStatus(ResultDocument resdoc, Element root) {
        Element product = resdoc.createSubNode(root, "product");
        product.setAttribute("name", getName());
        product.setAttribute("comment", getComment());
    }
    
}// EditorProduct
