/*
 * de.schlund.pfixcore.webservice.generate.Wsdl2Js
 */
package de.schlund.pfixcore.webservice.generate;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.wsdl.*;
import javax.wsdl.factory.*;
import javax.wsdl.xml.*;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import de.schlund.pfixcore.webservice.generate.js.*;
import de.schlund.pfixcore.webservice.Constants;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.reader.SchemaReader;

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
    
    private Schema buildSchemaModel(Document doc) throws Exception {
        try {
            TransformerFactory tf=TransformerFactory.newInstance();
            Transformer t=tf.newTransformer();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            t.transform(new DOMSource(doc),new StreamResult(baos));
            baos.close();
            ByteArrayInputStream bais=new ByteArrayInputStream(baos.toByteArray());
            SchemaReader sr=new SchemaReader(new InputSource(bais));       
            Schema s=sr.read();
            bais.close();
            return s;
        } catch(Exception x) {
            throw new Exception("Error while building schema model",x);
        }
    }
    
    private Document extractSchemaDoc(Element elem) throws Exception {
        try {
            DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
            DocumentBuilder db=dbf.newDocumentBuilder();
            Document doc=db.newDocument();
            Element docElem=(Element)doc.importNode(elem,true);
            docElem.setAttribute("xmlns:soapenc",Constants.XMLNS_SOAPENC);
            NodeList nl=docElem.getElementsByTagName("import");
            //for(int i=0;i<nl.getLength();i++) ((Element)nl.item(i)).setAttribute("schemaLocation",Constants.XMLNS_SOAPENC);
            //ignore soapenc import
            for(int i=0;i<nl.getLength();i++) docElem.removeChild(nl.item(i));
            doc.appendChild(docElem);
            return doc;
        } catch(Exception x) {
            throw new Exception("Error while extracting schema element",x);
        }
    }
    
    public void test() {
        try {
            WSDLFactory wf=WSDLFactory.newInstance();
            WSDLReader wr=wf.newWSDLReader();
            wr.setFeature("javax.wsdl.verbose",true);
            wr.setFeature("javax.wsdl.importDocuments",true);
            Definition def=wr.readWSDL(null,"/home/mleidig/workspace/pfixcore_ws/example/servletconf/tomcat/webapps/webservice/wsdl/Calculator.wsdl");
            
            //get schema types from wsdl definitions
            ArrayList schemas=new ArrayList();
            if(def.getTypes()!=null) {
                Iterator xsdIt=def.getTypes().getExtensibilityElements().iterator();
                while(xsdIt.hasNext()) {
                    ExtensibilityElement exElem=(ExtensibilityElement)xsdIt.next();
                    if(exElem instanceof UnknownExtensibilityElement) {
                        Element elem=((UnknownExtensibilityElement)exElem).getElement();
                        Schema schema=buildSchemaModel(extractSchemaDoc(elem));
                        schemas.add(schema);
                    }
                }
            }
            
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
                            System.out.println(part.getTypeName());
                            
                        }   
                        JsBlock jsBlock=jsMethod.getBody();
                        jsBlock.addStatement(new JsStatement("var call=new Call()"));
                        JsParam[] jsParams=jsMethod.getParams();
                        for(int i=0;i<jsParams.length;i++) {
                            Part part=inputMsg.getPart(jsParams[i].getName());
                            QName type=part.getTypeName();
                            String str="";
                            if(type.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
                                str="new TypeInfo(new QName(\""+type.getNamespaceURI()+"\",\""+type.getLocalPart()+"\"))";
                            }
                            jsBlock.addStatement(new JsStatement("call.addParameter(\""+jsParams[i].getName()+"\","+str+")"));
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
