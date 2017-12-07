package parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileParser {
	
	private BufferedReader br;
	private DataInputStream in;
	
	public void readFile(String file) {
		try {
			FileInputStream fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
		} catch (Exception e) {
			System.err.println("Error " + e.getMessage());
		}
	}
	
	public void closeInStr () throws IOException {
		in.close();
	}
	
	public String getNextLine () throws IOException {
		return br.readLine();
	}
}
