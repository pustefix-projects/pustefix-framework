/*
 * de.schlund.pfixcore.webservice.generate.Wsdl2Js
 */
package de.schlund.pfixcore.webservice.generate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.wsdl.*;
import javax.wsdl.factory.*;
import javax.wsdl.xml.*;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;

import de.schlund.pfixcore.webservice.generate.js.*;
import de.schlund.pfixcore.webservice.Constants;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.apache.wsif.schema.Parser;
import org.apache.wsif.schema.SchemaType;
import org.apache.wsif.schema.SequenceElement;
import org.apache.wsif.schema.ComplexType;


/**
 * Wsdl2Js.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class Wsdl2Js {
    
    public static final String JSPREFIX_WS="WS_";
    
    private File outputFile;
    private File inputFile;
    
    private HashMap schemaTypes;
    private HashMap typeInfoMap;
    private ArrayList typeInfoList;
    private ArrayList popInfoList;
    
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
    
    private SOAPBody getSOAPBody(BindingInput binding) {
        Iterator it=binding.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPBody) {
                SOAPBody body=(SOAPBody)elem;
                return body;
            }
        }
        return null;
    }
    
    private SOAPBody getSOAPBody(BindingOutput binding) {
        Iterator it=binding.getExtensibilityElements().iterator();
        while(it.hasNext()) {
            ExtensibilityElement elem=(ExtensibilityElement)it.next();
            if(elem instanceof SOAPBody) {
                SOAPBody body=(SOAPBody)elem;
                return body;
            }
        }
        return null;
    }
    
    
    
    private Document extractSchemaDoc(Element elem) throws Exception {
        try {
            DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
            DocumentBuilder db=dbf.newDocumentBuilder();
            Document doc=db.newDocument();
            Element docElem=(Element)doc.importNode(elem,true);
            docElem.setAttribute("xmlns:xsd",Constants.XMLNS_XSD);
            docElem.setAttribute("xmlns:wsdl",Constants.XMLNS_WSDL);
            docElem.setAttribute("xmlns:soapenc",Constants.XMLNS_SOAPENC);
            docElem.setAttribute("xmlns:apachesoap",Constants.XMLNS_APACHESOAP);
            NodeList nl=docElem.getElementsByTagName("import");
            for(int i=0;i<nl.getLength();i++) ((Element)nl.item(i)).setAttribute("schemaLocation","file:///tmp/encoding.xsd");
            //for(int i=0;i<nl.getLength();i++) ((Element)nl.item(i)).setAttribute("schemaLocation",Constants.XMLNS_SOAPENC);
            //ignore soapenc import
            //for(int i=0;i<nl.getLength();i++) docElem.removeChild(nl.item(i));
            doc.appendChild(docElem);
            return doc;
        } catch(Exception x) {
            throw new Exception("Error while extracting schema element",x);
        }
    }
    
    public void generate() throws Exception {
        
            WSDLFactory wf=WSDLFactory.newInstance();
            WSDLReader wr=wf.newWSDLReader();
            wr.setFeature("javax.wsdl.verbose",true);
            wr.setFeature("javax.wsdl.importDocuments",true);
            
            if(inputFile==null) throw new Exception("No WSDL input file specified");
            if(outputFile==null) throw new Exception("No JS output file specified");
            if(!inputFile.exists()) throw new Exception("WSDL input file doesn't exist");
            InputSource inSrc=new InputSource(new FileInputStream(inputFile));
            Definition def=wr.readWSDL(null,inSrc);
            
            //get schema types from wsdl definition
            ArrayList list=new ArrayList();
            schemaTypes=new HashMap();
            typeInfoMap=new HashMap();
            popInfoList=new ArrayList();
            typeInfoList=new ArrayList();
            Parser.getAllSchemaTypes(def,list,null);
            Iterator listIt=list.iterator();
            while(listIt.hasNext()) {
                SchemaType st=(SchemaType)listIt.next();
                schemaTypes.put(st.getTypeName(),st);
            }
            
            Iterator srvIt=def.getServices().values().iterator();
            while(srvIt.hasNext()) {
                Service service=(Service)srvIt.next();
                Iterator prtIt=service.getPorts().values().iterator();
                while(prtIt.hasNext()) {
                    Port port=(Port)prtIt.next();
                    String portName=port.getName();
                    
                    SOAPAddress soapAdr=getSOAPAddress(port);
                    if(soapAdr==null) throw new Exception("No soap address binding found for port "+portName);
                    
                    String stubClass=JSPREFIX_WS+portName;
                    
                    JsClass jsClass=new JsClass(stubClass,"SOAP_Stub");
                    JsBlock block=jsClass.getConstructorBody();
                    block.addStatement(new JsStatement("this._setURL(\""+soapAdr.getLocationURI()+"\")"));
                    
                    Binding binding=port.getBinding();
                    SOAPBinding soapBinding=getSOAPBinding(binding);
                    if(soapBinding==null) throw new Exception("No soap binding found for binding "+binding.getQName());
                    String style=soapBinding.getStyle();
                    
                    PortType portType=binding.getPortType();
                    Iterator bopIt=binding.getBindingOperations().iterator();
                    while(bopIt.hasNext()) {
                        BindingOperation bop=(BindingOperation)bopIt.next();
                        Operation op=bop.getOperation();
                       
                        SOAPBody soapBody=getSOAPBody(bop.getBindingInput());
                        if(soapBody==null) throw new Exception("No soap binding found for operation "+bop.getName());
                        String use=soapBody.getUse();
                        
                        JsMethod jsMethod=new JsMethod(jsClass,op.getName());
                        jsClass.addMethod(jsMethod);
                       
                        //get input params
                        Input input=op.getInput();
                        Message inputMsg=input.getMessage();
                        Iterator partIt=inputMsg.getOrderedParts(op.getParameterOrdering()).iterator();
                        while(partIt.hasNext()) {
                            Part part=(Part)partIt.next();
                            JsParam jsParam=new JsParam(part.getName());
                            jsMethod.addParam(jsParam);     
                        }   
                        
                       
                        
                        JsBlock jsBlock=jsMethod.getBody();
                        jsBlock.addStatement(new JsStatement("var call=this._createCall()"));
                        jsBlock.addStatement(new JsStatement("this._extractCallback(call,arguments,"+jsMethod.getParams().length+")"));
                        jsBlock.addStatement(new JsStatement("call.setEncoding(\""+style+"\",\""+use+"\")"));
                        jsBlock.addStatement(new JsStatement("call.setOperationName(\""+jsMethod.getName()+"\")"));
                        JsParam[] jsParams=jsMethod.getParams();
                        for(int i=0;i<jsParams.length;i++) {
                            Part part=inputMsg.getPart(jsParams[i].getName());
                            QName type=part.getTypeName();
                            String info=createJsTypeInfo(type);
                            jsBlock.addStatement(new JsStatement("call.addParameter(\""+jsParams[i].getName()+"\","+info+")"));
                        }
                        
                        //get return param (presume only one return param)
                        Output output=op.getOutput();
                        Message outputMsg=output.getMessage();
                        partIt=outputMsg.getParts().values().iterator();
                        while(partIt.hasNext()) {
                            Part part=(Part)partIt.next();
                            QName type=part.getTypeName();
                            String info=createJsTypeInfo(type);
                            jsBlock.addStatement(new JsStatement("call.setReturnType("+info+")"));
                        }
                        
                        
                        jsBlock.addStatement(new JsStatement("return call.invoke("+jsMethod.getParamList()+")"));
                        
                        
                    }
                    
                    
                    for(int i=0;i<typeInfoList.size();i++) {
                        block.addStatement(new JsStatement("this._typeInfos["+i+"]="+(String)typeInfoList.get(i)));
                    }
                    for(int i=0;i<popInfoList.size();i++) {
                        block.addStatement(new JsStatement((String)popInfoList.get(i)));
                    }
                    
                    jsClass.printCode(new FileOutputStream(outputFile));
                    
                }
                
            }
        
    }
    
    
    private String createJsTypeInfo(QName type) {
        String ret=(String)typeInfoMap.get(type);
        if(ret==null) {
            int ind=typeInfoList.size();
            ret="this._typeInfos["+ind+"]";
            typeInfoMap.put(type,ret);
            String info="\"\"";
            typeInfoList.add(info);
            if(type.getNamespaceURI().equals(Constants.XMLNS_XSD)||type.getNamespaceURI().equals(Constants.XMLNS_SOAPENC)||
                    type.getNamespaceURI().equals(Constants.XMLNS_APACHESOAP)) {
                info="new SOAP_TypeInfo("+createJsQName(type)+")";
            } else {
                SchemaType stype=(SchemaType)schemaTypes.get(type);
                if(stype!=null) {
                    if(stype.isComplex()) {
                        ComplexType ctype=(ComplexType)stype;
                        if(ctype.isArray()) {
                            String qn=createJsQName(ctype.getTypeName());
                            String ainf=createJsTypeInfo(ctype.getArrayType());
                            String pop=ret+".populate("+ainf+","+ctype.getArrayDimension()+")";
                            popInfoList.add(pop);
                            info="new SOAP_ArrayInfo("+qn+")";
                        } else if(true) {
                            info="new SOAP_BeanInfo("+createJsQName(type)+")";
                            String pop=ret+".populate(new Array(";
                            SequenceElement[] elems=ctype.getSequenceElements();
                            for(int i=0;i<elems.length;i++) {
                                QName propType=elems[i].getElementType();
                                String propInfo=createJsTypeInfo(propType);
                                pop+="\""+elems[i].getTypeName().getLocalPart()+"\","+propInfo;
                                if(i<elems.length-1) pop+=",";
                            }
                            pop+="))";
                            popInfoList.add(pop);
                        }
                    } else {
                        info="new SOAP_TypeInfo("+createJsQName(type)+")";
                    }
                }
            }
            typeInfoList.set(ind,info);
        }
        return ret; 
    }
   
    private String createJsQName(QName name) {
        String nsuri="";
        if(name.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
            nsuri="XML_NS_XSD";
        } else if(name.getNamespaceURI().equals(Constants.XMLNS_SOAPENC)) {
            nsuri="XML_NS_SOAPENC";
        } else nsuri="\""+name.getNamespaceURI()+"\"";
        return "new XML_QName("+nsuri+",\""+name.getLocalPart()+"\")";
    }
    
    public void setInputFile(File inputFile) {
        this.inputFile=inputFile;
    }
    
    public void setOutputFile(File outputFile) {
        this.outputFile=outputFile;
    }
    
}
