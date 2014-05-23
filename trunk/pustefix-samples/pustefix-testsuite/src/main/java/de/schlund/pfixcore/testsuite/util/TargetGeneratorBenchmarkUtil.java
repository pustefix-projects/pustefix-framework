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

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * Command-line utitlity that test the performance of a TargetGenerator.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetGeneratorBenchmarkUtil {


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: <tool> <depend.xml> <number of threads> <requests per thread>");
            return;
        }
        
        String dependxml = args[0];
        String numberofthreads = args[1];
        String requestsperthread = args[2];
        
        //BasicConfigurator.configure(new NullAppender());
        Properties log4jprops = new Properties();
        log4jprops.setProperty("log4j.threshold", "OFF");
        PropertyConfigurator.configure(log4jprops);
        GlobalConfigurator.setDocroot(GlobalConfig.guessDocroot().getAbsolutePath());
        
        Properties props = new Properties();
        props.setProperty("tgenbench.dependxml", dependxml);
        props.setProperty("tgenbench.numthreads", numberofthreads);
        props.setProperty("tgenbench.requestsperthread", requestsperthread);
        try {
            TargetGeneratorBenchmarkFactory.getInstance().init(props);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        TargetGeneratorBenchmarkFactory instance = TargetGeneratorBenchmarkFactory.getInstance();
        
        while (true) {
            int running = 0;
            for (int i = 0; i < instance.finished.length; i++) {
                if (!instance.finished[i]) {
                    running++;
                }
            }
            if (running == 0) {
                break;
            }
            System.out.println("Still " + running + " threads running");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        long sum = 0;
        for (int i = 0; i < instance.times.length; i++) {
            sum += instance.times[i];
            System.out.println("Thread " + (i + 1) + ": " + instance.times[i] + " ms");
        }
        for (int i = 0; i < instance.messages.length; i++) {
            if (instance.messages[i].length() > 0) {
                System.out.println("Messages in Thread" + (i + 1) + ":");
                System.out.print(instance.messages[i]);
            }
            System.out.println("Thread " + (i + 1) + ": " + instance.times[i] + " ms");
        }
        System.out.println("Average time per thread: " + ((double) sum / (double) instance.times.length) + "ms");
        System.out.println("Total time: " + instance.maxTime + "ms");
    }

}
