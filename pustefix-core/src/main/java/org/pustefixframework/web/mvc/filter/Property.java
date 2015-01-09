package org.pustefixframework.web.mvc.filter;

import java.util.Comparator;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class Property implements Filter {

    public enum Compare { LESS, EQUAL, GREATER };
    
    private final String name;
    private final Object value;
    private final Compare compOp;
    
    @SuppressWarnings("rawtypes")
    private final Comparator comparator;
    
    public Property(String name, Object value) {
        this(name, value, Compare.EQUAL);
    }
    
    public Property(String name, Object value, Compare compOp) {
        this(name, value, compOp, null);
    }
    
    public Property(String name, Object value, Compare compOp, Comparator<?> comparator) {
        this.name = name;
        this.value = value;
        this.compOp = compOp;
        this.comparator = comparator;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean isSatisfiedBy(Object candidate) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(candidate);
        Object val = beanWrapper.getPropertyValue(name);
        if(val != null && value != null) {
            if(comparator != null) {
                if(compOp == Compare.LESS) {
                    return comparator.compare(val, value) < 0;
                } else if(compOp == Compare.EQUAL) {
                    return comparator.compare(val, value) == 0;
                } else {
                    return comparator.compare(val, value) > 0;
                }
            } else if(val instanceof Comparable) {
                Comparable cmp = (Comparable)val;
                if(compOp == Compare.LESS) {
                    return cmp.compareTo(value) < 0;
                } else if(compOp == Compare.EQUAL) {
                    return cmp.compareTo(value) == 0;
                } else {
                    return cmp.compareTo(value) > 0;
                }
            } else if(compOp == Compare.LESS) {
                return val.toString().compareTo(value.toString()) < 0;
            } else if(compOp == Compare.GREATER) {
                return val.toString().compareTo(value.toString()) > 0;
            } else {
                return val.equals(value);
            }
        } else {
            if(val == null && value == null && compOp == Compare.EQUAL) {
                return true;
            }
        }
        return false;
    }

}
