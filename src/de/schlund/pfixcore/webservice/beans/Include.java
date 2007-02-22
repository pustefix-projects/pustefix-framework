package de.schlund.pfixcore.webservice.beans;

import java.lang.annotation.*;

/**
 * @author mleidig@schlund.de
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Include {
    
}
