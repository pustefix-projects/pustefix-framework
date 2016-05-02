package org.pustefixframework.container.spring.beans;

public class CounterImpl {

    private int count;

    public int count() {
        return ++count;
    }

}
