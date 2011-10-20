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

package de.schlund.pfixcore.webservice.jsonws;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import de.schlund.pfixcore.example.webservices.CallTest;
import de.schlund.pfixcore.example.webservices.CallTestImpl;
import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.ServiceDescriptor;
import de.schlund.pfixcore.webservice.ServiceException;
import de.schlund.pfixcore.webservice.ServiceStubGenerator;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.generate.js.JsBlock;
import de.schlund.pfixcore.webservice.generate.js.JsClass;
import de.schlund.pfixcore.webservice.generate.js.JsMethod;
import de.schlund.pfixcore.webservice.generate.js.JsParam;
import de.schlund.pfixcore.webservice.generate.js.JsStatement;

/**
 * @author mleidig@schlund.de
 */
public class JSONWSStubGenerator implements ServiceStubGenerator {

    public void generateStub(ServiceConfig service,OutputStream out) throws ServiceException,IOException {
        ServiceDescriptor desc=new ServiceDescriptor(service);
        JsParam[] constParams=new JsParam[] {new JsParam("context")};
       
        String jsClassName=null;
        if(service.getStubJSNamespace().equals(Constants.STUBGEN_JSNAMESPACE_COMPAT)) {
            jsClassName=Constants.STUBGEN_DEFAULT_JSNAMESPACE+service.getName();
        } else if(service.getStubJSNamespace().equals(Constants.STUBGEN_JSNAMESPACE_COMPATUNIQUE)) {
            jsClassName=Constants.STUBGEN_JSONWS_JSNAMESPACE+service.getName();
        } else if(service.getStubJSNamespace().equals(Constants.STUBGEN_JSNAMESPACE_JAVANAME)) {
            jsClassName=desc.getServiceClass().getName();
        } else {
            String prefix=service.getStubJSNamespace();
            if(prefix.contains(".")&&!prefix.endsWith(".")) prefix+=".";
            jsClassName=prefix+service.getName();
        }
        JsClass jsClass=new JsClass(jsClassName,"pfx.ws.json.BaseStub",constParams);
        JsBlock block=jsClass.getConstructorBody();
        block.addStatement(new JsStatement("pfx.ws.json.BaseStub.call(this,\""+service.getName()+"\",context)"));
        for(String methName:desc.getMethods()) {
            List<Method> meths=desc.getMethods(methName);
            for(Method meth:meths) {
                JsMethod jsMeth=new JsMethod(jsClass,meth.getName());
                jsClass.addMethod(jsMeth);
                JsBlock jsBody=jsMeth.getBody();
                jsBody.addStatement(new JsStatement("return this.callMethod(\""+meth.getName()+"\",arguments,"+meth.getParameterTypes().length+")"));
            }
        }
        jsClass.printCode(out);
    }
    
    public static void main(String[] args) throws Exception {
        ServiceConfig config=new ServiceConfig(null);
        config.setInterfaceName(CallTest.class.getName());
        config.setImplementationName(CallTestImpl.class.getName());
        config.setName("Test");
        config.setStubJSNamespace(Constants.STUBGEN_JSNAMESPACE_COMPAT);
        //config.setStubJSNamespace(Constants.STUBGEN_JSNAMESPACE_COMPATUNIQUE);
        //config.setStubJSNamespace(Constants.STUBGEN_JSNAMESPACE_JAVANAME);
        //config.setStubJSNamespace("My");
        //config.setStubJSNamespace("foo.");
        //config.setStubJSNamespace("foo.bar");
        (new JSONWSStubGenerator()).generateStub(config,System.out);
    }
    
}