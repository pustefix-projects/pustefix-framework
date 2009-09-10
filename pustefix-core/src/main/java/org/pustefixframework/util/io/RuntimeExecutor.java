/**
 * 
 */
package org.pustefixframework.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Simplifies the execution of system commands by managing
 * the process and its IO. 
 * 
 * 
 * @author mleidig@schlund.de
 *
 */
public class RuntimeExecutor {
	
	/**
	 * Execute a command in a separate process.
	 * 
	 * @param commandline - command to be executed
	 * @param out - where to write standard and error output
	 * @return the exit code of the process
	 */
	public static int exec(String commandline, PrintWriter out) {
		return exec(commandline, out, out);
	}
	
	/**
	 * Execute a command in a separate process.
	 * 
	 * @param commandline - command to be executed
	 * @param stdout - where to write the standard output
	 * @param stderr - where to write the standard error output
	 * @return the exit code of the process 
	 */
	public static int exec(String commandline, PrintWriter stdout, PrintWriter stderr) {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec(commandline);
			Thread inputThread = new ExecReaderThread(process.getInputStream(), stdout);
			inputThread.start();
			Thread outputThread = new ExecReaderThread(process.getErrorStream(), stderr);
			outputThread.start();
			int exitCode = process.waitFor();
			inputThread.join();
			outputThread.join();
			return exitCode;
		} catch(IOException x) {
			throw new RuntimeException("Error executing: " + commandline, x);
		} catch(InterruptedException x) {
			throw new RuntimeException("Error executing: " + commandline, x);
		}
	}
	
	
	private static class ExecReaderThread extends Thread {
		
		private InputStream input;
		private PrintWriter output;
		
		private ExecReaderThread(InputStream input, PrintWriter output) {
			this.input = input;
			this.output = output;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				String line = null;
				while((line = reader.readLine())!=null) {
					synchronized(output) {
						output.write(line);
						output.write("\n");
					}
				}
				reader.close();
			} catch(IOException x) {
				throw new RuntimeException("Error reading process stream", x);
			}
		}
		
	}
	
	
}