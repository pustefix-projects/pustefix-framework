package org.pustefixframework.web.mvc.internal;

import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

public class ControllerStateAdapter {

    private AnnotationMethodHandlerAdapter adapter;
    
    public void setAdapter(AnnotationMethodHandlerAdapter adapter) {
        this.adapter = adapter;
    }
    
    public AnnotationMethodHandlerAdapter getAdapter() {
        return adapter;
    }
    
}
