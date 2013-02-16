package com.cantina.lab

import java.util.List
import org.apache.log4j.Logger

/**
 * Utility class to contain static methods for executing system commands.
 * 
 * @author Peter N. Steinmetz
 */
class SysCmdUtils {
	private static Logger log = Logger.getLogger(SysCmdUtils.getClass())
	
	/**
	 * Execute a system command from an array of command and arguments
	 *
	 * @param array of command and arguments
	 * @return true if successful, false otherwise
	 */
	public static boolean exec(List cmdArr) {
		def out = new StringBuilder()
		def err = new StringBuilder()
		return exec(cmdArr,out,err)
	}
	
	/**
	 * Execute a system command from an array of command and arguments,
	 * placing regular and error output into provided StringBuilders.
	 *
	 * @param array of command and arguments
	 * @param out to receive normal output
	 * @param err to receive error output
	 * @return true if successful, false otherwise
	 */
	public static boolean exec(List cmdArr, StringBuilder out, StringBuilder err) {
		try {
			log.debug "Executing $cmdArr"
			def proc = Runtime.getRuntime().exec((String[])cmdArr)

			proc.waitForProcessOutput(out, err)
			if (out) log.debug "out:\n$out"
			if (err) log.debug "err:\n$err"
      
      def exitStatus = proc.exitValue()

			log.debug "Process exited with status $exitStatus"

			return exitStatus == 0
		}
		catch (Exception e) {
			log.error("Error while executing command $cmdArr", e)
			return false
		}
	}
	
}
