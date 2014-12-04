package org.pustefixframework.web.mvc.filter;

public interface Filter {

    public boolean isSatisfiedBy(Object candidate);

}