package de.schlund.pfixxml.testenv;

import gnu.getopt.Getopt;



import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.DOMConfigurator;


/**
 * Console application which plays-back recorded testcases and
 * prints the result on a console. Uses functionality from class {@link TestClient}.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class ConsoleTestClient {

    //~ Instance/static variables ..................................................................

    private String          log4j;
    private String          src_dir          = null;
    private String          tmp_dir          = null;
    private String          style_dir        = null;
    private static int      LOOP_COUNT       = 1;
    private static int      LOGLEVEL_QUIET   = 0;
    private static int      LOGLEVEL_VERBOSE = 1;
    private static int      LOGLEVEL_STD     = 2;
    private static String   DEFAULT_TEMP_DIR = System.getProperties().getProperty("java.io.tmpdir");
    private int             loglevel         = LOGLEVEL_STD;

    //~ Methods ....................................................................................

    public static void main(String[] args) {
        ConsoleTestClient ctc = null;
        ctc = new ConsoleTestClient();
        ctc.doIt(args);
    }

    public void doIt(String[] args)  {
        try {
            Testcase tc = new Testcase();
            if (! scanOptions(args)) {
                printUsage();
                //return;
            }
            if (log4j != null) {
                DOMConfigurator.configure(log4j);
            } // hack 
            else {
                Category        CAT     = Category.getInstance(Testcase.class.getName());
                PatternLayout   layout  = new PatternLayout("%m\n");
                ConsoleAppender console = new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT);
                console.setName("CONSOLE");
                CAT.removeAllAppenders();
                CAT.setAdditivity(false);
                CAT.addAppender(console);
                if (loglevel == LOGLEVEL_STD) {
                    CAT.setLevel(Level.WARN);
                } else if (loglevel == LOGLEVEL_QUIET) {
                    CAT.setLevel(Level.ERROR);
                } else if (loglevel == LOGLEVEL_VERBOSE) {
                    CAT.setLevel(Level.INFO);
                }
            }
            if (tmp_dir == null) {
                tmp_dir = DEFAULT_TEMP_DIR;
            }
            if (style_dir == null) {
                style_dir = src_dir;
            }
            System.out.println("Setting options to TestClient: src_dir="+src_dir+" tmp_dir="+tmp_dir+" styledir="+style_dir);
            tc.setOptions(src_dir, tmp_dir, style_dir, "currenttescase");
            if (loglevel != LOGLEVEL_QUIET) {
                System.out.println("|====================================================|");
                System.out.println("|               Pustefix Test ConsoleClient          |");
                System.out.println("|====================================================|");
            }
            for (int i = 0; i < LOOP_COUNT; i++) {
                TestcasePlaybackResult result = tc.execute();
                printResult(result);
            }
        } catch (Exception e) {
            System.out.println("\n**********************************************");
            System.out.println("ERROR in TestClient");
            System.out.println("Exception:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Nested Exception:");
            System.out.println(e.getCause().getMessage());
            e.getCause().printStackTrace();
            System.out.println("\n**********************************************");
        }
    }

    private void printUsage() {
        System.out.println(
                "ConsoleTestClient -d path_to_testcase [-l log4jconfig]  [-t temporary dir] [-s stylesheet dir] [-q] [-v]");
    }

    private boolean scanOptions(String[] args) throws TestClientException {
        Getopt getopt = new Getopt("TestClient", args, "l:d:t:s:qv");
        int    c = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'l':
                    log4j = getopt.getOptarg();
                    break;
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
        if (loglevel != LOGLEVEL_QUIET) {
            System.out.println("*** Result for testcase: ***");
        }
        
        for (int i = 0; i < result.getNumStepResult(); i++) {
            TestcaseStepResult stepresult = result.getStepResult(i);
            
            if ((stepresult.getDiffString() == null || stepresult.getDiffString().equals(""))
                && loglevel == LOGLEVEL_QUIET) {
                // print nothing in quiet mode
            } else {
                System.out.println("Step number " + i);
                System.out.println("  Statuscode : " + stepresult.getStatuscode());
                System.out.println("  TotalTime        : " + stepresult.getDuration());
                System.out.println("    PreProcessing  : " + stepresult.getPreProcessingDuration());
                System.out.println("    GetDocument    : " + stepresult.getGetDocumentDuration());
                System.out.println("    HandleDocument : " + stepresult.getHandleDocumentDuration());
                String diff = stepresult.getDiffString();
                if (diff == null) {
                    diff = "NONE";
                } else if (diff.equals("")) {
                    diff = ":-)";
                }
                System.out.println("  Diff       : " + diff);
                System.out.println("");
            }
        }
        System.out.println("-->Total time for testcase         : "+result.getTotalDuration()+"ms.");
        System.out.println("---->Total time for PreProcessing  : "+result.getTotalPreProcessingDuration()+"ms.");
        System.out.println("---->Total time for GetDocument    : "+result.getTotalGetDomDuration()+"ms.");
        System.out.println("---->Total time for HandleDocument : "+result.getTotalHandleDocumentDuartion()+"ms.");
    }
}