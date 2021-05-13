package assign1;

/**
 * Assignment 1
 * Submitted by: 
 * Student 1. Alex Chen	ID# 312286545
 * Student 2. Pinhas Ziv ID# 315709139
 */

//Uncomment if you wish to use FileOutputStream and FileInputStream for file access.
//import java.io.FileOutputStream;
//import java.io.FileInputStream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class HuffmanBetterEnDe extends HuffmanEncoderDecoder11 {

	public static void main(String[] args) {
		Path fileLocation = Paths.get("C:\\\\my11\\\\IORunner.txt");
		
		try {
			System.out.println(Arrays.toString(readFile(fileLocation)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HuffmanBetterEnDe() {
		// TODO Auto-generated constructor stub
	}

	public static String areadFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
	
	public static byte[] readFile(Path path) throws IOException {
		return Files.readAllBytes(path);
	}
	
	

}
