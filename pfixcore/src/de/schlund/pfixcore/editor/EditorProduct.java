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


import de.schlund.pfixcore.workflow.Navigation;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.Path;
import org.apache.log4j.Category;
import org.w3c.dom.Element;

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
    private static Category               CAT   = Category.getInstance(EditorProduct.class.getName());
    
    private final String                 name;
    private final String                 comment;
    private final Path                   depend;
    private final TargetGenerator        generator;
    private final Navigation             navigation;
    private final PfixcoreServlet[]      servlets;
    private final PfixcoreNamespace[]    nspaces;
    private final EditorDocumentation    editdoku;
    
    public EditorProduct (String name, String comment, Path depend, TargetGenerator generator,
                          Navigation navigation, PfixcoreServlet[] servlets, PfixcoreNamespace[] nspaces, 
                          EditorDocumentation editdoku) {
        this.name       = name;
        this.comment    = comment;
        this.depend     = depend;
        this.generator  = generator;
        this.navigation = navigation;
        this.servlets   = servlets;
        this.nspaces    = nspaces;
        this.editdoku   = editdoku;
    }


    public String getName() {return name;}
    public String getComment() {return comment;}
    public Path   getDepend() {return depend;}
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
