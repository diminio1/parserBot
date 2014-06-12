package banan.file.writer;

import org.pmw.tinylog.writers.FileWriter;

public class BananFileWriter extends FileWriter {

	public BananFileWriter(String input){
		
		super(input);
	}
	
	
	public String bananStackTraceToString(Throwable e) {
	    StringBuilder sb = new StringBuilder();
	    for (StackTraceElement element : e.getStackTrace()) {
	        sb.append(element.toString());
	        sb.append("\n");
	    }
	    return sb.toString();
	}
}
