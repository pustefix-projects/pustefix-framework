package org.pustefixframework.container.spring.beans;

import org.springframework.beans.BeansException;

/**
 * Exception which can be thrown when a given BeanFactory implementation
 * isn't supported, e.g. when the given implementation doesn't implement
 * a required interface.
 * 
 * @author mleidig
 *
 */
public class BeanFactoryImplNotSupportedException extends BeansException {
    
    private static final long serialVersionUID = 9085851219118895102L;

    public BeanFactoryImplNotSupportedException(String msg) {
        super(msg);
    }

}
