package de.schlund.pfixxml.testenv;

import gnu.getopt.Getopt;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ConsoleTestClient {
    private TestClient tc;
    private static int  LOOP_COUNT = 1;
    private static Category CAT = Category.getInstance(TestClient.class.getName());
    public static void main(String[] args) {
        CAT.setPriority(Priority.DEBUG);
        ConsoleTestClient ctc = null;
        
        try {
            ctc = new ConsoleTestClient();
            ctc.tc = new TestClient();
            CAT.info("|====================================================|");
            CAT.info("|               Pustefix Test TextClient             |");
            CAT.info("|====================================================|");
            if (! ctc.scanOptions(args)) {
                ctc.printUsage();
                return;
            }
            CAT.info("\n Starting test NOW!\n");
            for (int i = 0; i < LOOP_COUNT; i++) {
                TestcasePlaybackResult result = ctc.tc.makeTest();
                ctc.printResult(result);
            }
        } catch (TestClientException e) {
            CAT.error("\n**********************************************");
            CAT.error("ERROR in TestClient");
            CAT.error("Exception:");
            CAT.error(e.getMessage());
            e.printStackTrace();
            CAT.error("Nested Exception:");
            CAT.error(e.getExceptionCause().getMessage());
            e.getExceptionCause().printStackTrace();
            CAT.error("\n**********************************************");
        }
    }

    private void printUsage() {
        CAT.warn("TestClient -d [recorded dir] -t [temporary dir] -s [stylesheet dir] -q -v");
    }
    
     private boolean scanOptions(String[] args) throws TestClientException {
        String src_dir = null;
        String tmp_dir = null;
        String style_dir = null;
        Getopt getopt = new Getopt("TestClient", args, "d:t:s:qv");
        int    c = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'd':
                    src_dir = getopt.getOptarg();
                    break;
                case 't':
                    tmp_dir = getopt.getOptarg();
                    break;
                case 's':
                    style_dir = getopt.getOptarg();
                    break;
                case 'q':
                    CAT.setPriority(Priority.WARN);
                    break;
                case 'v':
                    CAT.setPriority(Priority.DEBUG);
                    break;
                default:
            }
        }
        if (src_dir == null || src_dir.equals("") || tmp_dir == null || tmp_dir.equals("")
            || style_dir == null || style_dir.equals("")) {
            return false;
        } else {
            tc.setOptions(src_dir, tmp_dir, style_dir);
            return true;
        }
    }
  
    private void printResult(TestcasePlaybackResult result) {
    }
    
}
