package org.pustefixframework.samples.modular.service.internal;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class MathRandomNumberGeneratorTest {

    private MathRandomNumberGenerator generator;
    
    @Before
    public void prepare() {
        generator = new MathRandomNumberGenerator();
    }
    
    @Test
    public void checkRange() {
        checkRange(5,10);
        checkRange(1,1);
    }
    
    private void checkRange(int min, int max) {
        for(int i=0; i<1000; i++) {
            int no = generator.generate(min, max);
            assertTrue(no >= min);
            assertTrue(no <= max);
        }
    }
    
    @Test
    public void checkVariance() {
        int tries = 100000;
        int maxVariance = 1000;
        int[] nos = new int[5];
        for(int i=0; i<tries; i++) {
            int no = generator.generate(0, 4);
            nos[no] = ++nos[no];
        }
        for(int i=0; i<nos.length; i++) {
            int variance = Math.abs((tries / 5) - nos[i]);
            assertTrue(variance <= maxVariance);
        }
    }
    
}
