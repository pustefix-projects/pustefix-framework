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
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.w3c.dom.Element;

/**
 * TypeTestImpl.java 
 * 
 * Created: 30.07.2004
 * 
 * @author mleidig
 */
@WebService
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
    
    @WebMethod(exclude=true)
    public Element echoElement(Element elem) {
        return elem;
    }
    
    @WebMethod(exclude=true)
    public Element[] echoElementArray(Element[] elems) {
        return elems;
    }
    
    public DataBean echoDataBean(DataBean data) {
        return data;
    }
    
    public DataBean[] echoDataBeanArray(DataBean[] data) {
        return data;
    }
    
    @WebMethod(exclude=true)
    public List<String> echoStringList(List<String> stringList) {
        return stringList;
    }
    
    @WebMethod(exclude=true)
    public List<DataBean> echoDataBeanList(List<DataBean> dataBeanList) {
        return dataBeanList;
    }
    
    @WebMethod(exclude=true)
    public Map<String,String> echoStringMap(Map<String,String> map) {
        return map;
    }
    
    @WebMethod(exclude=true)
    public Map<String,DataBean> echoDataBeanMap(Map<String,DataBean> dataBeanMap) {
        return dataBeanMap;
    }
    
    public BeanImpl[] echoBeanArray(BeanImpl[] beanArray) {
        return beanArray;
    }
    
}
