package de.schlund.pfixxml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JmxPortServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String port=System.getProperty("com.sun.management.jmxremote.port");
       if(port==null) port="";
       response.getOutputStream().print(port);
    }

}
