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
import de.schlund.pfixcore.oxm.bean.MapTestBean;
import de.schlund.pfixcore.oxm.helper.OxmTestHelper;
import de.schlund.pfixcore.oxm.impl.MarshallerImpl;
import de.schlund.pfixcore.oxm.impl.SerializerRegistry;

/**
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class MapTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger = Logger.getRootLogger();
        logger.setLevel((Level) Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
    }

    /**
     * Test map marshalling
     */
    public void testMap() {
        BeanDescriptorFactory bdf = new BeanDescriptorFactory();
        SerializerRegistry reg = new SerializerRegistry(bdf);
        Marshaller m = new MarshallerImpl(reg);

        MapTestBean bean = new MapTestBean();

        Document doc = OxmTestHelper.createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        String expected = "<result><annoMap><element><string>one</string><string>foo</string></element><element><string>two</string><string>bar</string></element></annoMap><myMap><entry><string>one</string><string>foo</string></entry><entry><string>two</string><string>bar</string></entry></myMap></result>";
        Document expDoc = OxmTestHelper.createDocument(expected);
        XmlAssert.assertEqualsUnordered(expDoc, doc);
    }
}