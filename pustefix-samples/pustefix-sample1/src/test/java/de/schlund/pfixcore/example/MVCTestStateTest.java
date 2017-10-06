package de.schlund.pfixcore.example;

import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.pustefixframework.config.contextxmlservice.parser.internal.StateConfigImpl;
import org.pustefixframework.test.MockContext;
import org.pustefixframework.web.mvc.AnnotationMethodHandlerAdapterConfig;
import org.pustefixframework.web.mvc.RequestMappingHandlerAdapterConfig;
import org.pustefixframework.web.mvc.internal.ControllerStateAdapterImpl;
import org.pustefixframework.web.mvc.internal.ControllerStateAdapterLegacyImpl;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.ResultDocument;
import junit.framework.TestCase;

public class MVCTestStateTest extends TestCase {

    public void test() throws Exception {

        GenericApplicationContext appContext = new GenericApplicationContext();
        appContext.registerBeanDefinition("controller", new RootBeanDefinition(MVCTestState.class));
        appContext.refresh();

        MVCTestState state = (MVCTestState)appContext.getBean("controller");
        StateConfigImpl stateConfig = new StateConfigImpl();
        stateConfig.setProperties(new Properties());
        state.setConfig(new StateConfigImpl());
        ContextData contextData = new ContextData();
        state.setContextData(contextData);

        ControllerStateAdapterImpl adapter = new ControllerStateAdapterImpl();
        adapter.setApplicationContext(appContext);
        adapter.setAdapterConfig(RequestMappingHandlerAdapterConfig.createDefaultConfig());
        adapter.afterPropertiesSet();
        state.setAdapter(adapter);

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

    public void testLegacy() throws Exception {

        MVCTestState state = new MVCTestState();
        StateConfigImpl stateConfig = new StateConfigImpl();
        stateConfig.setProperties(new Properties());
        state.setConfig(new StateConfigImpl());
        ContextData contextData = new ContextData();
        state.setContextData(contextData);

        ControllerStateAdapterLegacyImpl adapter = new ControllerStateAdapterLegacyImpl();
        adapter.setAdapterConfig(AnnotationMethodHandlerAdapterConfig.createDefaultConfig());
        adapter.afterPropertiesSet();
        state.setAdapter(adapter);

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