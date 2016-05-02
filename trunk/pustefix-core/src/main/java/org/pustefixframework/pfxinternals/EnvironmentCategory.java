package org.pustefixframework.pfxinternals;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;

import de.schlund.pfixxml.config.EnvironmentProperties;

public class EnvironmentCategory implements Category {

    @Override
    public void model(Element parent, HttpServletRequest request, PageContext context) {

        Element envElem = parent.getOwnerDocument().createElement("environment");
        parent.appendChild(envElem);
        Element propsElem = parent.getOwnerDocument().createElement("properties");
        envElem.appendChild(propsElem);
        Properties props = EnvironmentProperties.getProperties();
        Element elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "fqdn");
        elem.setTextContent(props.getProperty("fqdn"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "machine");
        elem.setTextContent(props.getProperty("machine"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "mode");
        elem.setTextContent(props.getProperty("mode"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "uid");
        elem.setTextContent(props.getProperty("uid"));
        propsElem.appendChild(elem);
        elem = parent.getOwnerDocument().createElement("property");
        elem.setAttribute("name", "logroot");
        elem.setTextContent(props.getProperty("logroot"));
        propsElem.appendChild(elem);
        
        Element sysPropsElem = parent.getOwnerDocument().createElement("system-properties");
        parent.appendChild(sysPropsElem);
        RuntimeMXBean mbean = ManagementFactory.getRuntimeMXBean();
        Map<String, String> sysProps = mbean.getSystemProperties();
        for(String sysPropKey: sysProps.keySet()) {
            String sysPropVal = sysProps.get(sysPropKey);
            Element sysPropElem = parent.getOwnerDocument().createElement("property");
            sysPropElem.setAttribute("name", sysPropKey);
            sysPropElem.setTextContent(sysPropVal);
            sysPropsElem.appendChild(sysPropElem);
        }
    }

}
