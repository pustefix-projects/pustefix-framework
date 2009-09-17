package org.pustefixframework.testmodule;

public class HelloImpl implements Hello {

	public String sayHello(String name) {
		return "Hello "+name+"!!!";
	}

	public Bean test(Bean bean) {
		return bean;
	}
	
}
