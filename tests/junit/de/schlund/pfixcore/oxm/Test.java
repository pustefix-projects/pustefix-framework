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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.oxm.impl.MarshallerImpl;
import de.schlund.pfixcore.oxm.impl.SerializerRegistry;


/**
 * @author mleidig@schlund.de
 */
public class Test {
    
    public static void main(String[] args) throws Exception {
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
        
        Bean bean=new Bean();
        bean.setBoolVal(true);
        bean.setIntVal(1);
        bean.setFloatVal(new Float(2.2));
        bean.setStrVal("aaa");
        bean.setBooleanArray(new Boolean[] {Boolean.TRUE,Boolean.FALSE});
        
        Bean bean1=new Bean();
        bean1.setBoolVal(false);
        bean1.setIntVal(2);
        bean1.setFloatVal(new Float(3.3));
        bean1.setStrVal("bbb");
        bean1.setBooleanArray(new Boolean[] {Boolean.TRUE,Boolean.TRUE});
        
        Bean bean2=new Bean();
        bean2.setBoolVal(true);
        bean2.setIntVal(3);
        bean2.setFloatVal(new Float(4.4));
        bean2.setStrVal("ccc");
        bean2.setBooleanArray(new Boolean[] {Boolean.FALSE,Boolean.FALSE});
       
        List<Bean> beanList=new ArrayList<Bean>();
        beanList.add(bean1);
        beanList.add(bean2);
        bean.setChildList(beanList);
        
        Map<String,Bean> beanMap=new HashMap<String,Bean>();
        beanMap.put(bean2.getStrVal(),bean2);
        beanMap.put(bean1.getStrVal(),bean1);
        bean.setChildMap(beanMap);
        
        Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root=doc.createElement("result");
        doc.appendChild(root);
        Result res=new DOMResult(doc);
        
        BeanDescriptorFactory bdf=new BeanDescriptorFactory();
        SerializerRegistry reg=new SerializerRegistry(bdf);
        Marshaller m=new MarshallerImpl(reg);
        m.marshal(beanMap,res);
        
        Transformer t=TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT,"yes");
        t.transform(new DOMSource(doc),new StreamResult(System.out));
        
        
    }

}
