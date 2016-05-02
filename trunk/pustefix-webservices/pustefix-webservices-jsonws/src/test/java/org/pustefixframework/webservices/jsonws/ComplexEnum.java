package org.pustefixframework.webservices.jsonws;

public enum ComplexEnum {

	RED   (0xFF0000),
	GREEN (0x00FF00),
	BLUE  (0x0000FF);
	
	private final int rgbValue;
	
	ComplexEnum(int rgbValue) {
		this.rgbValue = rgbValue;
	}
	
	public int getRgbValue() {
		return rgbValue;
	}
	
}
