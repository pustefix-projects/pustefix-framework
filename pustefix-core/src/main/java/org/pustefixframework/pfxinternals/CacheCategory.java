package org.pustefixframework.pfxinternals;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixxml.targets.cachestat.CacheStatistic;

public class CacheCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {
        
        CacheStatistic cacheStatistic = context.getApplicationContext().getBean(CacheStatistic.class);
        Document doc = cacheStatistic.getAsXML();
        Node imported = parent.getOwnerDocument().importNode(doc.getDocumentElement(), true);
        parent.appendChild(imported);
    }
}
