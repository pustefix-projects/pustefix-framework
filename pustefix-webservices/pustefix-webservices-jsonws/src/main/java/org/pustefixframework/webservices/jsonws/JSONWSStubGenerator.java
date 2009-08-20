/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.webservices.jsonws;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.ServiceDescriptor;
import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceStubGenerator;
import org.pustefixframework.webservices.config.WebserviceConfiguration;
import org.pustefixframework.webservices.jsgen.JsBlock;
import org.pustefixframework.webservices.jsgen.JsClass;
import org.pustefixframework.webservices.jsgen.JsMethod;
import org.pustefixframework.webservices.jsgen.JsParam;
import org.pustefixframework.webservices.jsgen.JsStatement;
import org.pustefixframework.webservices.spring.WebserviceRegistration;

/**
 * @author mleidig@schlund.de
 */
public class JSONWSStubGenerator implements ServiceStubGenerator {

    public void generateStub(WebserviceRegistration registration, WebserviceConfiguration configuration, OutputStream out) throws ServiceException,IOException {
        
    	ServiceDescriptor desc=new ServiceDescriptor(registration);
        JsParam[] constParams=new JsParam[] {new JsParam("context"),new JsParam("scope")};
       
        String jsClassName=null;
        String namespace = registration.getStubJSNamespace();
        if(namespace == null) namespace = configuration.getStubJSNamespace();
        if(namespace.equals(Constants.STUBGEN_JSNAMESPACE_COMPAT)) {
            jsClassName = Constants.STUBGEN_DEFAULT_JSNAMESPACE + registration.getServiceName();
        } else if(namespace.equals(Constants.STUBGEN_JSNAMESPACE_COMPATUNIQUE)) {
            jsClassName = Constants.STUBGEN_JSONWS_JSNAMESPACE + registration.getServiceName();
        } else if(namespace.equals(Constants.STUBGEN_JSNAMESPACE_JAVANAME)) {
            jsClassName = desc.getServiceClass().getName();
        } else {
            if(namespace.contains(".") && !namespace.endsWith(".")) namespace+=".";
            jsClassName = namespace + registration.getServiceName();
        }
        JsClass jsClass=new JsClass(jsClassName,"pfx.ws.json.BaseStub",constParams);
        JsBlock block=jsClass.getConstructorBody();
        block.addStatement(new JsStatement("pfx.ws.json.BaseStub.call(this,\""+registration.getServiceName()+"\",context,scope)"));
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
    
}
