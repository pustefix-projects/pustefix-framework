package org.pustefixframework.web.mvc.filter;

public class Not implements Filter {

    private final Filter filter;

    public Not(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    @Override
    public boolean isSatisfiedBy(Object candidate) {
        return !filter.isSatisfiedBy(candidate);
    }

}