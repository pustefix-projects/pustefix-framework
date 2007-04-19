/*
 * de.schlund.pfixcore.example.webservices.TypeTestImpl
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * TypeTestImpl.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
public class TypeTestImpl implements TypeTest {
    
    public String info() {
        return "TypeTest";
    }
    
    public byte echoByte(byte val) {
        return val;
    }
    
    public Byte echoByteObj(Byte val) {
        return val;
    }
    
    public short echoShort(short val) {
        return val;
    }
    
    public Short echoShortObj(Short val) {
        return val;
    }
    
    public int echoInt(int val) {
        return val;
    }
    
    public Integer echoIntObj(Integer val) {
        return val;
    }
    
    public int[] echoIntArray(int[] vals) {
        return vals;
    }
    
    public long echoLong(long val) {
        return val;
    }
    
    public Long echoLongObj(Long val) {
        return val;
    }
    
    public long[] echoLongArray(long[] vals) {
        return vals;
    }
    
    public float echoFloat(float val) {
        return val;
    }
    
    public Float echoFloatObj(Float val) {
        return val;
    }
    
    public float[] echoFloatArray(float[] vals) {
        return vals;
    }
    
    public double echoDouble(double val) {
        return val;
    }
    
    public Double echoDoubleObj(Double val) {
        return val;
    }
    
    public boolean echoBoolean(boolean val) {
        return val;
    }
    
    public Boolean echoBooleanObj(Boolean val) {
        return val;
    }
    
    public boolean[] echoBooleanArray(boolean[] vals) {
        return vals;
    }
    
    public Calendar echoCalendar(Calendar date) {
        return date;
    }
    
    public Calendar[] echoCalendarArray(Calendar[] dates) {
        return dates;
    }
    
    public Date echoDate(Date date) {
        return date;
    }
    
    public String echoString(String str) {
        return str;
    }
    
    public String[] echoStringArray(String[] strs) {
        return strs;
    }
    
    public String[][] echoStringMultiArray(String[][] strs) {
        return strs;
    }
    
    public Object echoObject(Object obj) {
        return obj;
    }
    
    public Object[] echoObjectArray(Object[] objs) {
        return objs;
    }
    
    public Element echoElement(Element elem) {
        return elem;
    }
    
    public Element[] echoElementArray(Element[] elems) {
        return elems;
    }
    
    public DataBean echoDataBean(DataBean data) {
        return data;
    }
    
    public DataBean[] echoDataBeanArray(DataBean[] data) {
        return data;
    }
    
    public List<String> echoStringList(List<String> stringList) {
        return stringList;
    }
    
    public List<DataBean> echoDataBeanList(List<DataBean> dataBeanList) {
        return dataBeanList;
    }
    
    public Map<String,String> echoStringMap(Map<String,String> map) {
        return map;
    }
    
    public Map<String,DataBean> echoDataBeanMap(Map<String,DataBean> dataBeanMap) {
        return dataBeanMap;
    }
    
    public BeanImpl[] echoBeanArray(BeanImpl[] beanArray) {
        return beanArray;
    }
    
}
