/*
 * de.schlund.pfixcore.example.webservices.TypeTest
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * TypeTest.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public interface TypeTest {
    
    public String info();
    
    public byte echoByte(byte val);
    
    public Byte echoByteObj(Byte val);
    
    public short echoShort(short val);
    
    public Short echoShortObj(Short val);
    
    public int echoInt(int val);
    
    public Integer echoIntObj(Integer val);
    
    public int[] echoIntArray(int[] vals);
    
    public long echoLong(long val);
    
    public Long echoLongObj(Long val);
    
    public long[] echoLongArray(long[] vals);
   
    public float echoFloat(float val);
    
    public Float echoFloatObj(Float val);
    
    public float[] echoFloatArray(float[] vals);
   
    public double echoDouble(double val);
    
    public Double echoDoubleObj(Double val);
    
    public boolean echoBoolean(boolean val);
    
    public Boolean echoBooleanObj(Boolean val);
    
    public boolean[] echoBooleanArray(boolean[] vals);
    
    public Date echoDate(Date date);
    
    public Calendar echoCalendar(Calendar date);
    
    public Calendar[] echoCalendarArray(Calendar[] dates);
    
    public String echoString(String str);
    
    public String[] echoStringArray(String[] strs);
    
    public String[][] echoStringMultiArray(String[][] strs); 
    
    public Object echoObject(Object obj);
    
    public Object[] echoObjectArray(Object[] objs);
    
    public Element echoElement(Element elem);
    
    public Element[] echoElementArray(Element[] elems);
    
    public DataBean echoDataBean(DataBean data);
    
    public DataBean[] echoDataBeanArray(DataBean[] data);
    
    public List<String> echoStringList(List<String> stringList);
    
    public List<DataBean> echoDataBeanList(List<DataBean> dataBeanList);
    
    public Map<String,String> echoStringMap(Map<String,String> stringMap);
    
    public Map<String,DataBean> echoDataBeanMap(Map<String,DataBean> dataBeanMap);
    
}
