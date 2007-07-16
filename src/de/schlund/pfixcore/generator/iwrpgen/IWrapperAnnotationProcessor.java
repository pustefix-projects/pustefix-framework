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

package de.schlund.pfixcore.generator.iwrpgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.util.SimpleDeclarationVisitor;


/**
 * @author mleidig@schlund.de
 */
public class IWrapperAnnotationProcessor implements AnnotationProcessor {

    private final static String XMLNS_IWRP="http://pustefix.sourceforge.net/interfacewrapper200401";
    private final static String DEFAULT_SUFFIX="Wrapper";
    
    private static DocumentBuilderFactory docBuilderFactory;
    
    protected AnnotationProcessorEnvironment env;
    protected AnnotationTypeDeclaration iwrpType;
    protected AnnotationTypeDeclaration transientType;
    protected AnnotationTypeDeclaration paramType;
    protected AnnotationTypeDeclaration casterType;
    protected AnnotationTypeDeclaration preCheckType;
    protected AnnotationTypeDeclaration postCheckType;
    protected AnnotationTypeDeclaration propertyType;
    
    public IWrapperAnnotationProcessor(AnnotationProcessorEnvironment env) {
        this.env=env;
        iwrpType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.IWrapper");
        transientType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.Transient");
        paramType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.Param");
        casterType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.Caster");
        preCheckType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.PreCheck");
        postCheckType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.PostCheck");
        propertyType=(AnnotationTypeDeclaration)env.getTypeDeclaration("de.schlund.pfixcore.generator.annotation.Property");
    }
    
    public void process() {
    	Collection<Declaration> decls=env.getDeclarationsAnnotatedWith(iwrpType);
        for(Declaration decl:decls) {
        	decl.accept(new TypeVisitor());
        }
    }
    
    protected static Document createDocument() {
        if(docBuilderFactory==null) {
            docBuilderFactory=DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
        }
        try {
        	DocumentBuilder docBuilder=docBuilderFactory.newDocumentBuilder();
            return docBuilder.newDocument();
        } catch(ParserConfigurationException x) {
            throw new RuntimeException("Can't create DOM",x);
        }
    }
    
    protected void warn(String msg) {
    	env.getMessager().printWarning(msg);
    }
    
    protected String getIWrapperName(ClassDeclaration classDecl) {
    	String beanFullName=classDecl.getQualifiedName();
    	AnnotationMirror iwrpMirror=MirrorApiUtils.getAnnotationMirror(classDecl,iwrpType);
    	String iwrapperClass=beanFullName+DEFAULT_SUFFIX;
    	if(iwrpMirror!=null) {
    		AnnotationValue nameValue=MirrorApiUtils.getAnnotationValue(iwrpMirror,"name");
    		if(nameValue!=null) {
    			String name=nameValue.getValue().toString();
    			if(!name.equals("")) {
    				if(name.contains(".")) iwrapperClass=name;
    				else {
    					int ind=beanFullName.lastIndexOf(".");
    					if(ind==-1) iwrapperClass=name;
    					else iwrapperClass=beanFullName.substring(0,ind)+"."+name;
    				}
    			} 
    		}
    	}
    	return iwrapperClass;
    }
    
    protected String extractPropertyName(String methodName) {
        String name=methodName.substring(3);
        if(name.length()>1&&Character.isUpperCase(name.charAt(0))&&
                Character.isUpperCase(name.charAt(1))) return name;
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
    
    protected String createGetterName(String propName) {
        return "get"+Character.toUpperCase(propName.charAt(0))+propName.substring(1);
    }
    
    protected String createSetterName(String propName) {
        return "set"+Character.toUpperCase(propName.charAt(0))+propName.substring(1);
    }
    
    class TypeVisitor extends SimpleDeclarationVisitor {
        
    	Document doc;
    	Element root;
    	
        public void visitClassDeclaration(ClassDeclaration classDecl) {
        	     	
        	AnnotationMirror iwrpMirror=MirrorApiUtils.getAnnotationMirror(classDecl,iwrpType);
        
        	String iwrapperClass=null;
        	
        	if(doc==null) {
        	
        		doc=createDocument();
            	root=doc.createElementNS(XMLNS_IWRP,"iwrp:interface");
            	doc.appendChild(root);
            	
            	iwrapperClass=getIWrapperName(classDecl);
        		
        		AnnotationValue ihandlerValue=MirrorApiUtils.getAnnotationValue(iwrpMirror,"ihandler");
        		if(ihandlerValue!=null) {
        			Element ihandlerElem=doc.createElementNS(XMLNS_IWRP,"iwrp:ihandler");
        			root.appendChild(ihandlerElem);
        			ihandlerElem.setAttribute("class",ihandlerValue.getValue().toString());
        		}
        		
        	}
        		
        	Collection<FieldDeclaration> fieldDecls=classDecl.getFields();
        	for(FieldDeclaration fieldDecl:fieldDecls) {
        		fieldDecl.accept(this);
        	}
        	Collection<MethodDeclaration> methDecls=classDecl.getMethods();
        	for(MethodDeclaration methDecl:methDecls) {
        		methDecl.accept(this);
        	}
        	
        	boolean hasSuperIWrapper=false;
        	ClassType supType=classDecl.getSuperclass();
        	if(supType!=null) {
        		ClassDeclaration supDecl=supType.getDeclaration();	
        		hasSuperIWrapper=MirrorApiUtils.getAnnotationMirror(supDecl,iwrpType)!=null;
        		if(hasSuperIWrapper) {
        			String superIWrapperName=getIWrapperName(supDecl);
        			root.setAttribute("extends",superIWrapperName);
        		}
        	}
            
        	if(!hasSuperIWrapper && supType!=null &&!supType.getDeclaration().getQualifiedName().equals("java.lang.Object")) {
        		supType.getDeclaration().accept(this);
        	}
        	
        	if(iwrpMirror!=null) {
        		Filer filer=env.getFiler();
        		try {
        			env.getMessager().printNotice("Generate class "+iwrapperClass);
        			PrintWriter writer=filer.createSourceFile(iwrapperClass);
	        	
	        		TransformerFactory tf=TransformerFactory.newInstance();
	        		FileInputStream fis=new FileInputStream(new File("projects/core/build/iwrapper.xsl"));
	        		Transformer t=tf.newTransformer(new StreamSource(fis));
	        		//t.setOutputProperty(OutputKeys.INDENT,"yes");
	        		int ind=iwrapperClass.lastIndexOf('.');
	        		String packageName="";
	        		String className=iwrapperClass;
	        		if(ind>-1) {
	        			packageName=iwrapperClass.substring(0,ind);
	        			className=iwrapperClass.substring(ind+1);
	        		}
	        		t.setParameter("package",packageName);
	        		t.setParameter("classname",className);
	        		t.transform(new DOMSource(doc),new StreamResult(writer));
	        		
	        		
	                 //t=tf.newTransformer();
	                 //t.setOutputProperty(OutputKeys.INDENT,"yes");
	                 //t.transform(new DOMSource(doc),new StreamResult(System.out));
	        	} catch(Exception x) {
	        		x.printStackTrace();
	        	}
        	}
        }
        
        public void visitFieldDeclaration(FieldDeclaration fieldDecl) {
        	if(MirrorApiUtils.isPublicNonStaticNonFinal(fieldDecl)) {
    			String propName=fieldDecl.getSimpleName();
    			String getterName=createGetterName(propName);
    			ClassDeclaration classDecl=(ClassDeclaration)fieldDecl.getDeclaringType();
    			MethodDeclaration methDecl=MirrorApiUtils.getMethodDeclaration(classDecl,getterName);
    			if(methDecl!=null) {
    				if(MirrorApiUtils.isPublicNonStatic(methDecl)) {
    					TypeMirror retType=methDecl.getReturnType();
    					if(retType instanceof VoidType) {
    						warn("Ignore getter returning void: "+methDecl);
    						methDecl=null;
    					} else {
    						TypeMirror fieldType=fieldDecl.getType();
    						if(retType!=fieldType) {
    							warn("Ignore field with differing getter type: "+
    								propName+" "+fieldType+" "+retType);
    						}
    					}
    				}
    			}
    			if(methDecl==null) {
    				AnnotationMirror transientMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,transientType);
    				if(transientMirror==null) {
    					AnnotationMirror paramMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,paramType);
	    				Element paramElem=addParam(root,propName,fieldDecl.getType(),paramMirror);
	    				if(paramElem==null) return;
	    				AnnotationMirror casterMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,casterType);
	            		if(casterMirror!=null) addCaster(paramElem,casterMirror);
	            		AnnotationMirror preCheckMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,preCheckType);
	            		if(preCheckMirror!=null) addPreCheck(paramElem,preCheckMirror);
	            		AnnotationMirror postCheckMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,postCheckType);
	            		if(postCheckMirror!=null) addPostCheck(paramElem,postCheckMirror);
    				}
    			}
    		}
        }
        
        public void visitMethodDeclaration(MethodDeclaration methDecl) {
        	String getterName=methDecl.getSimpleName();
        	if(MirrorApiUtils.isPublicNonStatic(methDecl) &&
        			getterName.length()>3 && Character.isUpperCase(getterName.charAt(3)) &&
        			getterName.startsWith("get") && methDecl.getParameters().size()==0 &&
        			!(methDecl.getReturnType() instanceof VoidType)) {
        		String propName=extractPropertyName(getterName);
        		ClassDeclaration classDecl=(ClassDeclaration)methDecl.getDeclaringType();
    			FieldDeclaration fieldDecl=MirrorApiUtils.getFieldDeclaration(classDecl,propName);
    			if(fieldDecl!=null && !fieldDecl.getType().equals(methDecl.getReturnType())) {
    				warn("Ignore field with differing type: "+fieldDecl);
    				fieldDecl=null;
    			}
    			String setterName=createSetterName(propName);
    			MethodDeclaration setter=MirrorApiUtils.getMethodDeclaration(classDecl,setterName);
    			if(setter==null) {
    				warn("Ignore getter without setter or public field: "+methDecl);
    				return;
    			} else {
    				if(!(setter.getReturnType() instanceof VoidType)) {
    					warn("Ignore getter with setter having return type: "+methDecl);
    					return;
    				} else if(setter.getParameters().size()!=1) {
    					warn("Ignore getter with setter not having single parameter: "+methDecl);
    				} else if(!setter.getParameters().iterator().next().getType().equals(methDecl.getReturnType())) {
    					warn("Ignore getter with setter having different type: "+methDecl+
    							" "+setter.getParameters().iterator().next().getType()+" "+methDecl.getReturnType());
    					return;
    				}
    			}
    			String propClass=null;
    			AnnotationMirror transientMirror=MirrorApiUtils.getAnnotationMirror(methDecl,transientType);
    			if(transientMirror==null && fieldDecl!=null) transientMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,transientType);
        		if(transientMirror==null) {
        			AnnotationMirror paramMirror=MirrorApiUtils.getAnnotationMirror(methDecl,paramType);
        			if(paramMirror==null && fieldDecl!=null) paramMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,paramType);
	        		Element paramElem=addParam(root,propName,methDecl.getReturnType(),paramMirror);
	        		if(paramElem==null) return;
	        		AnnotationMirror casterMirror=MirrorApiUtils.getAnnotationMirror(methDecl,casterType);
	        		if(casterMirror==null && fieldDecl!=null) casterMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,casterType);
	        		if(casterMirror!=null) addCaster(paramElem,casterMirror);
	        		AnnotationMirror preCheckMirror=MirrorApiUtils.getAnnotationMirror(methDecl,preCheckType);
	        		if(preCheckMirror==null && fieldDecl!=null) preCheckMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,preCheckType);
	        		if(preCheckMirror!=null) addPreCheck(paramElem,preCheckMirror);
	        		AnnotationMirror postCheckMirror=MirrorApiUtils.getAnnotationMirror(methDecl,postCheckType);
	        		if(postCheckMirror==null && fieldDecl!=null) postCheckMirror=MirrorApiUtils.getAnnotationMirror(fieldDecl,postCheckType);
	        		if(postCheckMirror!=null) addPostCheck(paramElem,postCheckMirror);
        		}
        		
        	}
        }
        
        private Element addParam(Element rootElem,String propertyName,TypeMirror typeMirror,AnnotationMirror mirror) {
        	Element paramElem=rootElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:param");
            
          
            String paramType=null;
        	if(typeMirror instanceof ClassType) {
        		ClassType propType=(ClassType)typeMirror;
				paramType=propType.getDeclaration().getQualifiedName();
        	} else if(typeMirror instanceof ArrayType) {
        		ArrayType propType=(ArrayType)typeMirror;
        		TypeMirror compType=propType.getComponentType();
        		if(compType instanceof ClassType) {
        			paramElem.setAttribute("frequency","multiple");
        			paramType=((ClassType)compType).getDeclaration().getQualifiedName();
        		} else {
        			warn("Ignore property because of unsupported component type: "+typeMirror);
        		}
        	} else {
				warn("Ignore property because of unsupported type: "+typeMirror);
				return null;
			}
        	rootElem.appendChild(paramElem);
            paramElem.setAttribute("type",paramType);     	
            
            String occurStr="mandatory";
            if(mirror!=null) {
            	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"mandatory");
            	if(value!=null && value.getValue().toString().equals("false")) occurStr="optional";
            }
            paramElem.setAttribute("occurrence",occurStr);
            
            boolean trim=true;
            if(mirror!=null) {
            	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"trim");
            	if(value!=null && value.getValue().toString().equals("false")) trim=false;
            }
            paramElem.setAttribute("trim",String.valueOf(trim));
            
            String misscode="";
            if(mirror!=null) {
            	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"missingscode");
            	if(value!=null) misscode=value.getValue().toString();
            }
            if(!misscode.equals("")) paramElem.setAttribute("missingscode",misscode);
            
            String name=Character.toUpperCase(propertyName.charAt(0))+propertyName.substring(1);
            if(mirror!=null) {
            	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"name");
            	if(value!=null) name=value.getValue().toString();
            }
            paramElem.setAttribute("name",name);
            
            if(mirror!=null) {
            	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"defaults");
            	if(value!=null) {
            		Element defElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:default");
        			paramElem.appendChild(defElem);
            		Collection<AnnotationValue> defaults=(Collection<AnnotationValue>)value.getValue();
            		for(AnnotationValue def:defaults) {
            			Element valElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:value");
            			valElem.setTextContent(def.getValue().toString());
            			defElem.appendChild(valElem);
            		}
            	}
            }
            
            return paramElem;
        }
        
        private void addCaster(Element paramElem,AnnotationMirror mirror) {
        	Element casterElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:caster");
        	paramElem.appendChild(casterElem);
        	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"type");
        	String casterType=value.getValue().toString();
        	casterElem.setAttribute("class",casterType);
        	value=MirrorApiUtils.getAnnotationValue(mirror,"properties");
        	if(value!=null) {
        		Collection<AnnotationValue> casterProps=(Collection<AnnotationValue>)value.getValue();
        		for(AnnotationValue val:casterProps) {
        			Element propElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:cparam");
        			casterElem.appendChild(propElem);
        			AnnotationMirror propMirror=(AnnotationMirror)val.getValue();
        			propElem.setAttribute("name",MirrorApiUtils.getAnnotationValue(propMirror,"name").getValue().toString());
        			propElem.setAttribute("value",MirrorApiUtils.getAnnotationValue(propMirror,"value").getValue().toString());
                }
            }
        }
        
        private void addPreCheck(Element paramElem,AnnotationMirror mirror) {
        	Element preCheckElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:precheck");
        	paramElem.appendChild(preCheckElem);
        	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"type");
        	String preCheckType=value.getValue().toString();
        	preCheckElem.setAttribute("class",preCheckType);
        	value=MirrorApiUtils.getAnnotationValue(mirror,"properties");
        	if(value!=null) {
        		Collection<AnnotationValue> preCheckProps=(Collection<AnnotationValue>)value.getValue();
        		for(AnnotationValue val:preCheckProps) {
        			Element propElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:cparam");
        			preCheckElem.appendChild(propElem);
        			AnnotationMirror propMirror=(AnnotationMirror)val.getValue();
        			propElem.setAttribute("name",MirrorApiUtils.getAnnotationValue(propMirror,"name").getValue().toString());
        			propElem.setAttribute("value",MirrorApiUtils.getAnnotationValue(propMirror,"value").getValue().toString());
        		}
        	}
        }
        
        private void addPostCheck(Element paramElem,AnnotationMirror mirror) {
        	Element postCheckElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:postcheck");
        	paramElem.appendChild(postCheckElem);
        	AnnotationValue value=MirrorApiUtils.getAnnotationValue(mirror,"type");
        	String postCheckType=value.getValue().toString();
        	postCheckElem.setAttribute("class",postCheckType);
        	value=MirrorApiUtils.getAnnotationValue(mirror,"properties");
        	if(value!=null) {
        		Collection<AnnotationValue> postCheckProps=(Collection<AnnotationValue>)value.getValue();
        		for(AnnotationValue val:postCheckProps) {
        			Element propElem=paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP,"iwrp:cparam");
        			postCheckElem.appendChild(propElem);
        			AnnotationMirror propMirror=(AnnotationMirror)val.getValue();
        			propElem.setAttribute("name",MirrorApiUtils.getAnnotationValue(propMirror,"name").getValue().toString());
        			propElem.setAttribute("value",MirrorApiUtils.getAnnotationValue(propMirror,"value").getValue().toString());
        		}
        	}
        }
        
        
        
    }
    
}
