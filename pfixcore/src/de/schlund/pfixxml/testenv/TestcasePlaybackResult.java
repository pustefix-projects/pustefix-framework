/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.testenv;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class encapsulating the result of a whole testcase after playback.
 * Aggregates serveral {@link TestcaseStepResult} objects.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcasePlaybackResult {
    private ArrayList stepResults = null;
    private TestClientException ex = null;
    /**
     * Constructor for TestcasePlaybackResult.
     */
    public TestcasePlaybackResult() {
        stepResults = new ArrayList();
    }

    /**
     * Returns the results for all steps in the testcase.
     * @return ArrayList containg TestcaseStepResult objects.
     */
    public TestcaseStepResult getStepResult(int index) {
        return (TestcaseStepResult) stepResults.get(index);
    }
    
    public int getNumStepResult() {
        return stepResults.size();
    }
    
    public long getTotalDuration() {
        long ret = 0;
        for(Iterator i = stepResults.iterator(); i.hasNext();) {
            ret += ((TestcaseStepResult) i.next()).getDuration();
        }  
        return ret;
    }
    
    public long getTotalPreProcessingDuration() {
        long ret = 0;
        for(Iterator i = stepResults.iterator(); i.hasNext();) {
            ret +=  ((TestcaseStepResult)i.next()).getPreProcessingDuration();
        }
        return ret;
    }
    
   public long getTotalGetDomDuration() {
        long ret = 0;
        for(Iterator i = stepResults.iterator(); i.hasNext();) {
            ret += ((TestcaseStepResult)i.next()).getGetDocumentDuration();
        }
        return ret;
   }
   
   public long getTotalHandleDocumentDuartion() {
        long ret = 0;
        for(Iterator i = stepResults.iterator(); i.hasNext();) {
            ret += ((TestcaseStepResult)i.next()).getHandleDocumentDuration();
        }
        return ret;
   }
   

    /**
     * Add a TestcaseStepResult object to the Testcase result.
     * @param the TestcaseStepResult to be added
     */
    public void addTestcaseStepResult(TestcaseStepResult step) {
        if(step == null) {
            throw new IllegalArgumentException("A NP as step is not allowed here!");
        }
        stepResults.add(step);
    }
    
    public void setException(TestClientException e) {
        this.ex = e;
    }
    
    public TestClientException getException() {
        return ex;
    }

    public boolean hasException() {
        return ex == null ? false : true;
    }

}
