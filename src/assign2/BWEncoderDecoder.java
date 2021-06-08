package assign2;

import java.util.*;

/**
 * Assignment 2
 * Submitted by: 
 * Alex Chen. 	ID# 312286545
 * Pinhas Ziv. 	ID# 315709139
 */

import base.Compressor;

public class BWEncoderDecoder implements Compressor {

	public static void main(String[] args) {

		String toCompress = "abra cadabra";

		BWEncoderDecoder bwe = new BWEncoderDecoder();
		String[] names = new String[1];
		names[0] = toCompress;
		String[] outNames = new String[2];
		bwe.Compress(names, outNames);
		bwe.Decompress(outNames, outNames);

	}

	private int size; // length of string input
	private String L; // last column of sortPermotations
	private String X; // index of the original string in sortPermotations
	private String sortedL; // // last column of sortPermotations sorted
	private int[] T; // array represent the traverse over sortedL using index in L
	private List<String> stringPermutations;
	private List<String> sortPermutations;
	private HashMap<Character, LinkedList<Integer>> indexMap; // key: each unique char in the string.
														      // value: linked list of integers represent the indexes this char locate in L.
	public BWEncoderDecoder() {
		this.stringPermutations = new ArrayList<>();
		this.L = "";
		this.X = "";
		this.indexMap = new HashMap<>();
	}

	@Override
	public void Compress(String[] input_names, String[] output_names) {
		this.size = input_names[0].length();
		this.stringPermutations.add(input_names[0]);
		fillStringPermutations();
		this.sortPermutations = sortStringPermutations(this.stringPermutations);
		setL();
		setX(input_names[0]);
		output_names[0] = this.L;
		output_names[1] = this.X;

		System.out.println("Compress successfully completed!");
	}

	// Fills an ArrayList of strings with all the transforms of the original string
	public void fillStringPermutations() {
		for (int i = 1; i < this.size; i++) {
			String previous = this.stringPermutations.get(i - 1);
			this.stringPermutations.add(previous.substring(1) + previous.charAt(0));
		}
	}

	// sorting ArrayList using quickSort
	public static List<String> sortStringPermutations(List<String> list) {
		if (list.isEmpty())
			return list;
		List<String> smaller = new ArrayList<String>();
		List<String> greater = new ArrayList<String>();
		String pivot = list.get(0);
		int i;
		String j = "";
		for (i = 1; i < list.size(); i++) {
			j = list.get(i);
			if (j.compareTo(pivot) < 0)
				smaller.add(j);
			else
				greater.add(j);
		}
		smaller = sortStringPermutations(smaller);
		greater = sortStringPermutations(greater);
		smaller.add(pivot);
		smaller.addAll(greater);

		return smaller;
	}

	// optional: to print the permutation table
	public static void printArrayList(List<String> list) {
		for (int i = 0; i < list.get(0).length(); i++) {
			System.out.println(list.get(i));
		}
	}

	// Initialize L using the last char in each string after sorting the permutation list
	public void setL() {
		for (int i = 0; i < this.size; i++) {
			char temp = this.sortPermutations.get(i).charAt(this.sortPermutations.get(i).length() - 1);
			this.L += temp;
		}
	}

	// Find the index of the original string in the sorted permutations table
	// (by comparing each string against the original string)
	public void setX(String code) {
		for (int i = 0; i < this.sortPermutations.size(); i++) {
			if (this.sortPermutations.get(i) == code) {
				this.X = String.valueOf(i);
			}
		}
	}

	@Override
	public void Decompress(String[] input_names, String[] output_names) {
		this.size = input_names[0].length();
		this.L = input_names[0];
		this.X = input_names[1];
		this.sortedL = sortL(this.L);
		fillIndexMap();
		this.T = new int[this.size];
		fillT();
		output_names[0] = rebuildString();
		System.out.println("Decompress successfully completed!");
	}

	// Sort the string L
	public static String sortL(String str) {
		char[] strChar = str.toCharArray();
		Arrays.sort(strChar);
		return new String(strChar);
	}

	// Filling in a hash map by running on the L string
	// and entering the index of each char in the corresponding linkedList.
	public void fillIndexMap() {
		for (int i = 0; i < this.size; i++) {
			char curr = this.L.charAt(i);
			if (this.indexMap.containsKey(curr)) {
				this.indexMap.get(curr).add(i);
			} else {
				LinkedList<Integer> list = new LinkedList<>();
				list.add(i);
				this.indexMap.put(curr, list);
			}
		}
	}

	// Filling in T[] by running on the sortedL string, finding the index list
	// corresponding to the char (using the hash map) and entering the indexes in T[].
	public void fillT() {
		for (int i = 0; i < this.size; i++) {
			char currChar = this.sortedL.charAt(i);
			this.T[i] = this.indexMap.get(currChar).remove();
		}
	}

	// Restore the original string by running on sortedL string
	// according to the indexes stored in T[]
	public String rebuildString() {
		String code = "";
		int ptr = Integer.parseInt(this.X);
		for (int i = 0; i < this.size; i++) {
			code += this.sortedL.charAt(ptr);
			ptr = this.T[ptr];
		}
		return code;
	}

	@Override
	public byte[] CompressWithArray(String[] input_names, String[] output_names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] DecompressWithArray(String[] input_names, String[] output_names) {
		// TODO Auto-generated method stub
		return null;
	}

}
