package org.pustefixframework.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {

    /**
     * Read a stream into a string.
     * 
     * @param stream - the input stream
     * @param encoding - stream content's encoding
     * @return the stream's content as string
     * @throws IOException
     */
    public static String load(InputStream in, String encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, encoding);
        StringBuffer strBuf = new StringBuffer();
        char[] buffer = new char[4096];
        int i = 0;
        try {
            while ((i = reader.read(buffer)) != -1)
                strBuf.append(buffer, 0, i);
        } finally {
            in.close();
        }
        return strBuf.toString();
    }
    
}
