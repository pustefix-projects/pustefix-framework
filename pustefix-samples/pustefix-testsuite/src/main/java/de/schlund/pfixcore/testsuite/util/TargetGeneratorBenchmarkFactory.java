/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.testsuite.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;

/**
 * Helper class for the TargetGenerator benchmark utility.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetGeneratorBenchmarkFactory {
    
    private class BenchmarkRunner implements Runnable {
        private TargetGenerator tgen;
        private int threadid;
        private int requestsperthread;
        
        public BenchmarkRunner(int threadid, TargetGenerator tgen, int requestsperthread) {
            this.threadid = threadid;
            this.tgen = tgen;
            this.requestsperthread = requestsperthread;
            TargetGeneratorBenchmarkFactory.getInstance().messages[threadid] = new StringBuffer();
            TargetGeneratorBenchmarkFactory.getInstance().finished[threadid] = false;
        }

        public void run() {
            ArrayList<Target> targets = new ArrayList<Target>(this.tgen.getPageTargetTree().getToplevelTargets());
            Collections.shuffle(targets);
            int numtargets = targets.size();
            TargetGeneratorBenchmarkFactory instance = TargetGeneratorBenchmarkFactory.getInstance();
            
            long start = System.currentTimeMillis();
            for (int i=0; i < requestsperthread; i++) {
                try {
                    targets.get(i % numtargets).getValue();
                } catch (TargetGenerationException e) {
                    instance.messages[threadid].append(makeStackTrace(e));
                    
                }
            }
            long end = System.currentTimeMillis();
            
            instance.times[threadid] = end - start;
            instance.finished[threadid] = true;
            
            long totaltime = end - instance.startTime;
            synchronized (instance) {
                instance.maxTime = Math.max(totaltime, instance.maxTime);
            }
        }
        
        private String makeStackTrace(Throwable e) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(buf));
            return buf.toString();
        }

    }

    private static TargetGeneratorBenchmarkFactory instance = new TargetGeneratorBenchmarkFactory();
    
    protected long times[];
    protected StringBuffer messages[];
    protected boolean finished[];
    protected ThreadGroup tgenGroup;
    private long startTime;
    protected long maxTime = 0;
    
    private TargetGeneratorBenchmarkFactory() {};
    
    public void init(Properties props) throws Exception {
        int numthreads = Integer.parseInt(props.getProperty("tgenbench.numthreads"));
        int requestsperthread = Integer.parseInt(props.getProperty("tgenbench.requestsperthread"));
        String dependxml = props.getProperty("tgenbench.dependxml");
        times = new long[numthreads];
        messages = new StringBuffer[numthreads];
        finished = new boolean[numthreads];
        Thread threads[] = new Thread[numthreads];
        tgenGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "tgen-threads");
        tgenGroup.setDaemon(true);
        TargetGenerator tgen = TargetGeneratorFactory.getInstance().createGenerator(ResourceUtil.getFileResourceFromDocroot(dependxml));
        for (int i=0; i < numthreads; i++) {
            threads[i] = new Thread(tgenGroup, new BenchmarkRunner(i, tgen, requestsperthread));
        }
        startTime = System.currentTimeMillis();
        for (int i=0; i < numthreads; i++) {
            threads[i].start();
        }
    }
    
    public static TargetGeneratorBenchmarkFactory getInstance() {
        return instance;
    }
    
}
