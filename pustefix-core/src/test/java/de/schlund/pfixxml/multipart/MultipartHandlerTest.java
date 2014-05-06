package de.schlund.pfixxml.multipart;

import junit.framework.TestCase;

public class MultipartHandlerTest extends TestCase {
    
    public void testRemoveFilePath() {
        
        String expected = "form-data; name=\"file\"; filename=\"test.txt\"";
        
        String value = "form-data; name=\"file\"; filename=\"C:\\tmp\\test.txt\"";
        String cleared = value.replaceAll(MultipartHandler.REMOVE_FILEPATH_PATTERN, "$1$3");
        assertEquals(expected, cleared);
        
        value = "form-data; name=\"file\"; filename=\"/etc/test.txt\"";
        cleared = value.replaceAll(MultipartHandler.REMOVE_FILEPATH_PATTERN, "$1$3");
        assertEquals(expected, cleared);
        
        value = "form-data; name=\"file\"; filename=\"test.txt\"";
        cleared = value.replaceAll(MultipartHandler.REMOVE_FILEPATH_PATTERN, "$1$3");
        assertEquals(expected, cleared);
    }

}
