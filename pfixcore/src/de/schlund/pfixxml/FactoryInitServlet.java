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

package de.schlund.pfixxml;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import de.schlund.pfixcore.util.*;
import de.schlund.util.FactoryInit;
import de.schlund.util.statuscodes.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.xml.*;

/**
 * This Servlet is just there to have it's init method called on startup of the VM.
 * It starts all VM-global factories.
 *
 *
 */

public class FactoryInitServlet extends HttpServlet {
    private Object   LOCK       = new Object();
    private Category CAT        = Category.getInstance(FactoryInitServlet.class.getName());
    private static   boolean  configured = false;

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        throw new ServletException("This servlet can't be called interactively");
    }

    public void init(ServletConfig Config) throws ServletException {
        super.init(Config);
        Properties properties = new Properties(System.getProperties());

        String confname = Config.getInitParameter("servlet.propfile");

        if (confname != null) {
            try {
                properties.load(new FileInputStream(confname));
            } catch (FileNotFoundException e) {
                throw new ServletException("*** [" + confname + "] Not found: " + e.toString());
            } catch (IOException e) {
                throw new ServletException("*** [" + confname + "] IO-error: " + e.toString());
            }
        }

        synchronized (LOCK) {
            if (!configured) {
                if (properties != null) {
                    String log4jconfig = properties.getProperty("pustefix.log4j.config");
                    if (log4jconfig == null & log4jconfig.equals("")) {
                        throw new ServletException ("*** FATAL: Need the pustefix.log4j.config property... ***");
                    }
                    DOMConfigurator.configure(log4jconfig);
                }

                CAT.debug(">>>> LOG4J Init OK <<<<");
            
                HashMap to_init = PropertiesUtils.selectProperties(properties, "factory.initialize");
                if (to_init != null) {
                    
                    // sort key to initialize the factories in defined order
                    TreeSet keyset = new TreeSet(to_init.keySet());
                    
                    for (Iterator i = keyset.iterator(); i.hasNext(); ) {
                        String key       = (String) i.next();
                        String the_class = (String) to_init.get(key);
                        
                        try {
                            CAT.debug(">>>> Init key: [" + key + "] class: [" + the_class + "] <<<<" );
                            FactoryInit factory = (FactoryInit) Class.forName(the_class).
                                getMethod("getInstance", null).invoke(null, null);
                            CAT.debug("     Object ID: " + factory);
                            factory.init(properties);
                        } catch (Exception e) {
                            CAT.error(e.toString());
                            ThrowableInformation info=new ThrowableInformation(e);
                            String[] trace=info.getThrowableStrRep();
                            StringBuffer strerror=new StringBuffer();
                            for(int ii=0; ii<trace.length; ii++) {
                                strerror.append("->"+trace[ii]+"\n");
                            }
                            CAT.error(strerror.toString());
                            // mk: If you run into an  InvocationTargetException and
                            // if you see a different class name here,
                            // one cause is a missing or wrong package declaration of your singleton.
                            // To see  the following debug messages, look in the file pfixlog.xml
                            // for "FactoryInitServlet" and set the priority to debug
                            CAT.debug("FactoryInitServlet: class=" + e.getClass());
                            throw new ServletException(e.toString());
                        }
                    }
                }
            }
            configured = true;
            CAT.debug("***** INIT of FactoryInitServlet done *****");
        }
    }
}
