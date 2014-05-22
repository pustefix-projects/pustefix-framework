package org.pustefixframework.webservices.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceStubGenerator;
import org.pustefixframework.webservices.config.ServiceConfig;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class JAXWSStubGenerator implements ServiceStubGenerator {
    
    public void generateStub(ServiceConfig service, String requestPath, OutputStream out) throws ServiceException, IOException {
        
        Resource res = ResourceUtil.getResource("/wsscript/" + service.getName() + ".js");
        if(res != null && res.exists()) {
            InputStream in = res.getInputStream();
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            StringBuffer strBuf = new StringBuffer();
            char[] buffer = new char[4096];
            int i = 0;
            try {
                while ((i = reader.read(buffer)) != -1)
                    strBuf.append(buffer, 0, i);
            } finally {
                in.close();
            }
            String str = strBuf.toString();
            str = str.replaceAll("###REQUESTPATH###", requestPath);
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            try {
                writer.write(str);
                writer.flush();
            } finally {
                out.close();
            }
        } else throw new ServiceException("Missing WSDL for service " + service.getName());
    }

}
