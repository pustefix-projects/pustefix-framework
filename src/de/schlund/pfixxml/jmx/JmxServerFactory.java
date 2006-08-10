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

package de.schlund.pfixxml.jmx;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.FactoryInitServlet;

/** 
 * Configures and starts JmxServer
 */
public class JmxServerFactory {
    public static String PROP_DEFAULT_PORT = "de.schlund.pfixcore.jmx.port.default";
    public static String PROP_CONF_PORT_PREFIX = "de.schlund.pfixcore.jmx.port";
    public static String PROP_CONF_REGEX_PREFIX = "de.schlund.pfixcore.jmx.regex";
    public static String PROP_USE_KEYSTORE = "de.schlund.pfixcore.jmx.keystore";
    public static String PROP_SERVERNAME = "de.schlund.pfixcore.jmx.servername";
    
    private static Logger LOG = Logger.getLogger(JmxServerFactory.class);
    
    private static JmxServerFactory instance = new JmxServerFactory();
    
    public static JmxServerFactory getInstance() {
        return instance;
    }
    
    public static JmxServer getServer() {
    	if (instance.server == null) {
            throw new RuntimeException();
    	}
    	return instance.server;
    }
    
    private JmxServer server;
    private int port;
    private String host;
    
    public JmxServerFactory() {
    	server = null;
    }
    
    public void init(Properties props) throws Exception {
        host = getHost(props);
        port = getPort(props);
        if(port <= 0) {
            LOG.error("Got port <=0["+port+"]. Not starting jmx-server!");
        } else {
            
            String keystore = props.getProperty(PROP_USE_KEYSTORE, null);
            LOG.info("Trying to start jmx-server on "+host+":"+port+" using keystore:"+keystore);
            server = new JmxServer();
            server.start(host, port, keystore);
            
            LOG.info("jmx-server started");
        }
    }
    
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    private static String getHost(Properties props) throws UnknownHostException {
        
        String machine = props.getProperty(PROP_SERVERNAME, null);
        InetAddress ret = null; 
        
        if(machine == null) {
            ret = InetAddress.getLocalHost();
            LOG.debug("Found no property ["+PROP_SERVERNAME+"]. Using: "+ret.getCanonicalHostName());
        } else {
            ret = InetAddress.getByName(machine);
            LOG.debug("Found property ["+PROP_SERVERNAME+"]. Using: "+ret.getCanonicalHostName());
        }
       
        LOG.debug("Hostname is: "+ret.getHostName());
        return ret.getHostName();
    }
    
    private static int getPort(Properties props) {
        
        String defaultport = props.getProperty(PROP_DEFAULT_PORT, null);
        int ret =  -1;
        if(defaultport != null) {
            ret = Integer.parseInt(defaultport);
            LOG.debug("Found defaultport ["+ret+"]. Using it.");
        } else {
            ret = getConfiguredPort(props);
            LOG.debug("No defaultport. Configured port: ["+ret+"]. Using it.");
        }
        
        return ret;
    }

    private static int getConfiguredPort(Properties props) {
        TreeMap ptm = PropertiesUtils.selectPropertiesSorted(props, PROP_CONF_PORT_PREFIX);
        TreeMap rtm = PropertiesUtils.selectPropertiesSorted(props, PROP_CONF_REGEX_PREFIX);
        
        String docroot = props.getProperty(FactoryInitServlet.PROP_DOCROOT, null);
        int ret = -1;
        if(docroot == null) {
            throw new IllegalArgumentException("Unable to get docroot from properties");
        }
        if(!( ptm.size() !=  rtm.size())) {
        
            while (!ptm.isEmpty()) {
                Object f1 = ptm.firstKey();
                Object f2 = rtm.firstKey();
            
                String port =(String)ptm.get(f1);
                String reg =(String)rtm.get(f2);
                
                if(doesMatchDocroot(reg, docroot)) {
                    ret = Integer.parseInt(port);
                    LOG.info("Bingo! Reg ["+reg+"] matches ["+docroot+"]. Using port: "+ret);
                    break;
                }
                ptm.remove(f1);
                rtm.remove(f2);
            }
        } else {
            LOG.error(ptm.size() +"!="+rtm.size());
        }
        
        return ret;
    }

    private static boolean doesMatchDocroot(String reg, String docroot) {
        PatternMatcher p5m = new Perl5Matcher();
        boolean ret = false;
        try {
            ret = p5m.matches(docroot, new Perl5Compiler().compile(reg));
        } catch (MalformedPatternException e) {
            LOG.error("Unable to compile pattern ["+reg+"]"+e);
        }
        LOG.debug("Trying if ["+reg+"] matches ["+docroot+"]: "+ret);
        return ret;
    }
}
