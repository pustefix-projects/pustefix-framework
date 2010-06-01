package org.pustefixframework.samples.modular.service.internal;

import java.util.Random;

import org.pustefixframework.samples.modular.service.RandomNumberGenerator;

public class MathRandomNumberGenerator implements RandomNumberGenerator {

    private Random random = new Random();
    
    public int generate(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
    
}
