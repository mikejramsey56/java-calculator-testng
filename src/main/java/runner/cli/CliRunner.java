package runner.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.text.html.HTML.Tag;

/**
 * Customized cucumber test runner utilizing the Command Line Interface (CLI) driver.
 * Purpose is to explore non-JUnit test runners.
 * 
 * Recognizes three command line system properties that can be specified using -D 
 * option. An example would be -Dproject.root=/home/foo/myprojects -Dm2e.root=/home/foo
 *   project.root     - A string used to fully qualify paths. The fully qualified 
 *                      parent of folder java-testng-calculator.
 *                  
 *   m2e.root         - Fully qualified path to .m2e, usually your home directory.
 *   
 *   cucumber.options - Same as the junit runner cucumber.otions.
 * 
 * @author Mike Ramsey
 *
 */

public class CliRunner {

	public static void main(String[] args) throws IOException, InterruptedException {
		class StreamGobbler extends Thread {
		    InputStream is;

		    // reads everything from is until empty. 
		    StreamGobbler(InputStream is) {
		        this.is = is;
		    }

		    @Override
		    public void run() {
		        try {
		            InputStreamReader isr = new InputStreamReader(is);
		            BufferedReader br = new BufferedReader(isr);
		            String line=null;
		            while ( (line = br.readLine()) != null)
		                System.out.println(line);    
		        } catch (IOException ioe) {
		            ioe.printStackTrace();  
		        }
		    }
		}
		
		String prjRoot = checkVerifyProperty("project.root");
		String m2eRoot = checkVerifyProperty("m2e.root");
		String cucumberOptions = checkVerifyProperty("cucumber.options");
		
		
		String classPath = prjRoot + "/java-calculator-testng/target/test-classes:" + prjRoot + "/java-calculator-testng/target/classes:" + m2eRoot + "/.m2/repository/info/cukes/cucumber-jvm-deps/1.0.5/cucumber-jvm-deps-1.0.5.jar:" + m2eRoot + "/.m2/repository/info/cukes/cucumber-java/1.2.4/cucumber-java-1.2.4.jar:" + m2eRoot + "/.m2/repository/info/cukes/cucumber-core/1.2.4/cucumber-core-1.2.4.jar:" + m2eRoot + "/.m2/repository/info/cukes/cucumber-html/0.2.3/cucumber-html-0.2.3.jar:" + m2eRoot + "/.m2/repository/info/cukes/gherkin/2.12.2/gherkin-2.12.2.jar:" + m2eRoot + "/.m2/repository/info/cukes/cucumber-testng/1.2.4/cucumber-testng-1.2.4.jar:" + m2eRoot + "/.m2/repository/org/testng/testng/6.9.4/testng-6.9.4.jar:" + m2eRoot + "/.m2/repository/org/beanshell/bsh/2.0b4/bsh-2.0b4.jar:" + m2eRoot + "/.m2/repository/com/beust/jcommander/1.48/jcommander-1.48.jar:" + m2eRoot + "/.m2/repository/org/apache/maven/surefire/surefire-testng-utils/2.17/surefire-testng-utils-2.17.jar:" + m2eRoot + "/.m2/repository/org/apache/maven/surefire/surefire-grouper/2.17/surefire-grouper-2.17.jar:" + prjRoot + "/java-calculator-testng/src/main/cucumber/examples/java/calculator/*:" + prjRoot + "/java-calculator-testng/src/main/cucumber/examples/java/calculator/*:" + prjRoot + "/java-calculator-testng/src/test/resources/cucumber/examples/java/calculator/*:";
        System.out.println("** classPath=" + classPath);		
		String featuresPath = prjRoot + "/java-calculator-testng/src/test/resources/cucumber/examples/java/calculator/features";
		
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("java -cp " + classPath + " cucumber.api.cli.Main " + cucumberOptions + " " + featuresPath);
		//output both stdout and stderr data from proc to stdout of this process
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
		errorGobbler.start();
		outputGobbler.start();
		proc.waitFor();
		int exitVal = proc.exitValue();
		System.out.println("Process exitValue: " + exitVal);
		if (proc != null)
            proc.destroy();
		
	}
	
	public static String checkVerifyProperty(String parameterName){
		String pathRoot = System.getProperty(parameterName);
		if (pathRoot == null) {
			pathRoot = getRootDefault(parameterName);
			System.out.println("no " + parameterName + " defined, using '" + pathRoot + "'");
		} else {
			pathRoot = replaceSuffix (pathRoot, "/", "");
			System.out.println("Supplied " + parameterName + " is '" + pathRoot + "'");
		}
		return pathRoot;
	}
	
	public static String getRootDefault(String parameterName){
		if (parameterName.equals("project.root")) {
			return "/home/mike/gitprojs";
		} else if (parameterName.equals("m2e.root")) {
			return "/home/mike";
		} else if (parameterName.equals("cucumber.options")) {
			return "--plugin json:target/cucumber-report.json --glue cucumber/examples/java/calculator";
		} else {
			throw new IllegalArgumentException("Unrecognized parameterType: ' + parameterName");
		}
			
	}
	
	public static String replaceSuffix (String target, String suffix, String replacement) {
	    if (!target.endsWith(suffix)) {
	        return target;
	    }

	    String prefix = target.substring(0, target.length() - suffix.length());
	    return prefix + replacement;
	}
}


