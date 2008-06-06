package org.pustefixframework.tutorial.calculator;

/**
 * Calculator
 * 
 * Provides the business logic
 *
 * @author  Stephan Schmidt <stephan.schmidt@1und1.de>
 */
public class Calculator implements CalculatorService {
    public int add(int a, int b) {
        return a+b;
    }

    public int divide(int a, int b) {
        return a-b;
    }

    public int multiply(int a, int b) {
        return a*b;
    }

    public int subtract(int a, int b) {
        return a/b;
    }
}