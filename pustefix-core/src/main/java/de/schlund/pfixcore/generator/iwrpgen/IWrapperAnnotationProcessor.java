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

package de.schlund.pfixcore.generator.iwrpgen;

import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.pustefixframework.util.AnnotationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.annotation.Caster;
import de.schlund.pfixcore.generator.annotation.IWrapper;
import de.schlund.pfixcore.generator.annotation.Param;
import de.schlund.pfixcore.generator.annotation.PostCheck;
import de.schlund.pfixcore.generator.annotation.PreCheck;
import de.schlund.pfixcore.generator.annotation.Property;
import de.schlund.pfixcore.generator.annotation.Transient;

/**
 * @author mleidig@schlund.de
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes(value= {"de.schlund.pfixcore.generator.annotation.IWrapper"})
public class IWrapperAnnotationProcessor extends AbstractProcessor {

    private final static String XMLNS_IWRP = "http://www.pustefix-framework.org/2008/namespace/iwrapper";
    private final static String DEFAULT_SUFFIX = "Wrapper";

    private static DocumentBuilderFactory docBuilderFactory;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeVisitor visitor = new TypeVisitor();
        for(javax.lang.model.element.Element element: roundEnv.getElementsAnnotatedWith(IWrapper.class)) {
            element.accept(visitor, null);
        }
        return true;
    }
    
    protected static Document createDocument() {
        if (docBuilderFactory == null) {
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
        }
        try {
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.newDocument();
        } catch (ParserConfigurationException x) {
            throw new RuntimeException("Can't create DOM", x);
        }
    }

    protected void warn(String msg) {
        processingEnv.getMessager().printMessage(Kind.WARNING, msg);
    }

    protected String getIWrapperName(TypeElement typeElem) {
        String beanFullName = typeElem.getQualifiedName().toString();
        IWrapper iwrp = typeElem.getAnnotation(IWrapper.class);
        String iwrapperClass = beanFullName + DEFAULT_SUFFIX;
        if(iwrp != null) {
            String name= iwrp.name();
            if (!name.equals("")) {
                if (name.contains(".")) {
                    iwrapperClass = name;
                } else {
                    int ind = beanFullName.lastIndexOf(".");
                    if (ind == -1) iwrapperClass = name;
                    else iwrapperClass = beanFullName.substring(0, ind) + "." + name;
                }
            }
        }
        return iwrapperClass;
    }

    protected String extractPropertyName(String methodName) {
        String name = "";
        if (methodName.length() > 3 && methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3))) {
            name = methodName.substring(3);
        } else  if (methodName.length() > 2 && methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2))) {
            name = methodName.substring(2);
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    protected String createGetterName(String propName) {
        return "get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
    }

    protected String createSetterName(String propName) {
        return "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
    }

    class TypeVisitor extends ElementScanner6<Void,Void> {

        Document doc;
        Element root;

        Set<TypeKind> builtinPrimitives = new HashSet<TypeKind>();
        Set<String> builtinTypes = new HashSet<String>();

        private String BOOLEAN_CASTER = "de.schlund.pfixcore.generator.casters.ToBoolean";
        private String BYTE_CASTER = "de.schlund.pfixcore.generator.casters.ToByte";
        private String DOUBLE_CASTER = "de.schlund.pfixcore.generator.casters.ToDouble";
        private String FLOAT_CASTER = "de.schlund.pfixcore.generator.casters.ToFloat";
        private String INTEGER_CASTER = "de.schlund.pfixcore.generator.casters.ToInteger";
        private String LONG_CASTER = "de.schlund.pfixcore.generator.casters.ToLong";

        private String DATE_CASTER = "de.schlund.pfixcore.generator.casters.ToDate";

        public TypeVisitor() {
            builtinPrimitives.add(TypeKind.BOOLEAN);
            builtinPrimitives.add(TypeKind.BYTE);
            builtinPrimitives.add(TypeKind.DOUBLE);
            builtinPrimitives.add(TypeKind.FLOAT);
            builtinPrimitives.add(TypeKind.INT);
            builtinPrimitives.add(TypeKind.LONG);
            builtinTypes.add(Boolean.class.getName());
            builtinTypes.add(Byte.class.getName());
            builtinTypes.add(Double.class.getName());
            builtinTypes.add(Float.class.getName());
            builtinTypes.add(Integer.class.getName());
            builtinTypes.add(Long.class.getName());
            builtinTypes.add(Date.class.getName());
            builtinTypes.add(String.class.getName());
        }

        @Override
        public Void visitType(TypeElement typeElem, Void param) {

            if(typeElem.getKind() == ElementKind.CLASS) {   
                
                String iwrapperClass = null;
                IWrapper iwrpAnno = typeElem.getAnnotation(IWrapper.class);

                if (doc == null) {

                    doc = createDocument();
                    root = doc.createElementNS(XMLNS_IWRP, "iwrp:interface");
                    doc.appendChild(root);

                    iwrapperClass = getIWrapperName(typeElem);

                    Element ihandlerElem = doc.createElementNS(XMLNS_IWRP, "iwrp:ihandler");
                    root.appendChild(ihandlerElem);
                
                    String beanRef = null;
                    String ihandlerClass = null;
                
                    if(!iwrpAnno.beanRef().equals("")) {
                        beanRef = iwrpAnno.beanRef(); 
                    }
                
                    try {
                        iwrpAnno.ihandler();
                    } catch(MirroredTypeException x) {
                        ihandlerClass = ((TypeElement)((DeclaredType)x.getTypeMirror()).asElement()).getQualifiedName().toString();
                    }
                    if(ihandlerClass.equals(IHandler.class.getName())) {
                        ihandlerClass = null;
                    }
                
                    if(beanRef == null && ihandlerClass == null) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Neither beanRef nor ihandler is set: " + typeElem.getQualifiedName());
                    } else if(beanRef != null && ihandlerClass != null) {
                        processingEnv.getMessager().printMessage(Kind.ERROR, "Setting both, beanRef and ihandler, isn't allowed: " + typeElem.getQualifiedName());
                    } else if(beanRef != null) {
                        ihandlerElem.setAttribute("bean-ref", beanRef);
                    } else if(ihandlerClass != null) {
                        ihandlerElem.setAttribute("class", ihandlerClass);
                    }

                }
                
                List<VariableElement> fieldElems = AnnotationUtils.getPublicNonStaticNonFinalFields(typeElem);
                for(VariableElement fieldElem: fieldElems) {
                    fieldElem.accept(this, null);
                }
                
                List<ExecutableElement> execElems = AnnotationUtils.getPublicNonStaticMethods(typeElem);
                for(ExecutableElement execElem: execElems) {
                    execElem.accept(this, null);
                }

                boolean hasSuperIWrapper = false;
                TypeElement supElem = null;
                if(typeElem.getSuperclass().getKind() != TypeKind.NONE) {
                    TypeMirror typeMirror = typeElem.getSuperclass();
                    if(typeMirror instanceof DeclaredType) {
                        DeclaredType decType = (DeclaredType)typeMirror;
                        javax.lang.model.element.Element elem = decType.asElement();
                        if(elem instanceof TypeElement && !((TypeElement) elem).getQualifiedName().toString().equals("java.lang.Object")) {
                            supElem = (TypeElement)elem;
                            IWrapper supIwrp = supElem.getAnnotation(IWrapper.class);
                            if(supIwrp != null) {
                                hasSuperIWrapper = true;
                                String superIWrapperName = getIWrapperName(supElem);
                                root.setAttribute("extends", superIWrapperName);
                            }
                        }
                    }
                }
                
                
                if (!hasSuperIWrapper && supElem != null) {
                    supElem.accept(this, null);
                }

                if (iwrpAnno != null) {
                    Filer filer = processingEnv.getFiler();
                    try {
                        processingEnv.getMessager().printMessage(Kind.NOTE, "Generate class " + iwrapperClass);
                        JavaFileObject fileObj = filer.createSourceFile(iwrapperClass, typeElem);
                        Writer writer = fileObj.openWriter();

                        TransformerFactory tf = TransformerFactory.newInstance();
                        InputStream fis = getClass().getResourceAsStream("/pustefix/xsl/iwrapper.xsl");
                        Transformer t = tf.newTransformer(new StreamSource(fis));
                        // t.setOutputProperty(OutputKeys.INDENT,"yes");
                        int ind = iwrapperClass.lastIndexOf('.');
                        String packageName = "";
                        String className = iwrapperClass;
                        if (ind > -1) {
                            packageName = iwrapperClass.substring(0, ind);
                            className = iwrapperClass.substring(ind + 1);
                        }
                        t.setParameter("package", packageName);
                        t.setParameter("classname", className);
                        t.transform(new DOMSource(doc), new StreamResult(writer));
                        doc = null;
                        root = null;
                        writer.close();
                        // t=tf.newTransformer();
                        // t.setOutputProperty(OutputKeys.INDENT,"yes");
                        // t.transform(new DOMSource(doc),new
                        // StreamResult(System.out));
                    } catch (Exception x) {
                        throw new RuntimeException("Error while generating IWrapper " + typeElem.getQualifiedName(), x);
                    }
                }
            
            } else {
                throw new RuntimeException("Type annotated with @IWrapper isn't class: " + typeElem.getKind() + " " + typeElem.getQualifiedName());
            }
            return null;
        }

        @Override
        public Void visitVariable(VariableElement varElem, Void param) {
            if(varElem.getKind() == ElementKind.FIELD) {
                if(AnnotationUtils.isPublicNonStaticNonFinal(varElem)) {
                    String propName = varElem.getSimpleName().toString();
                    String getterName = createGetterName(propName);
                    TypeElement encElem = (TypeElement)varElem.getEnclosingElement();
                    ExecutableElement meth = AnnotationUtils.getMethod(encElem, getterName);
                    if(meth != null) {
                        if(AnnotationUtils.isPublicNonStatic(meth)) {
                            TypeMirror retType = meth.getReturnType();
                            if(retType.getKind() == TypeKind.VOID) {
                                warn("Ignore getter returning void: " + meth);
                                meth = null;
                            } else {
                                TypeMirror fieldType = varElem.asType();
                                if(!processingEnv.getTypeUtils().isSameType(fieldType, retType)) {
                                    warn("Ignore field with differing getter type: " + propName + " " + fieldType + " " + retType);
                                }
                            }
                        }
                    }
                    if (meth == null) {
                        Transient transientAnno = varElem.getAnnotation(Transient.class);
                        if(transientAnno == null) {
                            Param paramAnno = varElem.getAnnotation(Param.class);
                            Caster casterAnno = varElem.getAnnotation(Caster.class);
                            if(paramAnno != null || casterAnno != null || isBuiltinType(varElem.asType())) {
                                Element paramElem = addParam(root, propName, varElem.asType(), paramAnno);
                                if (paramElem == null) {
                                    return null;
                                } else {
                                    if (casterAnno != null) addCaster(paramElem, casterAnno);
                                    else autoAddCaster(paramElem, varElem.asType());
                                    PreCheck preCheckAnno = varElem.getAnnotation(PreCheck.class);
                                    if (preCheckAnno != null) addPreCheck(paramElem, preCheckAnno);
                                    PostCheck postCheckAnno = varElem.getAnnotation(PostCheck.class);
                                    if (postCheckAnno != null) addPostCheck(paramElem, postCheckAnno);
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitExecutable(ExecutableElement execElem, Void param) {
            if(execElem.getKind() == ElementKind.METHOD) {
                String getterName = execElem.getSimpleName().toString();
                if (AnnotationUtils.isPublicNonStatic(execElem) && 
                        ((getterName.length() > 3 && Character.isUpperCase(getterName.charAt(3)) && getterName.startsWith("get")) || 
                        (getterName.length() > 2 && Character.isUpperCase(getterName.charAt(2)) && getterName.startsWith("is")))
                        && execElem.getParameters().size() == 0 && !(execElem.getReturnType().getKind() == TypeKind.VOID)) {
                    String propName = extractPropertyName(getterName);
                    TypeElement encElem = (TypeElement)execElem.getEnclosingElement();
                    VariableElement field = AnnotationUtils.getField(encElem, propName);
                    if (field != null && !processingEnv.getTypeUtils().isSameType(field.asType(), execElem.getReturnType())) {
                        warn("Ignore field with differing type: " + field.getSimpleName());
                        field = null;
                    }
                    String setterName = createSetterName(propName);
                    ExecutableElement setter = AnnotationUtils.getMethod(encElem, setterName);
                    if (setter == null) {
                        warn("Ignore getter without setter or public field: " + execElem);
                        return null;
                    } else {
                        if (!(setter.getReturnType().getKind() == TypeKind.VOID)) {
                            warn("Ignore getter with setter having return type: " + execElem);
                            return null;
                        } else if (setter.getParameters().size() != 1) {
                            warn("Ignore getter with setter not having single parameter: " + execElem);
                        } else if (!processingEnv.getTypeUtils().isSameType(setter.getParameters().iterator().next().asType(), execElem.getReturnType())) {
                            warn("Ignore getter with setter having different type: " + execElem + " "
                                    + setter.getParameters().iterator().next().asType() + " " + execElem.getReturnType());
                            return null;
                        }
                    }
                    Transient transientAnno = execElem.getAnnotation(Transient.class);
                    if (transientAnno == null) {
                        Param paramAnno = execElem.getAnnotation(Param.class);
                        if (paramAnno == null && field != null) paramAnno = field.getAnnotation(Param.class);
                        Caster casterAnno = execElem.getAnnotation(Caster.class);
                        if (casterAnno == null && field != null) casterAnno = field.getAnnotation(Caster.class);
                        if (paramAnno != null || casterAnno != null || isBuiltinType(execElem.getReturnType())) {
                            Element paramElem = addParam(root, propName, execElem.getReturnType(), paramAnno);
                            if (paramElem == null) return null;
                            if (casterAnno != null) addCaster(paramElem, casterAnno);
                            else autoAddCaster(paramElem, execElem.getReturnType());
                            PreCheck preCheckAnno = execElem.getAnnotation(PreCheck.class);
                            if (preCheckAnno == null && field != null)
                                preCheckAnno = field.getAnnotation(PreCheck.class);
                            if (preCheckAnno != null) addPreCheck(paramElem, preCheckAnno);
                            PostCheck postCheckAnno = execElem.getAnnotation(PostCheck.class);
                            if (postCheckAnno == null && field != null)
                                postCheckAnno = field.getAnnotation(PostCheck.class);
                            if (postCheckAnno != null) addPostCheck(paramElem, postCheckAnno);
                        }
                    }
                }
            }
            return null;
        }

        private Element addParam(Element rootElem, String propertyName, TypeMirror typeMirror, Param paramAnno) {
            Element paramElem = rootElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:param");
            String paramType = null;
            if (!isArrayType(typeMirror)) {
                if (typeMirror instanceof DeclaredType) {
                    DeclaredType propType = (DeclaredType) typeMirror;
                    paramType = ((TypeElement)propType.asElement()).getQualifiedName().toString();
                } else if (typeMirror.getKind().isPrimitive()) {
                    PrimitiveType propType = (PrimitiveType) typeMirror;
                    paramType = getPrimitiveWrapperType(propType.getKind());
                    if (paramType == null) {
                        warn("Ignore property because of unsupported primitive type: " + typeMirror);
                    }
                }
            } else {
                TypeMirror compType = getArrayComponentType(typeMirror);
                if (compType instanceof DeclaredType) {
                    paramElem.setAttribute("frequency", "multiple");
                    paramType = ((TypeElement)((DeclaredType)compType).asElement()).getQualifiedName().toString();
                } else if (compType.getKind().isPrimitive()) {
                    paramElem.setAttribute("frequency", "multiple");
                    paramType = getPrimitiveWrapperType(((PrimitiveType) compType).getKind());
                    if (paramType == null) {
                        warn("Ignore property because of unsupported primitive component type: " + typeMirror);
                    }
                } else {
                    warn("Ignore property because of unsupported component type: " + typeMirror);
                }
            }
            rootElem.appendChild(paramElem);
            paramElem.setAttribute("type", paramType);

            String occurStr = "mandatory";
            if (paramAnno != null) {
                if (!paramAnno.mandatory()) occurStr = "optional";
            }
            paramElem.setAttribute("occurrence", occurStr);

            boolean trim = true;
            if (paramAnno != null) {
                if(!paramAnno.trim()) trim = false;
            }
            paramElem.setAttribute("trim", String.valueOf(trim));

            String misscode = "";
            if (paramAnno != null) {
                misscode = paramAnno.missingscode();
            }
            if (!misscode.equals("")) paramElem.setAttribute("missingscode", misscode);

            String name = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            if (paramAnno != null) {
                if (!paramAnno.name().equals("")) name = paramAnno.name();
            }
            paramElem.setAttribute("name", name);

            if (paramAnno != null) {
                if (paramAnno.defaults().length > 0) {
                    Element defElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:default");
                    paramElem.appendChild(defElem);
                    for (String def : paramAnno.defaults()) {
                        Element valElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:value");
                        valElem.setTextContent(def);
                        defElem.appendChild(valElem);
                    }
                }
            }

            return paramElem;
        }

        private void addCaster(Element paramElem, Caster casterAnno) {
            Element casterElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:caster");
            paramElem.appendChild(casterElem);
            String casterType = null;
            try {
                casterAnno.type();
            } catch(MirroredTypeException x) {
                casterType = ((TypeElement)((DeclaredType)x.getTypeMirror()).asElement()).getQualifiedName().toString();
            }
            casterElem.setAttribute("class", casterType);
            Property[] casterProps = casterAnno.properties();
            if (casterProps != null && casterProps.length > 0) {
                for(Property casterProp: casterProps) {
                    Element propElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:cparam");
                    casterElem.appendChild(propElem);
                    propElem.setAttribute("name", casterProp.name());
                    propElem.setAttribute("value", casterProp.value());
                }
            }
        }

        private void addPreCheck(Element paramElem, PreCheck preCheckAnno) {
            Element preCheckElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:precheck");
            paramElem.appendChild(preCheckElem);
            String preCheckType = preCheckAnno.type().getName();
            preCheckElem.setAttribute("class", preCheckType);
            Property[] preCheckProps = preCheckAnno.properties();
            if (preCheckProps != null && preCheckProps.length > 0) {
                for(Property preCheckProp: preCheckProps) {
                    Element propElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:cparam");
                    preCheckElem.appendChild(propElem);
                    propElem.setAttribute("name", preCheckProp.name());
                    propElem.setAttribute("value", preCheckProp.value());
                }
            }
        }

        private void addPostCheck(Element paramElem, PostCheck postCheckAnno) {
            Element postCheckElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:postcheck");
            paramElem.appendChild(postCheckElem);
            String postCheckType = postCheckAnno.type().getName();
            postCheckElem.setAttribute("class", postCheckType);
            Property[] postCheckProps = postCheckAnno.properties();
            if (postCheckProps != null && postCheckProps.length > 0) {
                for(Property postCheckProp: postCheckProps) {
                    Element propElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:cparam");
                    postCheckElem.appendChild(propElem);
                    propElem.setAttribute("name", postCheckProp.name());
                    propElem.setAttribute("value", postCheckProp.value());
                }
            }
        }

        private String getPrimitiveWrapperType(TypeKind kind) {
            String type = null;
            if (kind == TypeKind.BOOLEAN) type = Boolean.class.getName();
            else if (kind == TypeKind.BYTE) type = Byte.class.getName();
            // else if(kind==PrimitiveType.Kind.CHAR)
            // type=Character.class.getName();
            else if (kind == TypeKind.DOUBLE) type = Double.class.getName();
            else if (kind == TypeKind.FLOAT) type = Float.class.getName();
            else if (kind == TypeKind.INT) type = Integer.class.getName();
            else if (kind == TypeKind.LONG) type = Long.class.getName();
            // else if(kind==PrimitiveType.Kind.SHORT)
            // type=Short.class.getName();
            return type;
        }

        private void autoAddCaster(Element paramElem, TypeMirror mirror) {
            String caster = null;
            if (isBuiltinArrayType(mirror)) {
                TypeMirror compType = getArrayComponentType(mirror);
                mirror = compType;
            }
            if (mirror.getKind().isPrimitive()) {
                TypeKind kind = ((PrimitiveType) mirror).getKind();
                
                if (kind == TypeKind.BOOLEAN) caster = BOOLEAN_CASTER;
                else if (kind == TypeKind.BYTE) caster = BYTE_CASTER;
                // else if(kind==PrimitiveType.Kind.CHAR)
                // caster=CHARACTER_CASTER;
                else if (kind == TypeKind.DOUBLE) caster = DOUBLE_CASTER;
                else if (kind == TypeKind.FLOAT) caster = FLOAT_CASTER;
                else if (kind == TypeKind.INT) caster = INTEGER_CASTER;
                else if (kind == TypeKind.LONG) caster = LONG_CASTER;
                // else if(kind==PrimitiveType.Kind.SHORT) caster=SHORT_CASTER;
            } else if (mirror instanceof DeclaredType) {
                String qname = ((TypeElement)((DeclaredType) mirror).asElement()).getQualifiedName().toString();
                if (qname.equals(Boolean.class.getName())) caster = BOOLEAN_CASTER;
                else if (qname.equals(Byte.class.getName())) caster = BYTE_CASTER;
                else if (qname.equals(Double.class.getName())) caster = DOUBLE_CASTER;
                else if (qname.equals(Float.class.getName())) caster = FLOAT_CASTER;
                else if (qname.equals(Integer.class.getName())) caster = INTEGER_CASTER;

                else if (qname.equals(Long.class.getName())) caster = LONG_CASTER;
                else if (qname.equals(Date.class.getName())) caster = DATE_CASTER;
            }
            if (caster != null) {
                Element casterElem = paramElem.getOwnerDocument().createElementNS(XMLNS_IWRP, "iwrp:caster");
                paramElem.appendChild(casterElem);
                casterElem.setAttribute("class", caster);
            }
        }

        private boolean isBuiltinType(TypeMirror mirror) {
            boolean builtin = isBuiltinArrayType(mirror);
            if (!builtin) builtin = isBuiltinSingleType(mirror);
            return builtin;
        }

        private boolean isBuiltinArrayType(TypeMirror mirror) {
            boolean builtin = false;
            if (mirror instanceof ArrayType) {
                TypeMirror compType = ((ArrayType) mirror).getComponentType();
                builtin = isBuiltinSingleType(compType);
            } else if (mirror instanceof DeclaredType) {
                TypeElement typeElem = (TypeElement)((DeclaredType)mirror).asElement();
                String qname = typeElem.getQualifiedName().toString();
                if (qname.equals("java.util.ArrayList") || qname.equals("java.util.List")) {
                    List<? extends TypeMirror> args = ((DeclaredType)mirror).getTypeArguments();
                    if (args.size() == 1) {
                        TypeMirror arg = args.iterator().next();
                        builtin = isBuiltinSingleType(arg);
                    }
                }
            }
            return builtin;
        }

        private boolean isArrayType(TypeMirror mirror) {
            if (mirror instanceof ArrayType) {
                return true;
            } else if (mirror instanceof DeclaredType) {
                TypeElement typeElem = (TypeElement)((DeclaredType)mirror).asElement();
                String qname = typeElem.getQualifiedName().toString();
                if (qname.equals("java.util.ArrayList") || qname.equals("java.util.List")) {
                    List<? extends TypeMirror> args = ((DeclaredType)mirror).getTypeArguments();
                    if (args.size() == 1) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isBuiltinSingleType(TypeMirror mirror) {
            boolean builtin = false;
            if (mirror.getKind().isPrimitive()) {
                TypeKind kind = ((PrimitiveType) mirror).getKind();
                builtin = builtinPrimitives.contains(kind);
            } else if (mirror instanceof DeclaredType) {
                String qname = ((TypeElement)((DeclaredType) mirror).asElement()).getQualifiedName().toString();
                builtin = builtinTypes.contains(qname);
            }
            return builtin;
        }

        private TypeMirror getArrayComponentType(TypeMirror typeMirror) {
            TypeMirror compType = null;
            if (typeMirror instanceof ArrayType) {
                ArrayType propType = (ArrayType) typeMirror;
                compType = propType.getComponentType();
            } else if (typeMirror instanceof DeclaredType) {
                TypeElement typeElem = (TypeElement)((DeclaredType)typeMirror).asElement();
                String qname = typeElem.getQualifiedName().toString();
                if (qname.equals("java.util.ArrayList") || qname.equals("java.util.List")) {
                    Collection<? extends TypeMirror> args = ((DeclaredType) typeMirror).getTypeArguments();
                    if (args.size() == 1) compType = args.iterator().next();
                }
            }
            return compType;
        }
    }

}
