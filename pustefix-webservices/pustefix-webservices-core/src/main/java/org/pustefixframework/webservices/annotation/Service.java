package org.pustefixframework.webservices.annotation;

import java.lang.annotation.*;

/**
 * @author mleidig@schlund.de
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

	public enum Scope {REQUEST,SESSION,APPLICATION};
	public enum Protocol {JSON};
	
	String name();
	Scope scope() default Scope.APPLICATION;
	Protocol  protocol() default Protocol.JSON;
	
}
