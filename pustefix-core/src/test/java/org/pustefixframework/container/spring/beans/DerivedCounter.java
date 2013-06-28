package org.pustefixframework.container.spring.beans;

public class DerivedCounter extends CounterImpl implements Counter {

	private int count;

	public int count() {
		return ++count;
	}

}
