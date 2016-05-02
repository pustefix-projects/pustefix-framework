package org.pustefixframework.webservices.jsonws;

public class EnumTestBean {

	public enum InnerEnum { RED, GREEN, BLUE };
	
	private SimpleEnum simpleEnum;
	private ComplexEnum complexEnum;
	private InnerEnum innerEnum;
	
	public void setSimpleEnum(SimpleEnum simpleEnum) {
		this.simpleEnum = simpleEnum;
	}
	
	public SimpleEnum getSimpleEnum() {
		return simpleEnum;
	}
	
	public void setComplexEnum(ComplexEnum complexEnum) {
		this.complexEnum = complexEnum;
	}
	
	public ComplexEnum getComplexEnum() {
		return complexEnum;
	}
	
	public void setInnerEnum(InnerEnum innerEnum) {
		this.innerEnum = innerEnum;
	}
	
	public InnerEnum getInnerEnum() {
		return innerEnum;
	}
	
}
