package de.schlund.pfixcore.oxm;

import de.schlund.pfixcore.oxm.impl.annotation.XMLFragmentSerializer;

/**
 * Simple test bean to test the serialization
 * of XML fragments
 * 
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class FragmentBean {

    private String myFragment = "<foo><bar baz=\"true\"/>character data</foo>";

    @XMLFragmentSerializer
    public String anotherFragment = "This is a <real>fragment</real>.";

    @XMLFragmentSerializer
    public String thirdFragment = "<foo/><bar>baz</bar>";

    @XMLFragmentSerializer
    public String getMyFragment() {
        return myFragment;
    }
}