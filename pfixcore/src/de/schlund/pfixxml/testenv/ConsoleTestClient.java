package de.schlund.pfixxml.testenv;

import java.util.ArrayList;

import gnu.getopt.Getopt;

import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
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
    private static int LOGLEVEL_QUIET = 0;
    private static int LOGLEVEL_VERBOSE = 1;
    private static int LOGLEVEL_STD = 2;
    private static String DEFAULT_TEMP_DIR = System.getProperties().getProperty("java.io.tmpdir");
    private int loglevel = LOGLEVEL_STD;
        
    public static void main(String[] args) {
        ConsoleTestClient ctc = null;
        
        try {
            ctc = new ConsoleTestClient();
            ctc.tc = new TestClient();
            if (! ctc.scanOptions(args)) {
                ctc.printUsage();
                return;
            }

            if(ctc.log4j != null) {
                DOMConfigurator.configure(ctc.log4j);
            } else { // hack
                Category CAT = Category.getInstance(TestClient.class.getName());
                PatternLayout layout = new PatternLayout("%m\n");
                ConsoleAppender console = new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT);
                console.setName("CONSOLE");
                CAT.removeAllAppenders();
                CAT.setAdditivity(false);
                CAT.addAppender(console);
                if(ctc.loglevel == LOGLEVEL_STD) {
                    CAT.setPriority(Priority.WARN);
                } else if(ctc.loglevel == LOGLEVEL_QUIET) {
                    CAT.setPriority(Priority.ERROR);
                } else if(ctc.loglevel == LOGLEVEL_VERBOSE) {
                    CAT.setPriority(Priority.INFO);
                }
            }
            if(ctc.tmp_dir == null) {
                ctc.tmp_dir = DEFAULT_TEMP_DIR;
            }
            if(ctc.style_dir == null) {
                ctc.style_dir = ctc.src_dir;
            }
            ctc.tc.setOptions(ctc.src_dir, ctc.tmp_dir, ctc.style_dir);
            if(ctc.loglevel != LOGLEVEL_QUIET) {
                System.out.println("|====================================================|");
                System.out.println("|               Pustefix Test ConsoleClient          |");
                System.out.println("|====================================================|");
            }
            
            for (int i = 0; i < LOOP_COUNT; i++) {
                TestcasePlaybackResult result = ctc.tc.makeTest();
                ctc.printResult(result);
            }
        } catch (TestClientException e) {
            System.out.println("\n**********************************************");
            System.out.println("ERROR in TestClient");
            System.out.println("Exception:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Nested Exception:");
            System.out.println(e.getExceptionCause().getMessage());
            e.getExceptionCause().printStackTrace();
            System.out.println("\n**********************************************");
        }
    }

    private void printUsage() {
        System.out.println("ConsoleTestClient -d path_to_testcase [-l log4jconfig]  [-t temporary dir] [-s stylesheet dir] [-q] [-v]");
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
                case 'q':
                    loglevel = LOGLEVEL_QUIET;
                    break;
                case 'v':
                    loglevel = LOGLEVEL_VERBOSE;
                    break;
                default:
            }
        }
        if (src_dir == null || src_dir.equals("")) {
            return false;
        } else {
            return true;
        }
    }
  
    private void printResult(TestcasePlaybackResult result) {
        if(loglevel != LOGLEVEL_QUIET) {
            System.out.println("*** Result for testcase: ***");
        }
        ArrayList steps = result.getStepResults();
        for(int i=0; i<steps.size(); i++) {
            TestcaseStepResult step = (TestcaseStepResult) steps.get(i);
            if((step.getDiffString() == null || step.getDiffString().equals("")) && loglevel == LOGLEVEL_QUIET) { 
                // print nothing in quiet mode
            } else {   
                System.out.println("Step number "+i);
                System.out.println("  Statuscode : "+step.getStatusCode());
                String diff = step.getDiffString();
                if(diff == null) {
                    diff = "NONE";
                } else if(diff.equals("")) {
                    diff = ":-)";
                }
                System.out.println("  Diff       : "+diff);
                System.out.println("");
            }
        } 
    }
    
}
