package org.pustefixframework.web.mvc.filter;

public abstract class Junction implements Filter {

    private final Filter[] filters;

    public Junction(Filter... filters) {
        this.filters = filters;
    }

    public Filter[] getFilters() {
        return filters;
    }

}
