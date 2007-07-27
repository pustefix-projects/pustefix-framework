package de.schlund.pfixcore.oxm;

import java.util.List;
import java.util.Map;

public interface BeanItf {

    public int getIntVal();

    public void setIntVal(int intVal);

    public boolean getBoolVal();

    public void setBoolVal(boolean boolVal);

    public Float getFloatVal();

    public void setFloatVal(Float floatVal);

    public String getStrVal();

    public void setStrVal(String strVal);

    public Boolean[] getBooleanArray();

    public void setBooleanArray(Boolean[] booleanArray);

    public List<Bean> getChildList();

    public void setChildList(List<Bean> childList);

    public Map<String, Bean> getChildMap();

    public void setChildMap(Map<String, Bean> childMap);

}