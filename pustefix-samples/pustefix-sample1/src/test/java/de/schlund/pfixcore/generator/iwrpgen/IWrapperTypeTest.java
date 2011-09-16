package de.schlund.pfixcore.generator.iwrpgen;

import java.util.Arrays;

import junit.framework.TestCase;
import de.schlund.pfixcore.example.test.TypeTestBeanWrapper;

public class IWrapperTypeTest extends TestCase {

    public void test() throws Exception {
        TypeTestBeanWrapper wrapper = new TypeTestBeanWrapper();
        wrapper.init("test");
        wrapper.setStringValEnabled("true");
        wrapper.setStringValNumberArray(new String[] { "1", "2", "3" });
        wrapper.setStringValNumberObjArray(new String[] { "1", "2", "3" });
        wrapper.setStringValNumberList(new String[] { "1", "2", "3" });
        wrapper.setStringValNumberArrayList(new String[] { "1", "2", "3" });
        wrapper.setStringValStrArray(new String[] {"a", "b", "c"});
        wrapper.setStringValStrList(new String[] {"a", "b", "c"});
        wrapper.setStringValStrArrayList(new String[] {"a", "b", "c"});
        wrapper.loadFromStringValues();
        Integer[] numbers = new Integer[] {1, 2, 3};
        String[] strs = new String[] {"a", "b", "c"};
        assertTrue(wrapper.getEnabled());
        assertTrue(Arrays.equals(numbers, wrapper.getNumberArray()));
        assertTrue(Arrays.equals(numbers, wrapper.getNumberObjArray()));
        assertTrue(Arrays.equals(numbers, wrapper.getNumberList()));
        assertTrue(Arrays.equals(numbers, wrapper.getNumberArrayList()));
        assertTrue(Arrays.equals(strs, wrapper.getStrArray()));
        assertTrue(Arrays.equals(strs, wrapper.getStrList()));
        assertTrue(Arrays.equals(strs, wrapper.getStrArrayList()));
    }
    
}
