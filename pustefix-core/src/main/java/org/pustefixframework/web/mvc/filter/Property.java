package org.pustefixframework.web.mvc.filter;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class Property implements Filter {

    private final String name;
    private final String value;

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isSatisfiedBy(Object candidate) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(candidate);
        Object val = beanWrapper.getPropertyValue(name);
        if(val != null && String.valueOf(val).equals(value)) {
            return true;
        }
        return false;
    }

}
