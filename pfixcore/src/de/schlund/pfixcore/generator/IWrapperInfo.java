package de.schlund.pfixcore.generator;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.generator.annotation.Caster;
import de.schlund.pfixcore.generator.annotation.PostCheck;
import de.schlund.pfixcore.generator.annotation.PreCheck;
import de.schlund.pfixcore.generator.annotation.Property;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * @author mleidig@schlund.de
 */
public class IWrapperInfo {

    private static Logger                  LOG               = Logger.getLogger(IWrapperInfo.class);
    private static DocumentBuilderFactory  docBuilderFactory = DocumentBuilderFactory.newInstance();
    private static Map<Class<?>, Document> docCache          = new HashMap<Class<?>, Document>();

    public static Document getDocument(Class<? extends IWrapper> iwrpClass, XsltVersion xsltVersion) {
        synchronized (iwrpClass) {
            Document iwrpDoc = null;
            synchronized (docCache) {
                iwrpDoc = docCache.get(iwrpClass);
            }
            if (iwrpDoc == null) {
                try {
                    DocumentBuilder db = docBuilderFactory.newDocumentBuilder();
                    Document doc = db.newDocument();
                    Element root = doc.createElement("iwrapper");
                    root.setAttribute("class", iwrpClass.getName());
                    doc.appendChild(root);
                    IWrapper iw = iwrpClass.newInstance();
                    iw.init("dummy");
                    IWrapperParamDefinition[] defs = iw.gimmeAllParamDefinitions();
                    for (IWrapperParamDefinition def : defs) {
                        Element elem = doc.createElement("param");
                        root.appendChild(elem);
                        elem.setAttribute("name", def.getName());
                        elem.setAttribute("type", def.getType());
                        elem.setAttribute("occurrence", def.getOccurance());
                        elem.setAttribute("frequency", def.getFrequency());
                        String getterName = "get" + def.getName();
                        Method getter = null;
                        try {
                            getter = iwrpClass.getMethod(getterName, new Class[0]);
                        } catch (NoSuchMethodException x) {
                            LOG.warn("Getter not found: " + getterName);
                        }
                        IWrapperParamCaster caster = def.getCaster();
                        if (caster != null) {
                            Element casterElem = doc.createElement("caster");
                            casterElem.setAttribute("class", caster.getClass().getName());
                            elem.appendChild(casterElem);
                            if (getter != null) {
                                Caster casterAnno = getter.getAnnotation(Caster.class);
                                if (casterAnno != null) {
                                    Property[] propAnnos = casterAnno.properties();
                                    for (Property propAnno : propAnnos) {
                                        Element propElem = doc.createElement("property");
                                        casterElem.appendChild(propElem);
                                        propElem.setAttribute("name", propAnno.name());
                                        propElem.setAttribute("value", propAnno.value());
                                    }
                                }
                            }
                        }
                        IWrapperParamPreCheck[] preChecks = def.getPreChecks();
                        if (preChecks.length > 0) {
                            IWrapperParamPreCheck preCheck = preChecks[0];
                            Element preElem = doc.createElement("precheck");
                            elem.appendChild(preElem);
                            preElem.setAttribute("class", preCheck.getClass().getName());
                            if (getter != null) {
                                PreCheck preAnno = getter.getAnnotation(PreCheck.class);
                                if (preAnno != null) {
                                    Property[] propAnnos = preAnno.properties();
                                    for (Property propAnno : propAnnos) {
                                        Element propElem = doc.createElement("property");
                                        preElem.appendChild(propElem);
                                        propElem.setAttribute("name", propAnno.name());
                                        propElem.setAttribute("value", propAnno.value());
                                    }
                                }
                            }
                        }
                        IWrapperParamPostCheck[] postChecks = def.getPostChecks();
                        if (postChecks.length > 0) {
                            IWrapperParamPostCheck postCheck = postChecks[0];
                            Element postElem = doc.createElement("postcheck");
                            elem.appendChild(postElem);
                            postElem.setAttribute("class", postCheck.getClass().getName());
                            if (getter != null) {
                                PostCheck postAnno = getter.getAnnotation(PostCheck.class);
                                if (postAnno != null) {
                                    Property[] propAnnos = postAnno.properties();
                                    for (Property propAnno : propAnnos) {
                                        Element propElem = doc.createElement("property");
                                        postElem.appendChild(propElem);
                                        propElem.setAttribute("name", propAnno.name());
                                        propElem.setAttribute("value", propAnno.value());
                                    }
                                }
                            }
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer t = tf.newTransformer();
                        t.setOutputProperty(OutputKeys.INDENT, "yes");
                        StringWriter writer = new StringWriter();
                        t.transform(new DOMSource(doc), new StreamResult(writer));
                        LOG.debug(writer.toString());
                    }
                    iwrpDoc = Xml.parse(xsltVersion, doc);
                    synchronized (docCache) {
                        docCache.put(iwrpClass, iwrpDoc);
                    }
                } catch (Exception x) {
                    LOG.error("Can't get IWrapper information for " + iwrpClass.getName(), x);
                    throw new RuntimeException("Can't get IWrapper information for " + iwrpClass.getName(), x);
                }
            }
            return iwrpDoc;
        }
    }

}
