package org.pustefixframework.http;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

public class RequestIdFilterTest extends TestCase {

    public void testIdGenerating() throws Exception {

        int threadNo = 50;
        int idsPerThread = 1000;
        RequestIdFilter filter = new RequestIdFilter();
        ConcurrentMap<String, String> generatedIds = new ConcurrentHashMap<>();
        GeneratorThread[] threads = new GeneratorThread[threadNo];
        for(int i=0; i<threadNo; i++) {
            threads[i] = new GeneratorThread();
            threads[i].filter = filter;
            threads[i].generatedIds = generatedIds;
            threads[i].number = idsPerThread;
            threads[i].start();
        }
        for(int i=0; i<threadNo; i++) {
            threads[i].join();
        }
        for(int i=0; i<threadNo; i++) {
            assertFalse("Generated duplicate ID", threads[i].duplicate);
        }
        assertEquals(threadNo * idsPerThread, generatedIds.size());
    }
    
    public void testFiltering() throws Exception {
        
        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                //check if values are available and valid during processing
                String attributeValue = (String)request.getAttribute(RequestIdFilter.DEFAULT_ATTRIBUTE_NAME);
                String headerValue = ((HttpServletResponse)response).getHeader(RequestIdFilter.DEFAULT_HEADER_NAME);
                String mdcValue = (String)MDC.get(RequestIdFilter.DEFAULT_MDC_KEY);
                assertNotNull(attributeValue);
                assertFalse(attributeValue.isEmpty());
                assertEquals(attributeValue, headerValue);
                assertEquals(attributeValue, mdcValue);
            }
        };
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        //check if values are reset correctly after completing
        assertNull(request.getAttribute(RequestIdFilter.DEFAULT_ATTRIBUTE_NAME));
        assertNull(MDC.get(RequestIdFilter.DEFAULT_MDC_KEY));
    }
    
    public void testFilteringWithCustomParamNames() throws Exception {
        
        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                //check if values are available and valid during processing
                String attributeValue = (String)request.getAttribute("foo");
                String headerValue = ((HttpServletResponse)response).getHeader("bar");
                String mdcValue = (String)MDC.get("baz");
                assertNotNull(attributeValue);
                assertFalse(attributeValue.isEmpty());
                assertEquals(attributeValue, headerValue);
                assertEquals(attributeValue, mdcValue);
            }
        };
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("attributeName", "foo");
        config.addInitParameter("headerName", "bar");
        config.addInitParameter("mdcKey", "baz");
        RequestIdFilter filter = new RequestIdFilter();
        filter.init(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        //check if values are reset correctly after completing
        assertNull(request.getAttribute("foo"));
        assertNull(MDC.get("baz"));
    }
    
    public void testFilteringWithEmptyParamNames() throws Exception {
        
        FilterChain chain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                //check if values are available and valid during processing
                String attributeValue = (String)request.getAttribute(RequestIdFilter.DEFAULT_ATTRIBUTE_NAME);
                String headerValue = ((HttpServletResponse)response).getHeader(RequestIdFilter.DEFAULT_HEADER_NAME);
                String mdcValue = (String)MDC.get(RequestIdFilter.DEFAULT_MDC_KEY);
                assertNull(attributeValue);
                assertNull(headerValue);
                assertNull(mdcValue);
            }
        };
        MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("attributeName", "");
        config.addInitParameter("headerName", "");
        config.addInitParameter("mdcKey", " ");
        RequestIdFilter filter = new RequestIdFilter();
        filter.init(config);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        //check if values are reset correctly after completing
        assertNull(request.getAttribute(RequestIdFilter.DEFAULT_ATTRIBUTE_NAME));
        assertNull(MDC.get(RequestIdFilter.DEFAULT_MDC_KEY));
    }
    
    
    class GeneratorThread extends Thread {
        
        RequestIdFilter filter;
        ConcurrentMap<String, String> generatedIds;
        int number;
        boolean duplicate;
        
        @Override
        public void run() {
            for(int i=0; i<number; i++) {
                String id = filter.generateID();
                String old = generatedIds.putIfAbsent(id, id);
                if(old != null) {
                    duplicate = true;
                }
            }
        }
    }

}
