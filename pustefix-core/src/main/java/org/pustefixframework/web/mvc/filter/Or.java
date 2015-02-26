package org.pustefixframework.web.mvc.filter;

public class Or extends Junction {

    public Or(Filter... filters) {
        super(filters);
    }

    @Override
    public boolean isSatisfiedBy(Object candidate) {
        for(Filter filter: getFilters()) {
            if(filter.isSatisfiedBy(candidate)) {
                return true;
            }
        }
        return false;
    }

}