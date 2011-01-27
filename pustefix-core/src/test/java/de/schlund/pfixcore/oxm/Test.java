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
package de.schlund.pfixcore.oxm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.pustefixframework.test.XmlAssert;
import org.w3c.dom.Document;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.helper.OxmTestHelper;
import de.schlund.pfixcore.oxm.impl.MarshallerImpl;
import de.schlund.pfixcore.oxm.impl.SerializerRegistry;
import de.schlund.pfixxml.util.XMLUtils;

/**
 * @author mleidig@schlund.de
 * @author  Stephan Schmidt <schst@stubbles.net>
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

        Document doc = OxmTestHelper.createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        String expected = "<result prio=\"HIGH\" text=\"abc\"><day name=\"FRIDAY\" workingHours=\"6\"/></result>";
        Document expDoc = OxmTestHelper.createDocument(expected);
        XmlAssert.assertEquals(expDoc, doc);

        // List with enums

        List<Enum<?>> elist = new ArrayList<Enum<?>>();
        elist.add(WeekDay.MONDAY);
        elist.add(EnumTestBean.Priority.LOW);

        doc = OxmTestHelper.createResultDocument();
        res = new DOMResult(doc);
        m.marshal(elist, res);
        expected = "<result><weekDay workingHours=\"8\" name=\"MONDAY\"/><priority>LOW</priority></result>";
        expDoc = OxmTestHelper.createDocument(expected);
        XmlAssert.assertEquals(expDoc, doc);
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
        bean.file = new File(File.separator + "tmp");
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

        Document expDoc = OxmTestHelper.createDocument(getInputStream("testcomplex.xml"));
        XMLUtils.stripWhitespace(expDoc);
        Document doc = OxmTestHelper.createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        XmlAssert.assertEqualsUnordered(expDoc, doc);
    }

    public void testFragment() throws Exception {
        BeanDescriptorFactory bdf = new BeanDescriptorFactory();
        SerializerRegistry reg = new SerializerRegistry(bdf);
        Marshaller m = new MarshallerImpl(reg);

        FragmentBean fBean = new FragmentBean();

        Document expDoc = OxmTestHelper.createDocument(getInputStream("testfragment.xml"));
        XMLUtils.stripWhitespace(expDoc);
        Document doc = OxmTestHelper.createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(fBean, res);
        XmlAssert.assertEquals(expDoc, doc);
    }

    public void testForceElement() throws Exception {
        BeanDescriptorFactory bdf = new BeanDescriptorFactory();
        SerializerRegistry reg = new SerializerRegistry(bdf);
        Marshaller m = new MarshallerImpl(reg);

        ForceElementTestBean bean = new ForceElementTestBean();

        Document doc = OxmTestHelper.createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        
        String expected = "<result><foo>foo</foo><baz>bar</baz><openingDate>2008-03-02 18:43:12</openingDate></result>";
        Document expDoc = OxmTestHelper.createDocument(expected);
        
        XmlAssert.assertEqualsUnordered(expDoc, doc);
    }
    
    private InputStream getInputStream(String fileName) {
        InputStream in = getClass().getClassLoader().getResourceAsStream("de/schlund/pfixcore/oxm/" + fileName);
        if (in == null) {
            try {
                in = new FileInputStream("src/test/java/de/schlund/pfixcore/oxm/" + fileName);
            } catch (FileNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return in;
    }
}