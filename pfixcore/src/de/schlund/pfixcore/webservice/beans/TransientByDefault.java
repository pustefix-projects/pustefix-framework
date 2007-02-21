package de.schlund.pfixcore.webservice.beans;

import java.lang.annotation.*;

/**
 * @author mleidig@schlund.de
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransientByDefault {
    
}
