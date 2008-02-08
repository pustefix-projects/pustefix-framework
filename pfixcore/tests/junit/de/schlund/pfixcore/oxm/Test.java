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
package de.schlund.pfixcore.oxm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.impl.MarshallerImpl;
import de.schlund.pfixcore.oxm.impl.SerializerRegistry;
import de.schlund.pfixxml.util.XMLUtils;

/**
 * @author mleidig@schlund.de
 */
public class Test extends TestCase {

    @Override
    protected void setUp() throws Exception {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger = Logger.getRootLogger();
        logger.setLevel((Level) Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
    }

    public void testEnums() {

        BeanDescriptorFactory bdf = new BeanDescriptorFactory();
        SerializerRegistry reg = new SerializerRegistry(bdf);
        Marshaller m = new MarshallerImpl(reg);

        // Bean with enums

        EnumTestBean bean = new EnumTestBean();
        bean.setText("abc");
        bean.setDay(WeekDay.FRIDAY);
        bean.setPrio(EnumTestBean.Priority.HIGH);

        Document doc = createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        String expected = "<result prio=\"HIGH\" text=\"abc\"><day name=\"FRIDAY\" workingHours=\"6\"/></result>";
        Document expDoc = createDocument(expected);
        XMLUtils.assertEquals(expDoc, doc);

        // List with enums

        List<Enum<?>> elist = new ArrayList<Enum<?>>();
        elist.add(WeekDay.MONDAY);
        elist.add(EnumTestBean.Priority.LOW);

        doc = createResultDocument();
        res = new DOMResult(doc);
        m.marshal(elist, res);
        expected = "<result><weekDay workingHours=\"8\" name=\"MONDAY\"/><priority>LOW</priority></result>";
        expDoc = createDocument(expected);
        XMLUtils.assertEquals(expDoc, doc);

    }

    public void testComplex() throws Exception {

        BeanDescriptorFactory bdf = new BeanDescriptorFactory();
        SerializerRegistry reg = new SerializerRegistry(bdf);
        Marshaller m = new MarshallerImpl(reg);

        ComplexTestBean bean = new ComplexTestBean();
        bean.setBoolVal(true);
        bean.setIntVal(1);
        bean.setFloatVal(new Float(2.2));
        bean.setStrVal("aaa");
        bean.setBooleanArray(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
        bean.type = ComplexTestBean.class;
        bean.file = new File("/tmp");
        Properties props = new Properties();
        props.setProperty("key1", "val1");
        props.setProperty("key2", "val2");
        bean.props = props;
        bean.uri = new URI("http://pustefix-framework.org");

        ComplexTestBean bean1 = new ComplexTestBean();
        bean1.setBoolVal(false);
        bean1.setIntVal(2);
        bean1.setFloatVal(new Float(3.3));
        bean1.setStrVal("bbb");
        bean1.setBooleanArray(new Boolean[] { Boolean.TRUE, Boolean.TRUE });

        ComplexTestBean bean2 = new ComplexTestBean();
        bean2.setBoolVal(true);
        bean2.setIntVal(3);
        bean2.setFloatVal(new Float(4.4));
        bean2.setStrVal("ccc");
        bean2.setBooleanArray(new Boolean[] { Boolean.FALSE, Boolean.FALSE });

        List<ComplexTestBean> beanList = new ArrayList<ComplexTestBean>();
        beanList.add(bean1);
        beanList.add(bean2);
        bean.setChildList(beanList);

        Map<String, ComplexTestBean> beanMap = new HashMap<String, ComplexTestBean>();
        beanMap.put(bean2.getStrVal(), bean2);
        beanMap.put(bean1.getStrVal(), bean1);
        bean.setChildMap(beanMap);

        Document expDoc = createDocument(getInputStream("testcomplex.xml"));
        XMLUtils.stripWhitespace(expDoc);
        Document doc = createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        // printDocument(doc);
        XMLUtils.assertEquals(expDoc, doc);

    }

    private Document createResultDocument() {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("result");
            doc.appendChild(root);
            return doc;
        } catch (Exception x) {
            throw new RuntimeException("Can't create document", x);
        }
    }

    private Document createDocument(String str) {
        try {
            StringReader reader = new StringReader(str);
            InputSource src = new InputSource();
            src.setCharacterStream(reader);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
            return doc;
        } catch (Exception x) {
            throw new RuntimeException("Can't create document", x);
        }
    }

    private Document createDocument(InputStream in) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            return doc;
        } catch (Exception x) {
            throw new RuntimeException("Can't create document", x);
        }
    }

    private InputStream getInputStream(String fileName) {
        InputStream in = getClass().getClassLoader().getResourceAsStream("de/schlund/pfixcore/oxm/" + fileName);
        if (in == null) {
            try {
                in = new FileInputStream("tests/junit/de/schlund/pfixcore/oxm/" + fileName);
            } catch (FileNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return in;
    }

    @SuppressWarnings("unused")
    private void printDocument(Document doc) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(System.out));
        } catch (Exception x) {
            throw new RuntimeException("Can't print document", x);
        }
    }

}
