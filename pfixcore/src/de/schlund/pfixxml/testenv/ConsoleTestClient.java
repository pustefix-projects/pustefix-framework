package de.schlund.pfixxml.testenv;

import java.util.ArrayList;

import gnu.getopt.Getopt;

import org.apache.log4j.Category;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Console application which plays-back recorded testcases and
 * prints the result on a console. Uses functionality from class {@link TestClient}.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class ConsoleTestClient {
    private TestClient tc;
    private String log4j;
    private String src_dir = null;
    private String tmp_dir = null;
    private String style_dir = null;
    private static int  LOOP_COUNT = 1;
    private static Category CAT = Category.getInstance(ConsoleTestClient.class.getName());
    
    public static void main(String[] args) {
        ConsoleTestClient ctc = null;
        
        try {
            ctc = new ConsoleTestClient();
            ctc.tc = new TestClient();
            if (! ctc.scanOptions(args)) {
                ctc.printUsage();
                return;
            }

            DOMConfigurator.configure(ctc.log4j);
            ctc.tc.setOptions(ctc.src_dir, ctc.tmp_dir, ctc.style_dir);
            CAT.info("|====================================================|");
            CAT.info("|               Pustefix Test ConsoleClient          |");
            CAT.info("|====================================================|");
            
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
        System.out.println("ConsoleTestClient -l [log4jconfig] -d [recorded dir] -t [temporary dir] -s [stylesheet dir]");
    }
    
     private boolean scanOptions(String[] args) throws TestClientException {
       
        Getopt getopt = new Getopt("TestClient", args, "l:d:t:s:qv");
        int    c = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'l':
                    log4j = getopt.getOptarg();
                case 'd':
                    src_dir = getopt.getOptarg();
                    break;
                case 't':
                    tmp_dir = getopt.getOptarg();
                    break;
                case 's':
                    style_dir = getopt.getOptarg();
                    break;
                /*case 'q':
                    CAT.setPriority(Priority.WARN);
                    break;
                case 'v':
                    CAT.setPriority(Priority.DEBUG);
                    break;*/
                default:
            }
        }
        if (src_dir == null || src_dir.equals("") || tmp_dir == null || tmp_dir.equals("")
            || style_dir == null || style_dir.equals("") || log4j == null || log4j.equals("")) {
            return false;
        } else {
            return true;
        }
    }
  
    private void printResult(TestcasePlaybackResult result) {
        CAT.info("*** Result for testcase: ***");
        ArrayList steps = result.getStepResults();
        for(int i=0; i<steps.size(); i++) {
            TestcaseStepResult step = (TestcaseStepResult) steps.get(i);
            CAT.info("Step number "+i);
            CAT.info("  Statuscode : "+step.getStatusCode());
            String diff = step.getDiffString();
            if(diff == null) {
                diff = "NONE";
            } else if(diff.equals("")) {
                diff = ":-)";
            }
            
            CAT.info("  Diff       : "+diff);
            CAT.info("");
        } 
    }
    
}
