package org.pustefixframework.http;

import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.config.contextxmlservice.ServletManagerConfig;

import de.schlund.pfixxml.PfixServletRequest;

public class SessionHandlingTestHandler extends AbstractPustefixRequestHandler {

    Properties properties;
    ServletManagerConfig config;
    
    public SessionHandlingTestHandler(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public void init() throws ServletException {
        config = new SessionHandlingTestHandlerConfig(properties);
        super.init();
    }
    
    @Override
    public ServletManagerConfig getServletManagerConfig() {
        return config;
    }

    @Override
    public boolean needsSession() {
        return true;
    }

    @Override
    public boolean allowSessionCreate() {
        return true;
    }

    @Override
    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        res.setContentType("text/html");
        PrintWriter writer = res.getWriter();
        HttpSession session = preq.getSession(false);
        int count = 1;
        if(session != null) {
            Integer counter = (Integer)session.getAttribute("COUNTER");
            if(counter == null) counter = new Integer(1);
            else counter = Integer.valueOf(counter + 1);
            session.setAttribute("COUNTER", counter);
            count = counter;
        }
        writer.write("<html><head><title>test</title></head><body>test</body><!--" + count + "--></html>");
        writer.close();
    }
    
    @Override
    public boolean needsSSL(PfixServletRequest preq) throws ServletException {
        if(preq.getRequestParam("nossl") != null) return false;
        return true;
    }
}
