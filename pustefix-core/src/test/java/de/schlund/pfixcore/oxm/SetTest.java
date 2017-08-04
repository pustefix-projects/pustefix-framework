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

import org.pustefixframework.test.XmlAssert;
import org.w3c.dom.Document;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.bean.SetTestBean;
import de.schlund.pfixcore.oxm.helper.OxmTestHelper;
import de.schlund.pfixcore.oxm.impl.MarshallerImpl;
import de.schlund.pfixcore.oxm.impl.SerializerRegistry;
import junit.framework.TestCase;

/**
 * @author Dunja Fehrenbach <dunja.fehrenbach@1und1.de>
 */
public class SetTest extends TestCase {

    /**
     * Test set marshalling
     */
    public void testSet() {
        BeanDescriptorFactory bdf = new BeanDescriptorFactory();
        SerializerRegistry reg = new SerializerRegistry(bdf);
        Marshaller m = new MarshallerImpl(reg);

        SetTestBean bean = new SetTestBean();

        Document doc = OxmTestHelper.createResultDocument();
        Result res = new DOMResult(doc);
        m.marshal(bean, res);
        String expected = "<result><mySet><entry><string>foo</string></entry><entry><string>bar</string></entry></mySet><annoSet><element><string>foo</string></element><element><string>bar</string></element></annoSet></result>";
        Document expDoc = OxmTestHelper.createDocument(expected);
        XmlAssert.assertEqualsUnordered(expDoc, doc);
    }
}
