package org.pustefixframework.web.mvc.filter;

public class And extends Junction {

    public And(Filter... filters) {
        super(filters);
    }    

    @Override
    public boolean isSatisfiedBy(Object candidate) {
        for(Filter filter: getFilters()) {
            if(!filter.isSatisfiedBy(candidate)) {
                return false;
            }
        }
        return true;
    }

}