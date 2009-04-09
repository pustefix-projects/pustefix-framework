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
package de.schlund.pfixcore.example;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.pustefixframework.test.PustefixWebApplicationContextLoader;
import org.pustefixframework.test.XmlAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.schlund.pfixcore.example.iwrapper.AdultInfo;
import de.schlund.pfixcore.example.iwrapper.TShirt;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.util.XMLUtils;

@ContextConfiguration(loader=PustefixWebApplicationContextLoader.class,locations={"docroot:/WEB-INF/project.xml","docroot:/WEB-INF/spring.xml"})
public class OrderFlowTest extends AbstractJUnit38SpringContextTests implements ServletContextAware, ApplicationContextAware {
    
    private ServletContext servletContext;
    
    @Autowired
    @Qualifier("global_testdata")
    private TestData testData;
    
    @Autowired
    private Context pustefixContext;
    
    @Autowired
    private OverviewState overviewState;
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public void testSingletonBean() {
        Assert.assertEquals(testData.getText(), "bar");
    }
    
    public void testSessionBean() {
        Assert.assertEquals(testData.getText(), "bar");
    }
    
    public void testHandler() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setPathInfo("/home");
        req.setMethod("GET");
        //MockHttpServletResponse res = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession(servletContext);
        req.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        PfixServletRequest pfxReq = new PfixServletRequestImpl(req,new Properties());
        ((ContextImpl)pustefixContext).prepareForRequest();
        //((ContextImpl)pustefixContext).setCurrentPageRequest(new PageRequest("dummy"));
        ((ContextImpl)pustefixContext).setPfixServletRequest(pfxReq);
        //session.setAttribute(SessionHelper.SESSION_ID_URL, SessionHelper.getURLSessionId(req));
        
        Assert.assertFalse(overviewState.isAccessible(pustefixContext, pfxReq));
        
        AdultInfoHandler handler = (AdultInfoHandler)applicationContext.getBean(AdultInfoHandler.class.getName()+"#home#info");
        Assert.assertTrue(handler.needsData(pustefixContext));
        
        AdultInfo adultInfo = new AdultInfo();
        adultInfo.init("info");
        adultInfo.setStringValAdult("false");
        adultInfo.loadFromStringValues();
        handler.handleSubmittedData(pustefixContext, adultInfo);
        
        TShirtHandler tshirtHandler = (TShirtHandler)applicationContext.getBean(TShirtHandler.class.getName()+"#order#shirt");
        TShirt tshirt = new TShirt();
        tshirt.init("shirt");
        tshirt.setStringValColor("3");
        tshirt.setStringValSize("XL");
        tshirt.setStringValFeature(new String[] {"0","1","2"});
        tshirt.loadFromStringValues();
        tshirtHandler.handleSubmittedData(pustefixContext, tshirt);
        
        Assert.assertTrue(overviewState.isAccessible(pustefixContext, pfxReq));
        
        ResultDocument resDoc = overviewState.getDocument(pustefixContext, pfxReq);
        
        Document doc = resDoc.getSPDocument().getDocument();
        
        Node expNode = XMLUtils.parse("<adultinfo adult=\"false\"/>").getDocumentElement();
        XmlAssert.assertEquals(expNode, doc.getElementsByTagName("adultinfo").item(0));
        
        expNode = XMLUtils.parse("<tshirt color=\"3\" size=\"XL\"><feature><int>0</int>"+
                "<int>1</int><int>2</int></feature></tshirt>").getDocumentElement();
        XmlAssert.assertEquals(expNode, doc.getElementsByTagName("tshirt").item(0));
        
        //((ContextImpl)pustefixContext).cleanupAfterRequest();
    
    }
    
}
