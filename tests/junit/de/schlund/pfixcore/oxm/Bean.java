package de.schlund.pfixcore.oxm;

import java.util.List;
import java.util.Map;

public class Bean implements BeanItf {
    
    private int intVal;
    private boolean boolVal;
    private  Float floatVal;
    private String strVal;
    private Boolean[] booleanArray;
    private List<Bean> childList;
    private Map<String,Bean> childMap;
    
    public Bean() {
        
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getIntVal()
     */
    public int getIntVal() {
        return intVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setIntVal(int)
     */
    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getBoolVal()
     */
    public boolean getBoolVal() {
        return boolVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setBoolVal(boolean)
     */
    public void setBoolVal(boolean boolVal) {
        this.boolVal = boolVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getFloatVal()
     */
    public Float getFloatVal() {
        return floatVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setFloatVal(java.lang.Float)
     */
    public void setFloatVal(Float floatVal) {
        this.floatVal = floatVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getStrVal()
     */
    public String getStrVal() {
        return strVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setStrVal(java.lang.String)
     */
    public void setStrVal(String strVal) {
        this.strVal = strVal;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getBooleanArray()
     */
    public Boolean[] getBooleanArray() {
        return booleanArray;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setBooleanArray(java.lang.Boolean[])
     */
    public void setBooleanArray(Boolean[] booleanArray) {
        this.booleanArray = booleanArray;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getChildList()
     */
    public List<Bean> getChildList() {
        return childList;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setChildList(java.util.List)
     */
    public void setChildList(List<Bean> childList) {
        this.childList = childList;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#getChildMap()
     */
    public Map<String, Bean> getChildMap() {
        return childMap;
    }

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.oxm.BeanItf#setChildMap(java.util.Map)
     */
    public void setChildMap(Map<String, Bean> childMap) {
        this.childMap = childMap;
    }
    
}
