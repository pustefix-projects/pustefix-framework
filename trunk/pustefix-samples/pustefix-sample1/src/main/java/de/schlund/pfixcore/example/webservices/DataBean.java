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

/**
 * DataBean.java 
 * 
 * Created: 28.06.2004
 * 
 * @author mleidig
 */
public class DataBean {
    
    String name;
    Calendar date;
    int intVal;
    float[] floatVals;
    Boolean boolVal;
    DataBean[] children;
    
    public DataBean() {}
    
    public DataBean(String name,Calendar date,int intVal,float[] floatVals) {
        this.name=name;
        this.date=date;
        this.intVal=intVal;
        this.floatVals=floatVals;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name=name;
    }
    
    public Calendar getDate() {
        return date;
    }
    
    public void setDate(Calendar date) {
        this.date=date;
    }
    
    public int getIntVal() {
        return intVal;
    }
    
    public void setIntVal(int intVal) {
        this.intVal=intVal;
    }
    
    public float[] getFloatVals() {
        return floatVals;
    }
    
    public void setFloatVals(float[] floatVals) {
        this.floatVals=floatVals;
    }
    
    public DataBean[] getChildren() {
        return children;
    }
   
    public void setChildren(DataBean[] children) {
        this.children=children;
    }
    
    public Boolean getBoolVal() {
        return boolVal;
    }
    
    public void setBoolVal(Boolean boolVal) {
        this.boolVal=boolVal;
    }
    
}
