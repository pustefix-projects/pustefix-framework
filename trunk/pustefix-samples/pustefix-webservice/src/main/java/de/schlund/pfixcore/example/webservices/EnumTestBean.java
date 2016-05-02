package de.schlund.pfixcore.example.webservices;

public class EnumTestBean {

	public enum InnerTestEnum { RED, GREEN, BLUE };
	
	private InnerTestEnum innerTestEnum;
	private TestEnum testEnum;
	
	public void setInnerTestEnum(InnerTestEnum innerTestEnum) {
		this.innerTestEnum = innerTestEnum;
	}
	
	public InnerTestEnum getInnerTestEnum() {
		return innerTestEnum;
	}
	
	public void setTestEnum(TestEnum testEnum) {
		this.testEnum = testEnum;
	}
	
	public TestEnum getTestEnum() {
		return testEnum;
	}
	
}
