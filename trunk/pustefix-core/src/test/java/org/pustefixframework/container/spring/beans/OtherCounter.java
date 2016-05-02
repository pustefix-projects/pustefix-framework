package org.pustefixframework.container.spring.beans;

public class OtherCounter implements Counter {

    private int count;

    public int count() {
        return ++count;
    }

}
