package de.schlund.pfixcore.example;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@XmlRootElement(name="jaxbTest")
public class JAXBTest {
	
	private String text;
	private int number;
	private TestData testData;
	private List<String> values;
	
	public JAXBTest() {
		text = "foo";
		number = 7;
		values = new ArrayList<String>();
		values.add("hey");
		values.add("ho");
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@XmlAttribute
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public TestData getTestData() {
		return testData;
	}

	@Autowired
	@Qualifier("testdata")
	public void setTestData(TestData testData) {
		this.testData = testData;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
