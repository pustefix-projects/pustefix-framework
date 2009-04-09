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
 *
 */
package de.schlund.pfixcore.oxm;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author mleidig@schlund.de
 */
public class ComplexTestBean {

    private int intVal;
    private boolean boolVal;
    private Float floatVal;
    private String strVal;
    private Boolean[] booleanArray;
    private List<ComplexTestBean> childList;
    private Map<String, ComplexTestBean> childMap;
    public Class<?> type;
    public File file;
    public Properties props;
    public URI uri;

    public ComplexTestBean() {

    }

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public boolean getBoolVal() {
        return boolVal;
    }

    public void setBoolVal(boolean boolVal) {
        this.boolVal = boolVal;
    }

    public Float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(Float floatVal) {
        this.floatVal = floatVal;
    }

    public String getStrVal() {
        return strVal;
    }

    public void setStrVal(String strVal) {
        this.strVal = strVal;
    }

    public Boolean[] getBooleanArray() {
        return booleanArray;
    }

    public void setBooleanArray(Boolean[] booleanArray) {
        this.booleanArray = booleanArray;
    }

    public List<ComplexTestBean> getChildList() {
        return childList;
    }

    public void setChildList(List<ComplexTestBean> childList) {
        this.childList = childList;
    }

    public Map<String, ComplexTestBean> getChildMap() {
        return childMap;
    }

    public void setChildMap(Map<String, ComplexTestBean> childMap) {
        this.childMap = childMap;
    }

}
