package dpm.tutorial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

public class Log {

	static PrintStream writer = System.out;

	
	static HashMap<Class<?>,String> printers = new HashMap<>();
	
	
	public static void log(Class<?> c, String message) {
		long timestamp = System.currentTimeMillis() % 100000;
		String s = printers.get(c);
		if(s != null){
			writer.println(s + "::" + timestamp + ": " + message);
		}
	}


	public static void addClass(Class<?> c,String name){
		printers.put(c,name);
	}

	public static void setLogWriter(String filename) throws FileNotFoundException {
		writer = new PrintStream(new File(filename));
	}

}
