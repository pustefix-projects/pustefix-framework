/*
 * Created on 19.02.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.ContextResource;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ContextEncodingTest  extends ContextResource {
    
    public String getText();
    public void setText(String text);
    public String getEncoding();
    public void setEncoding(String encoding);

}
