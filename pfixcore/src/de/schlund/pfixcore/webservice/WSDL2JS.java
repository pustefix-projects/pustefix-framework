/*
 * de.schlund.pfixcore.webservice.WSDL2JS
 */
package de.schlund.pfixcore.webservice;

import java.util.Iterator;

import javax.wsdl.*;
import javax.wsdl.factory.*;
import javax.wsdl.xml.*;

/**
 * WSDL2JS.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class WSDL2JS {
    
    public WSDL2JS() {
        test();
    }
    
    String portName;
    
    public void test() {
        try {
            WSDLFactory wf=WSDLFactory.newInstance();
            WSDLReader wr=wf.newWSDLReader();
            wr.setFeature("javax.wsdl.verbose",true);
            wr.setFeature("javax.wsdl.importDocuments",true);
            Definition def=wr.readWSDL(null,"webservice/gen/Calculator.wsdl");
            Iterator sit=def.getServices().values().iterator();
            while(sit.hasNext()) {
                Service service=(Service)sit.next();
                Iterator pit=service.getPorts().values().iterator();
                while(pit.hasNext()) {
                    Port port=(Port)pit.next();
                    portName=port.getName();
                    createClass();
                    Binding binding=port.getBinding();
                    Iterator oit=binding.getBindingOperations().iterator();
                    while(oit.hasNext()) {
                        BindingOperation bop=(BindingOperation)oit.next();
                        createMethod(bop);
                    }
                }
            }
        } catch(WSDLException x) {
            x.printStackTrace();
        }
    }
    
    private void createClass() {
        System.out.println("function "+portName+"() {}");
    }
    
    private void createMethod(BindingOperation bop) {
        System.out.println(portName+".prototype."+bop.getName()+"=function() {}");
    }
    
    public static void main(String args[]) {
        new WSDL2JS();
    }

}
