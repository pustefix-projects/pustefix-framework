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
package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.pustefixframework.webservices.Constants;
import org.pustefixframework.webservices.jsgen.JsBlock;
import org.pustefixframework.webservices.jsgen.JsClass;
import org.pustefixframework.webservices.jsgen.JsMethod;
import org.pustefixframework.webservices.jsgen.JsParam;
import org.pustefixframework.webservices.jsgen.JsStatement;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.wsdl.util.xml.DOMUtils;

/**
 * This class generates a Javascript stub file from a WSDL description.
 *
 * @author mleidig@schlund.de
 */
public class Wsdl2Js {

    private final static Pattern PREFIXPATTERN=Pattern.compile("(\\w+):(\\w+)");
    private final static String XMLNS_SCHEMA="http://www.w3.org/2001/XMLSchema";
    private final static String XMLNS_JAXB_ARRAY="http://jaxb.dev.java.net/array";
    
    private File outputFile;
    private File inputFile;
    
    private Map<QName,Element> schemaComplexTypes;
    private Map<QName,QName> schemaElementsToTypes;
    
    private HashMap<QName,String> typeInfoMap;
    private ArrayList<String> typeInfoList;
    private ArrayList<String> popInfoList;
    
    /**
     * Set WSDL input file
     */
    public void setInputFile(File inputFile) {
        this.inputFile=inputFile;
    }
    
    /**
     * Set Javascript output file
     */
    public void setOutputFile(File outputFile) {
        this.outputFile=outputFile;
    }
    
    /**
     * Generate Javascript stub file from WSDL file 
     */
    public void generate() throws Wsdl2JsException, IOException {
        if(inputFile==null) throw new Wsdl2JsException("No WSDL input file specified");
        if(outputFile==null) throw new Wsdl2JsException("No JS output file specified");
        if(!inputFile.exists()) throw new Wsdl2JsException("WSDL input file doesn't exist");
        Definition def=null;
        try {
            WSDLFactory wf=WSDLFactory.newInstance();
            WSDLReader wr=wf.newWSDLReader();
            wr.setFeature("javax.wsdl.verbose",false);
            wr.setFeature("javax.wsdl.importDocuments",true);
            InputSource inSrc=new InputSource(new FileInputStream(inputFile));
            def=wr.readWSDL(inputFile.getParentFile().toURI().toString(),inSrc);
        } catch(Exception x) {
            throw new Wsdl2JsException("Error reading WSDL from: "+inputFile.getAbsolutePath(),x);
        }      
        typeInfoMap=new HashMap<QName,String>();
        popInfoList=new ArrayList<String>();
        typeInfoList=new ArrayList<String>();    
        readSchema(def.getTypes());    
        Iterator<?> srvIt=def.getServices().values().iterator();
        if(srvIt.hasNext()) {
            Service service=(Service)srvIt.next();
            Iterator<?> prtIt=service.getPorts().values().iterator();
            if(prtIt.hasNext()) {  
                Port port=(Port)prtIt.next();   
                String stubClass=Constants.STUBGEN_DEFAULT_JSNAMESPACE+service.getQName().getLocalPart();
                JsParam[] constParams=new JsParam[] {new JsParam("cbObj")};
                JsClass jsClass=new JsClass(stubClass,"SOAP_Stub",constParams);
                JsBlock block=jsClass.getConstructorBody();
                block.addStatement(new JsStatement("this._cbObj=cbObj"));
                block.addStatement(new JsStatement("this._setService(\""+service.getQName().getLocalPart()+"\")"));
                block.addStatement(new JsStatement("this._setRequestPath(\"###REQUESTPATH###\")"));
                Binding binding=port.getBinding();
                Iterator<?> bopIt=binding.getBindingOperations().iterator();
                while(bopIt.hasNext()) {
                    BindingOperation bop=(BindingOperation)bopIt.next();
                    Operation op=bop.getOperation();
                    try {
                        JsMethod jsMethod=new JsMethod(jsClass,op.getName());
                        JsBlock jsBlock=jsMethod.getBody();
                        jsBlock.addStatement(new JsStatement("var call=this._createCall()"));
                        jsBlock.addStatement(new JsStatement("call.setTargetNamespace(this._targetNamespace)"));
                        jsBlock.addStatement(new JsStatement("call.setOperationName(\""+jsMethod.getName()+"\")"));
                        //get input params
                        Input input=op.getInput();
                        Message inputMsg=input.getMessage();
                        Part inputPart=inputMsg.getPart("parameters");
                        if(inputPart!=null) {
                            QName typeName=schemaElementsToTypes.get(inputPart.getElementName());
                            Element typeElem=schemaComplexTypes.get(typeName);
                            Element[] children=getSchemaChildren(typeElem);
                            if(children.length>1 || (children.length==1 && !children[0].getLocalName().equals("sequence"))) 
                                throw new Wsdl2JsException("Expected only single sequence child element for complexType: "+typeName);
                            children=getSchemaChildren(children[0]);
                            for(int paraNo=0;paraNo<children.length;paraNo++) {
                                if(!children[paraNo].getLocalName().equals("element")) 
                                    throw new Wsdl2JsException("Expected only element children within sequence of complexType: "+typeName);
                                String name=children[paraNo].getAttribute("name");
                                JsParam jsParam=new JsParam(name);
                                jsMethod.addParam(jsParam);     
                                String info=createTypeInfo(children[paraNo]);
                                jsBlock.addStatement(new JsStatement("call.addParameter(\""+name+"\","+info+")"));
                            }
                        }   
                        jsBlock.addStatement(new JsStatement("this._extractCallback(call,arguments,"+jsMethod.getParams().length+")"));
                        //get return param
                        Output output=op.getOutput();
                        Message outputMsg=output.getMessage();
                        Part outputPart=outputMsg.getPart("parameters");
                        if(outputPart!=null) {
                            QName typeName=schemaElementsToTypes.get(outputPart.getElementName());
                            Element typeElem=schemaComplexTypes.get(typeName);
                            Element[] children=getSchemaChildren(typeElem);
                            if(children.length>1 || (children.length==1 && !children[0].getLocalName().equals("sequence"))) 
                                throw new Wsdl2JsException("Expected only single sequence child element for complexType: "+typeName);
                            children=getSchemaChildren(children[0]);
                            if(children.length>1) throw new Wsdl2JsException("Expected only one or none return parameters");
                            if(children.length==1) {
                                String info=createTypeInfo(children[0]);
                                jsBlock.addStatement(new JsStatement("call.setReturnType("+info+")"));
                            }
                        }
                        jsBlock.addStatement(new JsStatement("return call.invoke("+jsMethod.getParamList()+")"));
                        jsClass.addMethod(jsMethod);
                    } catch(Wsdl2JsTypeException x) {
                        System.out.println("WARNING: Skip method '"+op.getName()+"' as signature "+
                                "contains unsupported type '"+x.getTypeName()+"'.");
                    }
                }
                block.addStatement(new JsStatement("this._targetNamespace=\""+def.getTargetNamespace()+"\""));
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
    
    /**
     * Create type description JS code for the the given schema element
     */
    private String createTypeInfo(Element element) throws Wsdl2JsException {
        if(!(element.getNamespaceURI().equals(XMLNS_SCHEMA) && element.getLocalName().equals("element")))
            throw new IllegalArgumentException("Expected element declaration as argument");
        if(isElementArray(element)) {
            ArrayInfo arrayInfo=getArrayInfoFromElement(element);
            QName compType=arrayInfo.arrayType;
            int dim=arrayInfo.arrayDim;
            String name=compType.getLocalPart();
            for(int i=0;i<dim;i++) name+="Array";
            String nsuri=compType.getNamespaceURI();
            if(compType.getNamespaceURI().equals(XMLNS_SCHEMA)) nsuri=XMLNS_JAXB_ARRAY;
            QName arrType=new QName(nsuri,name);
            String ret=typeInfoMap.get(arrType);
            if(ret==null) {
                int ind=typeInfoList.size();
                String info="\"\"";
                typeInfoList.add(info);
                ret="this._typeInfos["+ind+"]";
                typeInfoMap.put(arrType,ret);
                String ainf=createJsTypeInfo(arrayInfo.arrayType);
                String pop=ret+".populate("+ainf+","+arrayInfo.arrayDim+")";
                popInfoList.add(pop);
                String qn=createJsQName(arrType);
                info="new SOAP_ArrayInfo("+qn+")";
                typeInfoList.set(ind,info);
            }
            return ret;
        } else {
           
            String type=element.getAttribute("type");
            QName qname=getQName(type,element);
          
            return createJsTypeInfo(qname);
        }
    }
    
    /**
     * Create type description JS code for the the given schema type
     */
    private String createJsTypeInfo(QName type) throws Wsdl2JsException {
        String ret=typeInfoMap.get(type);
        if(ret==null) {
            int ind=typeInfoList.size();
            ret="this._typeInfos["+ind+"]";
            typeInfoMap.put(type,ret);
            String info="\"\"";
            typeInfoList.add(info);
            if(type.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
                info="new SOAP_TypeInfo("+createJsQName(type)+")";
            } else if(type.getLocalPart().equals("hashMap")) {
                throw new Wsdl2JsTypeException(type);
            } else {
                Element ctypeElem=(Element)schemaComplexTypes.get(type);
                if(ctypeElem!=null) {
                	if(isComplexTypeArray(ctypeElem)) {
                		String qn=createJsQName(type);
                        ArrayInfo arrayInfo=getArrayInfoFromComplexType(ctypeElem);
                		String ainf=createJsTypeInfo(arrayInfo.arrayType);
                		String pop=ret+".populate("+ainf+","+arrayInfo.arrayDim+")";
                		popInfoList.add(pop);
                		info="new SOAP_ArrayInfo("+qn+")";
                	} else {
                		info="new SOAP_BeanInfo("+createJsQName(type)+")";
                		String pop=ret+".populate(new Array(";
                		String memberList=getMemberInfos(ctypeElem);
                		pop+=memberList;
                		pop+="))";
                		popInfoList.add(pop);
                    }
                } else throw new Wsdl2JsException("Type '"+type+"' not found within WSDL types section."+" ->"+type.getNamespaceURI());
            }
            typeInfoList.set(ind,info);
        }
        return ret; 
    }

    /**
     * Create QName instance JS code (using JS constant for schema namespace)
     */
    private String createJsQName(QName name) {
        String nsuri="";
        if(name.getNamespaceURI().equals(Constants.XMLNS_XSD)) nsuri="XML_NS_XSD";
        else nsuri="\""+name.getNamespaceURI()+"\"";
        return "new XML_QName("+nsuri+",\""+name.getLocalPart()+"\")";
    }
  
    /**
     * Stores top-level schema elements and complexTypes in maps (for all imported schemas)
     */
    private void readSchema(Types wsdlTypes) throws Wsdl2JsException {
        schemaComplexTypes = new HashMap<QName, Element>();
        schemaElementsToTypes = new HashMap<QName, QName>();
        if (wsdlTypes != null) {
            List<?> schemaList = wsdlTypes.getExtensibilityElements();
            Iterator<?> schemaIt = schemaList.iterator();
            while (schemaIt.hasNext()) {
                ExtensibilityElement extElem = (ExtensibilityElement) schemaIt.next();
                if (extElem instanceof Schema) {
                    Schema schema = (Schema) extElem;
                    readSchema(schema);
                }
            }
        }
    }
    
    /**
     * Stores top-level schema elements and complexTypes in maps (for a single schema)
     */
    private void readSchema(Schema schema) throws Wsdl2JsException {
        // Process imported schemas
        Map<?, ?> imports = schema.getImports();
        Iterator<?> impIt = imports.keySet().iterator();
        while (impIt.hasNext()) {
            String targetNS = (String) impIt.next();
            List<?> list = (List<?>) imports.get(targetNS);
            Iterator<?> listIt = list.iterator();
            while (listIt.hasNext()) {
                Object obj = listIt.next();
                if (obj instanceof SchemaImport) {
                    SchemaImport schemaImp = (SchemaImport) obj;
                    Schema refSchema = schemaImp.getReferencedSchema();
                    readSchema(refSchema);
                }
            }
        }
        // Process schema type definitions
        NodeList nodes = schema.getElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
                if (node.getLocalName().equals("complexType")) {
                    Element typeElem = (Element) node;
                    String name = typeElem.getAttribute("name");
                    String targetNS = schema.getElement().getAttribute("targetNamespace");
                    QName qname = new QName(targetNS, name);
                    schemaComplexTypes.put(qname, typeElem);
                } else if (node.getLocalName().equals("element")) {
                    Element elemElem = (Element) node;
                    String name = elemElem.getAttribute("name");
                    String targetNS = schema.getElement().getAttribute("targetNamespace");
                    String type = elemElem.getAttribute("type");
                    Matcher matcher = PREFIXPATTERN.matcher(type);
                    if (!matcher.matches()) throw new Wsdl2JsException("Element type value isn't prefixed: " + type);
                    String prefix = matcher.group(1);
                    String localName = matcher.group(2);
                    String nsuri = DOMUtils.getNamespaceURIFromPrefix(elemElem, prefix);
                    if (nsuri == null) throw new Wsdl2JsException("No namespace declaration found for prefix: " + prefix);
                    QName elemQName = new QName(targetNS, name);
                    QName typeQName = new QName(nsuri, localName);
                    schemaElementsToTypes.put(elemQName, typeQName);
                }
            }
        }
    }
    
    /**
     * Check if complexType declaration represents an array
     */
    private boolean isComplexTypeArray(Element complexType) {
        if(!(complexType.getNamespaceURI().equals(XMLNS_SCHEMA) && complexType.getLocalName().equals("complexType")))
            throw new IllegalArgumentException("Expected complex type declaration element as argument");
        Element[] elems=getSchemaChildren(complexType);
        if(elems.length==1 && elems[0].getLocalName().equals("sequence")) {
            elems=getSchemaChildren(elems[0]);
            if(elems.length==1 && elems[0].getLocalName().equals("element")) {
                String min=elems[0].getAttribute("minOccurs");
                String max=elems[0].getAttribute("maxOccurs");
                if(min!=null&&max!=null&&min.equals("0")&&max.equals("unbounded")) return true;
            }
        }    
    	return false;
    }
    
    /**
     * Check if element declaration represents an array
     */
    private boolean isElementArray(Element element) {
        if(!(element.getNamespaceURI().equals(XMLNS_SCHEMA) && element.getLocalName().equals("element")))
            throw new IllegalArgumentException("Expected element declaration as argument");
        String min=element.getAttribute("minOccurs");
        String max=element.getAttribute("maxOccurs");
        if(min!=null && max!=null && min.equals("0") && max.equals("unbounded")) return true;
        return false;
    }
    
    /**
     * Extract array information from complexType
     */
    private ArrayInfo getArrayInfoFromComplexType(Element complexType) throws Wsdl2JsException {
        if(!(complexType.getNamespaceURI().equals(XMLNS_SCHEMA) && complexType.getLocalName().equals("complexType"))) 
            throw new IllegalArgumentException("Expected complex type declaration element as argument");
        Element[] elems=getSchemaChildren(complexType);
        if(elems.length==1 && elems[0].getLocalName().equals("sequence")) {
            elems=getSchemaChildren(elems[0]);
            if(elems.length==1 && elems[0].getLocalName().equals("element")) {
                if(isElementArray(elems[0])) {
                    return getArrayInfoFromElement(elems[0]);  
                }
            }
        }
        throw new IllegalArgumentException("Expected complex type declaration representing an array");
    }
    
    /**
     * Extract array information from element
     */
    private ArrayInfo getArrayInfoFromElement(Element element) throws Wsdl2JsException {
        if(!(element.getNamespaceURI().equals(XMLNS_SCHEMA) && element.getLocalName().equals("element")))
            throw new IllegalArgumentException("Expected element declaration as argument");
        if(!isElementArray(element)) 
            throw new IllegalArgumentException("Expected element declaration representing an array");
        String compType=element.getAttribute("type");
        Matcher mat=PREFIXPATTERN.matcher(compType);
        if(!mat.matches()) throw new Wsdl2JsException("Expected prefixed element array component type: "+compType);
        String compPrefix=mat.group(1);
        String compLocalName=mat.group(2);
        String compNSUri=DOMUtils.getNamespaceURIFromPrefix(element,compPrefix);
        QName compQName=new QName(compNSUri,compLocalName);
        Element complexType=(Element)schemaComplexTypes.get(compQName);
        if(complexType!=null) {
            if(isComplexTypeArray(complexType)) {
                ArrayInfo compInfo=getArrayInfoFromComplexType(complexType);
                ArrayInfo info=new ArrayInfo(compInfo.arrayType,compInfo.arrayDim+1);
                return info;
            } else return new ArrayInfo(compQName,1);
        } else return new ArrayInfo(compQName,1);           
    }
    
    /**
     * Create type description JS code for a bean using its complexType definition
     */
    private String getMemberInfos(Element complexType) throws Wsdl2JsException {
        if(!(complexType.getNamespaceURI().equals(XMLNS_SCHEMA) && complexType.getLocalName().equals("complexType")))
            throw new IllegalArgumentException("Expected complex type declaration element as argument: "+getXMLString(complexType));
            Element modelElem=getFirstSchemaChild(complexType);
            if(modelElem!=null) {
            	if(modelElem.getLocalName().equals("sequence")) {
                    return getSequenceMemberInfos(modelElem);
                } else if(modelElem.getLocalName().equals("complexContent")) {
                    Element extElem=getFirstSchemaChild(modelElem);
                    if(extElem!=null&&extElem.getLocalName().equals("extension")) {
                    	String base=extElem.getAttribute("base");     
                    	QName qname=getQName(base,extElem);
                    	Element baseElem=(Element)schemaComplexTypes.get(qname);
                    	String baseInfos=getMemberInfos(baseElem);
                    	Element seqElem=getFirstSchemaChild(extElem);
                    	if(seqElem!=null&&seqElem.getLocalName().equals("sequence")) {
                    	    String infos=getSequenceMemberInfos(seqElem);
                    	    if(infos.length()>0 && baseInfos.length()>0) {
                    	        infos=infos+","+baseInfos;
                    	    } else {
                    	        infos=infos+baseInfos;
                    	    }
                    	    return infos;
                    	} else throw new Wsdl2JsException("Illegal 'extension' content: "+complexType.getAttribute("name"));
                    } else throw new Wsdl2JsException("Illegal 'complexContent': "+complexType.getAttribute("name"));
                } else throw new Wsdl2JsException("Illegal content model: "+complexType.getAttribute("name"));
            } else throw new Wsdl2JsException("XML Schema child expected below complex type: "+getXMLString(complexType));
    }
    
    /**
     * Create type description JS code for a bean using its sequence definition
     */
    private String getSequenceMemberInfos(Element sequence) throws Wsdl2JsException {
        if(!(sequence.getNamespaceURI().equals(XMLNS_SCHEMA) && sequence.getLocalName().equals("sequence")))
            throw new IllegalArgumentException("Expected sequence declaration element as argument");
        String memberList="";
        Element[] elems=getSchemaChildren(sequence);
        for(int i=0;i<elems.length;i++) {
            Element elem=elems[i];
            if(elem.getLocalName().equals("element")) {
                String name=elem.getAttribute("name");
                String typeInfo=createTypeInfo(elem);
                memberList+="\""+name+"\","+typeInfo;
                if(i<elems.length-1) memberList+=",";
        	}
        }
        return memberList;
    }
    
    /**
     * Create a QName object from a prefixed value (the passed element is used for namespace resolution)
     */
    private QName getQName(String value,Element element) throws Wsdl2JsException {
        Matcher matcher=PREFIXPATTERN.matcher(value);
        if (!matcher.matches()) throw new Wsdl2JsException("Value isn't prefixed: " + value);
        String prefix = matcher.group(1);
        String localName = matcher.group(2);
        String nsuri = DOMUtils.getNamespaceURIFromPrefix(element, prefix);
        return new QName(nsuri,localName);
    }
    
    /**
     * Return the first schema child element
     */
    private Element getFirstSchemaChild(Element parent) {
        NodeList nl=parent.getChildNodes();
        for(int i=0;i<nl.getLength();i++) {
        	Node n=nl.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE&&n.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
            	return (Element)n;
            }
        }
        return null;
    }
    
    /**
     * Return an array of all schema child elements
     */
    private Element[] getSchemaChildren(Element parent) {
        List<Element> al=new ArrayList<Element>();
        NodeList nl=parent.getChildNodes();
        for(int i=0;i<nl.getLength();i++) {
            Node n=nl.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE&&n.getNamespaceURI().equals(Constants.XMLNS_XSD)) {
            	al.add((Element)n);
            }
        }
        Element[] elems=new Element[al.size()];
        al.toArray(elems);
        return elems;
    }
    
    /**
     * Create string representation of the XML element
     */
    private String getXMLString(Element element) {
        StringBuilder sb=new StringBuilder();
        getXMLString(element,sb);
        return sb.toString();
    }
    
    /**
     * Create string representation of the XML element by walking down the DOM tree
     */
    private void getXMLString(Element element, StringBuilder sb) {
        sb.append("<");
        sb.append(element.getLocalName());
        NamedNodeMap attrs=element.getAttributes();
        for(int i=0;i<attrs.getLength();i++) {
            Node node=attrs.item(i);
            sb.append(" ");
            sb.append(node.getNodeName());
            sb.append("=\"");
            sb.append(node.getNodeValue());
            sb.append("\"");
        }
        if(element.hasChildNodes()) {
            sb.append(">");
            NodeList nodes=element.getChildNodes();
            for(int i=0;i<nodes.getLength();i++) {
                if(nodes.item(i).getNodeType()==Node.ELEMENT_NODE) {
                    Element child=(Element)nodes.item(i);
                    getXMLString(child,sb);
                }
            }
            sb.append("</");
            sb.append(element.getLocalName());
            sb.append(">");
        } else {
            sb.append("/>");
        }
    }
    
    
    /**
     * Helper class for storing information while traversing array schema definitions
     */
    class ArrayInfo {
     
        QName arrayType;
        int arrayDim;
        
        ArrayInfo(QName arrayType,int arrayDim) {
        	this.arrayType=arrayType;
            this.arrayDim=arrayDim;
        }
        
    }
    
}
