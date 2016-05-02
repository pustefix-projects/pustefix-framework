package org.pustefixframework.util.javascript.internal;

import junit.framework.TestCase;

public abstract class AbstractAdapterTest extends TestCase {

    String[] inputs = new String[] {
            "//comment\n      alert(\"hey\");  //afdfadfs\n",
            "function foo() {var xxxxxx=3;};",
            "function bar(xyz) {alert(xyz);};",
            "function bar(xyz) {FOO.bar=xyz;};"
    };
    
}
