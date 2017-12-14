package parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileParser {
	
	private FileInputStream fstream;
	private DataInputStream in;
	private BufferedReader br;
	
	public void readFile(File file) {
		try {
			fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			
		} catch (Exception e) {
			System.err.println("Error " + e.getMessage());
		}
	}
	
	public void closeInStream () throws IOException {
		br.close();
		in.close();
		fstream.close();
	}
	
	public String getNextLine () throws IOException {
		return br.readLine();
	}
}
