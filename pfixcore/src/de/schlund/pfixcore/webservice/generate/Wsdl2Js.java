/*
 * de.schlund.pfixcore.webservice.generate.Wsdl2Js
 */
package de.schlund.pfixcore.webservice.generate;

import java.util.Iterator;

import javax.wsdl.*;
import javax.wsdl.factory.*;
import javax.wsdl.xml.*;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;

import de.schlund.pfixcore.webservice.generate.js.*;

/**
 * Wsdl2Js.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class Wsdl2Js {
    
    public Wsdl2Js() {
        test();
    }
    
    private SOAPAddress getSOAPAddress(Port port) {
        Iterator it=port.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPAddress) {
                SOAPAddress adr=(SOAPAddress)elem;
                return adr;
            }
        }
        return null;
    }
    
    private SOAPBinding getSOAPBinding(Binding binding) {
        Iterator it=binding.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPBinding) {
                SOAPBinding bind=(SOAPBinding)elem;
                return bind;
            }
        }
        return null;
    }
    
    public void test() {
        try {
            WSDLFactory wf=WSDLFactory.newInstance();
            WSDLReader wr=wf.newWSDLReader();
            wr.setFeature("javax.wsdl.verbose",true);
            wr.setFeature("javax.wsdl.importDocuments",true);
            Definition def=wr.readWSDL(null,"/home/mleidig/workspace/pfixcore_ws/example/servletconf/tomcat/webapps/webservice/wsdl/Calculator.wsdl");
            Iterator srvIt=def.getServices().values().iterator();
            while(srvIt.hasNext()) {
                Service service=(Service)srvIt.next();
                Iterator prtIt=service.getPorts().values().iterator();
                while(prtIt.hasNext()) {
                    Port port=(Port)prtIt.next();
                    String portName=port.getName();
                    JsClass jsClass=new JsClass(portName);
                    if(getSOAPAddress(port)==null) throw new Exception("No soap address binding found for port "+portName);
                    Binding binding=port.getBinding();
                    if(getSOAPBinding(binding)==null) throw new Exception("No soap binding found for binding "+binding.getQName());
                    PortType portType=binding.getPortType();
                    Iterator bopIt=binding.getBindingOperations().iterator();
                    while(bopIt.hasNext()) {
                        BindingOperation bop=(BindingOperation)bopIt.next();
                        Operation op=bop.getOperation();
                       
                        JsMethod jsMethod=new JsMethod(jsClass,op.getName());
                        jsClass.addMethod(jsMethod);
                       
                        Input input=op.getInput();
                        Message inputMsg=input.getMessage();
                        Iterator partIt=inputMsg.getOrderedParts(op.getParameterOrdering()).iterator();
                        while(partIt.hasNext()) {
                            Part part=(Part)partIt.next();
                            JsParam jsParam=new JsParam(part.getName());
                            jsMethod.addParam(jsParam);
                            //System.out.println(part.getName());
                            //System.out.println(part.getTypeName());
                        }   
                        JsBlock jsBlock=jsMethod.getBody();
                        jsBlock.addStatement(new JsStatement("var call=new Call()"));
                        JsParam[] jsParams=jsMethod.getParams();
                        for(int i=0;i<jsParams.length;i++) {
                            jsBlock.addStatement(new JsStatement("call.addParameter(\""+jsParams[i].getName()+"\")"));
                        }
                    }
                    jsClass.printCode(System.out);
                    
                }
                
             
                 /**   
                    Iterator oit=binding.getBindingOperations().iterator();
                    while(oit.hasNext()) {
                        BindingOperation bop=(BindingOperation)oit.next();
                        createMethod(bop);
                    }
                }
                */
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
    }
    
    
    
    
      
   
 
    
    public static void main(String args[]) {
        new Wsdl2Js();
    }

}
