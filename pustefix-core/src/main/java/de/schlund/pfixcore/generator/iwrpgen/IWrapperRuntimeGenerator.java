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

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

import de.schlund.pfixcore.generator.annotation.Caster;
import de.schlund.pfixcore.generator.annotation.IWrapper;
import de.schlund.pfixcore.generator.annotation.Param;
import de.schlund.pfixcore.generator.annotation.PostCheck;
import de.schlund.pfixcore.generator.annotation.PreCheck;
import de.schlund.pfixcore.generator.annotation.Property;

public class IWrapperRuntimeGenerator {

    private final static String    XMLNS_IWRP     = "http://www.pustefix-framework.org/2008/namespace/iwrapper";
    private final static String    DEFAULT_SUFFIX = "Wrapper";

    private static DocumentBuilder docBuilder;

    private static Document createDocument() {
        if (docBuilder == null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            try {
                docBuilder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException x) {
                throw new RuntimeException("Can't create DocumentBuilder", x);
            }
        }
        docBuilder.reset();
        Document doc = docBuilder.newDocument();
        return doc;
    }

    @SuppressWarnings("rawtypes")
    public static void generate(Class<?> clazz, File genSrcDir) {

        BeanDescriptor beanDesc = new BeanDescriptor(clazz);

        Document doc = createDocument();
        Element root = doc.createElementNS(XMLNS_IWRP, "iwrp:interface");
        doc.appendChild(root);

        Class<?> beanClass = beanDesc.getBeanClass();
        String beanFullName = beanClass.getName();

        String iwrapperClass = beanFullName + DEFAULT_SUFFIX;

        IWrapper iwrapper = beanClass.getAnnotation(IWrapper.class);
        if (iwrapper != null) {

            if (!iwrapper.name().equals("")) {
                if (iwrapper.name().contains("."))
                    iwrapperClass = iwrapper.name();
                else {
                    int ind = beanFullName.lastIndexOf(".");
                    if (ind == -1)
                        iwrapperClass = iwrapper.name();
                    else
                        iwrapperClass = beanFullName.substring(0, ind) + "." + iwrapper.name();
                }
            }
            System.out.println("NAME: " + iwrapperClass);

            Class<?> ihandlerType = iwrapper.ihandler();
            if (ihandlerType != void.class) {
                Element ihandlerElem = doc.createElementNS(XMLNS_IWRP, "iwrp:ihandler");
                root.appendChild(ihandlerElem);
                ihandlerElem.setAttribute("class", ihandlerType.getName());
            }
        }

        Set<String> properties = beanDesc.getProperties();
        for (String property : properties) {
            Element paramElem = doc.createElementNS(XMLNS_IWRP, "iwrp:param");
            root.appendChild(paramElem);
            String paramName = Character.toUpperCase(property.charAt(0)) + property.substring(1);
            paramElem.setAttribute("name", paramName);

            Type targetType = beanDesc.getPropertyType(property);

            Class<?> targetClass = null;
            if (targetType instanceof Class)
                targetClass = (Class<?>) targetType;
            else if (targetType instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) targetType).getRawType();
                if (rawType instanceof Class)
                    targetClass = (Class<?>) rawType;
                else
                    throw new RuntimeException("Type not supported: " + targetType);
            } else
                throw new RuntimeException("Type not supported: " + targetType);

            if (targetClass.isArray()) {
                Class<?> compType = targetClass.getComponentType();
                paramElem.setAttribute("frequency", "multiple");
                paramElem.setAttribute("type", compType.getName());
            } else if (List.class.isAssignableFrom(targetClass)) {
                Type argType = null;
                if (targetType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) targetType;
                    Type[] argTypes = paramType.getActualTypeArguments();
                    if (argTypes.length == 1) {
                        argType = argTypes[0];
                        if (!(argType instanceof Class)) throw new RuntimeException("Type not supported: " + argType);
                    } else
                        throw new RuntimeException("Type not supported: " + targetType);
                } else
                    throw new RuntimeException("Unparameterized List types aren't supported: " + targetType);
                
                List list = null;
                if (!targetClass.isInterface()) {
                    try {
                        list = (List) targetClass.newInstance();
                    } catch (Exception x) {}
                }
                if (list == null && !targetClass.isAssignableFrom(ArrayList.class)) {
                    throw new RuntimeException("Can't create instance of class '" + targetClass.getName() + "'.");
                }
                paramElem.setAttribute("frequency", "multiple");
                paramElem.setAttribute("type", ((Class<?>) argType).getName());
            } else {
                paramElem.setAttribute("type", targetClass.getName());
            }

            Method getter = beanDesc.getGetMethod(property);
            Field field = beanDesc.getDirectAccessField(property);

            Param param = getter.getAnnotation(Param.class);
            if (param == null && field != null) param = field.getAnnotation(Param.class);

            String occurStr = "mandatory";
            if (param != null) occurStr = param.mandatory() ? "mandatory" : "optional";
            paramElem.setAttribute("occurrence", occurStr);

            boolean trim = true;
            if (param != null) trim = param.trim();
            paramElem.setAttribute("trim", String.valueOf(trim));

            String misscode = "";
            if (param != null) misscode = param.missingscode();
            if (!misscode.equals("")) paramElem.setAttribute("missingscode", misscode);

            Caster caster = getter.getAnnotation(Caster.class);
            if (caster == null && field != null) caster = field.getAnnotation(Caster.class);
            if (caster != null) {
                Element casterElem = doc.createElementNS(XMLNS_IWRP, "iwrp:caster");
                paramElem.appendChild(casterElem);
                Class<?> casterType = caster.type();
                casterElem.setAttribute("class", casterType.getName());
                Property[] casterProps = caster.properties();
                if (casterProps != null) {
                    for (Property casterProp : casterProps) {
                        Element propElem = doc.createElementNS(XMLNS_IWRP, "iwrp:cparam");
                        casterElem.appendChild(propElem);
                        propElem.setAttribute("name", casterProp.name());
                        propElem.setAttribute("value", casterProp.value());
                    }
                }
            }

            PreCheck preCheck = getter.getAnnotation(PreCheck.class);
            if (preCheck == null && field != null) preCheck = field.getAnnotation(PreCheck.class);
            if (preCheck != null) {
                Element preCheckElem = doc.createElementNS(XMLNS_IWRP, "iwrp:precheck");
                paramElem.appendChild(preCheckElem);
                Class<?> preCheckType = preCheck.type();
                preCheckElem.setAttribute("class", preCheckType.getName());
                Property[] preCheckProps = preCheck.properties();
                if (preCheckProps != null) {
                    for (Property preCheckProp : preCheckProps) {
                        Element propElem = doc.createElementNS(XMLNS_IWRP, "iwrp:cparam");
                        preCheckElem.appendChild(propElem);
                        propElem.setAttribute("name", preCheckProp.name());
                        propElem.setAttribute("value", preCheckProp.value());
                    }
                }
            }

            PostCheck postCheck = getter.getAnnotation(PostCheck.class);
            if (postCheck == null && field != null) postCheck = field.getAnnotation(PostCheck.class);
            if (postCheck != null) {
                Element postCheckElem = doc.createElementNS(XMLNS_IWRP, "iwrp:postcheck");
                paramElem.appendChild(postCheckElem);
                Class<?> postCheckType = postCheck.type();
                postCheckElem.setAttribute("class", postCheckType.getName());
                Property[] postCheckProps = postCheck.properties();
                if (postCheckProps != null) {
                    for (Property postCheckProp : postCheckProps) {
                        Element propElem = doc.createElementNS(XMLNS_IWRP, "iwrp:cparam");
                        postCheckElem.appendChild(propElem);
                        propElem.setAttribute("name", postCheckProp.name());
                        propElem.setAttribute("value", postCheckProp.value());
                    }
                }
            }

        }

        // try {
        // TransformerFactory tf=TransformerFactory.newInstance();
        // Transformer t=tf.newTransformer();
        // t.setOutputProperty(OutputKeys.INDENT,"yes");
        // t.transform(new DOMSource(doc),new StreamResult(System.out));
        // } catch(Exception x) {
        // x.printStackTrace();
        // }

        String destPath = iwrapperClass.replace('.', '/') + ".java";
        File destFile = new File(genSrcDir, destPath);
        File destDir = destFile.getParentFile();
        if (!destDir.exists()) destDir.mkdirs();
        System.out.println(destFile.getAbsolutePath());
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.transform(new DOMSource(doc), new StreamResult(System.out));

            t = tf.newTransformer(new StreamSource(IWrapperRuntimeGenerator.class.getResourceAsStream("/pustefix/xsl/iwrapper.xsl")));
            t.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(destFile)));
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

}
