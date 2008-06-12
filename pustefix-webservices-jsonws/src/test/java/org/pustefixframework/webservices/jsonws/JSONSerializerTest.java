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
package org.pustefixframework.webservices.jsonws;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.SortedMap;
//import java.util.TreeMap;

import junit.framework.TestCase;
import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import org.pustefixframework.webservices.json.JSONObject;
import org.pustefixframework.webservices.json.parser.JSONParser;

/**
 * @author mleidig@schlund.de
 */
public class JSONSerializerTest extends TestCase {

    BeanDescriptorFactory beanDescFactory;
    SerializerRegistry serializerRegistry;
    DeserializerRegistry deserializerRegistry;
     
    @Override
    protected void setUp() throws Exception {
        beanDescFactory=new BeanDescriptorFactory();
        serializerRegistry=new SerializerRegistry(beanDescFactory);
        deserializerRegistry=new DeserializerRegistry(beanDescFactory);
    }
    
    public void testSerialization() throws Exception {
        JSONSerializer serializer=new JSONSerializer(serializerRegistry);
        JSONDeserializer deserializer=new JSONDeserializer(deserializerRegistry);
        StringWriter writer=new StringWriter();
        TestBean bean=new TestBean();
        bean.setText("test");
        bean.setValue(7);
        List<String> strList=new ArrayList<String>();
        strList.add("foo");
        strList.add("bar");
        bean.setStrList(strList);
        List<Integer> subIntList1=new ArrayList<Integer>();
        subIntList1.add(1);
        subIntList1.add(2);
        List<Integer> subIntList2=new ArrayList<Integer>();
        subIntList2.add(3);
        subIntList2.add(4);
        List<List<Integer>> intList=new ArrayList<List<Integer>>();
        intList.add(subIntList1);
        intList.add(subIntList2);
        bean.setIntList(intList);
        Map<String,String> strMap=new HashMap<String,String>();
        strMap.put("key1","val1");
        strMap.put("key2","val2");
        bean.setStrMap(strMap);
        
        Map<String,Map<String,Integer>> mapMap=new HashMap<String,Map<String,Integer>>();
        Map<String,Integer> subMap1=new HashMap<String,Integer>();
        subMap1.put("val1",1);
        subMap1.put("val2",2);
        mapMap.put("sub1",subMap1);
        Map<String,Integer> subMap2=new HashMap<String,Integer>();
        subMap2.put("val3",3);
        subMap2.put("val4",4);
        mapMap.put("sub2",subMap2);
        bean.setMapMap(mapMap);
        
        HashMap<String,Integer> intMap=new HashMap<String,Integer>();
        intMap.put("one",1);
        intMap.put("two",2);
        intMap.put("three",3);
        bean.setIntMap(intMap);
        
        serializer.serialize(bean,writer);
        String json=writer.toString();
       
        JSONParser parser=new JSONParser(new StringReader(json));
        JSONObject jsonObj=(JSONObject)parser.getJSONValue();
        TestBean refBean=(TestBean)deserializer.deserialize(jsonObj,TestBean.class);
        
        writer=new StringWriter();
        serializer.serialize(refBean,writer);
        String jsonRef=writer.toString();

        //System.out.println(json);
        //System.out.println(jsonRef);
        
        assertEquals(json,jsonRef);
        
    }
    
    /**
    @SuppressWarnings("unchecked")
    public void testReadOnlySerialization() throws Exception {
        
        JSONSerializer serializer=new JSONSerializer(serializerRegistry);
        StringWriter writer=new StringWriter();
        ReadOnlyBean bean=new ReadOnlyBean();
        
        SortedMap<String,Number> numMap=new TreeMap<String,Number>();
        numMap.put("int",new Integer(1));
        numMap.put("float",new Float(2));
        numMap.put("double",new Double(3));
        bean.setNumMap(numMap);
        
        List mixedList=new ArrayList();
        mixedList.add("text");
        mixedList.add(1);
        mixedList.add(true);
        TestBean testBean=new TestBean();
        testBean.setText("test");
        testBean.setValue(7);
        mixedList.add(testBean);
        bean.setMixedList(mixedList);
        
        serializer.serialize(bean,writer);
        String json=writer.toString();
        String jsonRef="{\"numMap\":{\"double\":3.0,\"float\":2.0,\"int\":1},\"mixedList\":[\"text\",1,true," +
                "{\"intMap\":null,\"intList\":null,\"value\":7,\"text\":\"test\",\"mapMap\":null,\"strMap\":null,\"strList\":null}]}";
        
        //System.out.println(json);
        
        assertEquals(json,jsonRef);
        
    }
    */
    
}
