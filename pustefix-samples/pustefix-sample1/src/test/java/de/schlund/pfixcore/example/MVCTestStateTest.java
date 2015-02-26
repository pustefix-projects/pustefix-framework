package de.schlund.pfixcore.example;

import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.test.MockContext;
import org.pustefixframework.web.mvc.internal.ControllerStateAdapter;
import org.springframework.mock.web.MockHttpServletRequest;

import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.ResultDocument;

public class MVCTestStateTest extends TestCase {

    public void test() throws Exception {
        
        MVCTestState state = new MVCTestState();
        StateConfigImpl stateConfig = new StateConfigImpl();
        stateConfig.setProperties(new Properties());
        state.setConfig(new StateConfigImpl());
        ControllerStateAdapter adapter = new ControllerStateAdapter();
        adapter.afterPropertiesSet();
        state.setAdapter(adapter);
        ContextData contextData = new ContextData();
        state.setContextData(contextData);
        
        MockContext context = new MockContext();
        context.setPropertiesForCurrentPageRequest(new Properties());
        context.setProperties(new Properties());
        MockHttpServletRequest req = new MockHttpServletRequest();
        PfixServletRequest preq = new PfixServletRequestImpl(req, new Properties());
        context.setCurrentPageRequest(new PageRequest("mvctest"));
        
        ResultDocument resDoc = state.getDocument(context, preq);
        XPath xpath = XPathFactory.newInstance().newXPath();
        int count = Integer.valueOf(xpath.evaluate("count(//dataBean)", resDoc.getSPDocument().getDocument()));
        assertEquals(10, count);
    }

}