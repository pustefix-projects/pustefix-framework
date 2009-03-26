package org.pustefixframework.tutorial.calculator;

/**
 * CalculatorService
 * 
 * Interfaces that provides the methods that
 * will be exported as a web service.
 *
 * @author  Stephan Schmidt <stephan.schmidt@1und1.de>
 */
public interface CalculatorService {
    public int add(int a, int b);
    public int subtract(int a, int b);
    public int multiply(int a, int b);
}