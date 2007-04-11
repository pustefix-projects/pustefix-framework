/*
 * Created on 04.04.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.jsonws;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import de.schlund.pfixcore.webservice.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.json.parser.JSONParser;

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
    
}
