package org.pustefixframework.samples.modular.context;

import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixxml.ResultDocument;

public class StatisticsContext {

    private int tries;
    private int success;
  
    public int getTries() {
        return tries;
    }
    
    public void incTries() {
        tries++;
    }
    
    public void incSuccess() {
        success++;
    }
    
    @InsertStatus
    public void insertStatus(ResultDocument document, Element element) throws Exception {
        element.setAttribute("tries", String.valueOf(tries));
        element.setAttribute("success", String.valueOf(success));
    }
    
}
