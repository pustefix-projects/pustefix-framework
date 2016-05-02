package org.pustefixframework.web.mvc.filter;

import java.util.Comparator;

import junit.framework.TestCase;

public class PropertyTest extends TestCase {
    
    public void testStringComparison() {
        
        Bean bean = new Bean();
        bean.setText("foo");
        
        Property prop = new Property("text", "foo", Property.Compare.EQUAL);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("text", "bar", Property.Compare.EQUAL);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("text", "zzz", Property.Compare.LESS);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("text", "aaa", Property.Compare.LESS);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("text", "aaa", Property.Compare.GREATER);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("text", "zzz", Property.Compare.GREATER);
        assertFalse(prop.isSatisfiedBy(bean));
        
    }
    
    public void testNumberComparison() {
        
        Bean bean = new Bean();
        bean.setNumber(2l);
        
        Property prop = new Property("number", 2l, Property.Compare.EQUAL);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("number", 0l, Property.Compare.EQUAL);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("number", 3l, Property.Compare.LESS);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("number", 1l, Property.Compare.LESS);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("number", 1l, Property.Compare.GREATER);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("number", 3l, Property.Compare.GREATER);
        assertFalse(prop.isSatisfiedBy(bean));
        
    }
    
    public void testPrimitiveNumberComparison() {
        
        Bean bean = new Bean();
        bean.setNo(2);
        
        Property prop = new Property("no", 2, Property.Compare.EQUAL);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("no", 0, Property.Compare.EQUAL);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("no", 3, Property.Compare.LESS);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("no", 1, Property.Compare.LESS);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("no", 1, Property.Compare.GREATER);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("no", 3, Property.Compare.GREATER);
        assertFalse(prop.isSatisfiedBy(bean));
        
    }

    public void testNonComparable() {
        
        Bean bean = new Bean();
        bean.setValue(new Bean.Value(2));
        
        Property prop = new Property("value", new Bean.Value(2), Property.Compare.EQUAL);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("value", new Bean.Value(0), Property.Compare.EQUAL);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("value", new Bean.Value(3), Property.Compare.LESS);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("value", new Bean.Value(1), Property.Compare.LESS);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("value", new Bean.Value(1), Property.Compare.GREATER);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("value", new Bean.Value(3), Property.Compare.GREATER);
        assertFalse(prop.isSatisfiedBy(bean));
        
    }
    
    public void testNullValues() {
        
        Bean bean = new Bean();
        bean.setText("foo");
        
        Property prop = new Property("text", null, Property.Compare.EQUAL);
        assertFalse(prop.isSatisfiedBy(bean));
        bean.setText(null);
        prop = new Property("text", "bar", Property.Compare.EQUAL);
        assertFalse(prop.isSatisfiedBy(bean));
        prop = new Property("text", null, Property.Compare.EQUAL);
        assertTrue(prop.isSatisfiedBy(bean));
        
    }
    
    public void testComparator() {
        
        Comparator<Bean.Value> comparator = new Comparator<Bean.Value>() {
          
            @Override
            public int compare(Bean.Value o1, Bean.Value o2) {
                if(o1 != null && o2 != null) {
                    return o1.value - o2.value;
                } else if(o1 == null && o2 == null) {
                    return 0;
                }
                return -1;
            }
        };
        
        Bean bean = new Bean();
        bean.setValue(new Bean.Value(2));
        
        Property prop = new Property("value", new Bean.Value(2), Property.Compare.EQUAL, comparator);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("value", new Bean.Value(0), Property.Compare.EQUAL, comparator);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("value", new Bean.Value(3), Property.Compare.LESS, comparator);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("value", new Bean.Value(1), Property.Compare.LESS, comparator);
        assertFalse(prop.isSatisfiedBy(bean));
        
        prop = new Property("value", new Bean.Value(1), Property.Compare.GREATER, comparator);
        assertTrue(prop.isSatisfiedBy(bean));
        prop = new Property("value", new Bean.Value(3), Property.Compare.GREATER, comparator);
        assertFalse(prop.isSatisfiedBy(bean));
        
    }
    
}
