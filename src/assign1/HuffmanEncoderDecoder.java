package assign1;

/**
 * Assignment 1
 * Submitted by: 
 * Student 1. 	ID# XXXXXXXXX
 * Student 2. 	ID# XXXXXXXXX
 */

// Uncomment if you wish to use FileOutputStream and FileInputStream for file access.
//import java.io.FileOutputStream;
//import java.io.FileInputStream;

import java.io.*;
import base.Compressor;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class HuffmanEncoderDecoder implements Compressor {

	private byte[] fileInBytes;
	private TwoBytes[] fileInTwoBytes;
	private HashMap<HuffmanNode, Integer> hashFreqMap;
	private PriorityQueue<HuffmanNode> queueByFreq;
	private HuffmanNode huffmanTree;
	private HashMap<TwoBytes, String> hashStringMap;
	private BitSet bitset;
	private int bitSetIndex = 0;
	private byte lastByte;
	private boolean isOdd = false;
	private HashMap<String, HuffmanNode> hashDecodeMap;

	public static void main(String[] args) {
		HuffmanEncoderDecoder aaa = new HuffmanEncoderDecoder();
		String path = "C:\\my11\\aaa.txt";
		String outPath = "C:\\my11\\aaaOutput.txt";
		String decompressed = "C:\\my11\\bbb.txt";
		
//		String path = "C:\\my11\\smiley.bmp";
//		String outPath = "C:\\my11\\aaaOutput.txt";
//		String decompressed = "C:\\my11\\smiley111.bmp";

		String[] input = { path };
		String[] output = { outPath, decompressed };
		aaa.Compress(input, output);

		aaa.Decompress(output, output);
	}

	public HuffmanEncoderDecoder() {
		this.huffmanTree = new HuffmanNode();
		this.hashFreqMap = new HashMap<>();
		this.hashStringMap = new HashMap<>();
		this.queueByFreq = new PriorityQueue<>();
		this.bitset = new BitSet();
	}

	@Override
	public void Compress(String[] input_names, String[] output_names) {
		readBytesFromFile(input_names[0]);
		convertToTwoBytes();
		fillFreqMap();
		sortByFreq();
		makeHuffmanTree();
		huffmanEncodingWords(huffmanTree, "", hashStringMap);
		makeBitset();
		writeOutputFile(output_names[0]);
	}

	@Override
	public void Decompress(String[] input_names, String[] output_names) {
		readBytesFromFile(input_names[0]);
		System.out.println();
		this.bitset = BitSet.valueOf(this.fileInBytes);
//		for (int i = 0; i < this.bitset.length(); i++) {
//			boolean get = this.bitset.get(i);
//			int p = (get == true) ? 1 : 0;
//			System.out.print(p);
//		}
		if (this.bitset.get(0)) {
			this.isOdd = true;
			getLastByte();
		}
		int numOfWord = getNumOfWords();
		System.out.println("numOfWOrd: " + numOfWord);
		getCode(numOfWord);
		treeReconstruction();
		ArrayList<TwoBytes> codeInTwoBytes = decodeByTree();
		byte[] codeInBytes;
		if (this.isOdd) {
			codeInBytes = new byte[(codeInTwoBytes.size() * 2) + 1];
			codeInBytes[codeInBytes.length - 1] = this.lastByte;
			System.out.println(Arrays.toString(codeInBytes));
			System.out.println("length: " + codeInTwoBytes.size());

		} else {
			codeInBytes = new byte[codeInTwoBytes.size() * 2];
		}
		int i = 0;
		for (TwoBytes tb : codeInTwoBytes) {
			codeInBytes[i++] = tb.bytes[0];
			codeInBytes[i++] = tb.bytes[1];
		}
		System.out.println(Arrays.toString(codeInBytes));

		try (FileOutputStream fos = new FileOutputStream(output_names[1])) {
			fos.write(codeInBytes);

		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public void readBytesFromFile(String file_name) {
		File file = new File(file_name);
		try {
			this.fileInBytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException("can't read file " + file_name);
		}
	}

	public void convertToTwoBytes() {
		if (this.fileInBytes.length % 2 != 0) {
			this.isOdd = true;
			this.lastByte = fileInBytes[fileInBytes.length - 1];
		}
		this.fileInTwoBytes = new TwoBytes[(this.fileInBytes.length / 2)];
		for (int i = 0; 2 * i < this.fileInBytes.length - 1; i++) {
			this.fileInTwoBytes[i] = new TwoBytes(this.fileInBytes[2 * i], this.fileInBytes[(2 * i) + 1]);
		}
	}

	public void fillFreqMap() {
		for (int i = 0; i < this.fileInTwoBytes.length; i++) {
			TwoBytes currWord = this.fileInTwoBytes[i];
			HuffmanNode currNode = new HuffmanNode(currWord, currWord.getFreq());
			int newValue = this.hashFreqMap.containsKey(currNode) ? this.hashFreqMap.get(currNode) + 1 : 1;
			this.fileInTwoBytes[i].setFreq(newValue);
			currNode.setFreq(newValue);
			this.hashFreqMap.remove(currNode);
			this.hashFreqMap.put(currNode, newValue);
		}
	}

	public void sortByFreq() {
		this.queueByFreq.addAll(this.hashFreqMap.keySet());
	}

	public void makeHuffmanTree() {
		if (this.queueByFreq.size() == 1) {
			if (this.queueByFreq.peek().isLeaf()) {
				this.huffmanTree.setLeftChild(this.queueByFreq.poll());
			} else {
				this.huffmanTree = queueByFreq.poll();
			}
		} else {
			HuffmanNode right = queueByFreq.poll();
			HuffmanNode left = queueByFreq.poll();
			HuffmanNode father = makeFather(left, right);
			this.queueByFreq.add(father);
			makeHuffmanTree();
		}
	}

	public static HuffmanNode makeFather(HuffmanNode left, HuffmanNode right) {
		HuffmanNode father = new HuffmanNode();
		father.setLeftChild(left);
		father.setRightChild(right);
		father.setFreq(left.getFreq() + right.getFreq());
		return father;
	}

	public void huffmanEncodingWords(HuffmanNode node, String code, HashMap<TwoBytes, String> hashStringMap) {
		if (node.isLeaf()) {
			node.setCode(code);
			hashStringMap.put(node.getWord(), node.getCode());
		} else {
			if (node.getLeftChild() != null) {
				huffmanEncodingWords(node.getLeftChild(), code + '0', hashStringMap);
			}
			if (node.getRightChild() != null) {
				huffmanEncodingWords(node.getRightChild(), code + '1', hashStringMap);
			}
		}
	}

	public void makeBitset() {
		// first bit represent number of bytes (odd = 1, even = 0)
		this.bitset.set(this.bitSetIndex++, this.isOdd);
		if (this.isOdd) { // add last byte before prelude (if exist)
			String last = paddByte(this.lastByte);
			for (int i = 0; i < last.length(); i++) {
				boolean write = (last.charAt(i) == '0') ? false : true;
				this.bitset.set(bitSetIndex++, write);
			}
		} else { // pad first byte to zeros (in not exist)
			for (int i = 0; i < 8; i++) {
				this.bitset.set(this.bitSetIndex++, false);
			}
		}
		String prelude = getPrelude();
		for (int i = 0; i < prelude.length(); i++) {
			boolean write = (prelude.charAt(i) == '0') ? false : true;
			this.bitset.set(bitSetIndex++, write);
		}
		for (int i = 0; i < this.fileInTwoBytes.length; i++) {
			String huffmanWord = this.hashStringMap.get(fileInTwoBytes[i]);
			for (int j = 0; j < huffmanWord.length(); j++) {
				boolean write = (huffmanWord.charAt(j) == '1') ? true : false;
				this.bitset.set(this.bitSetIndex++, write);
			}
		}

		// set last bit to 1 (to prevent avoiding data of zeros).
		this.bitset.set(this.bitSetIndex);

//		StringBuilder str = new StringBuilder();
//		for (int i = 0; i < bitset.length(); i++) {
//			str.append(bitset.get(i) == true ? 1 : 0);
//		}
//		System.out.println(str);
	}

	public String getPrelude() {
		Set<TwoBytes> words = this.hashStringMap.keySet();
		int size = words.size();
		String prelude = paddPreludeSize(size);
		for (TwoBytes tb : words) {
			prelude += getWordCode(tb);
		}
		return prelude;
	}

	// Returns word encoding: 16bits for the word, the encoding length of the word
	// (in unary base) and the encoding itself
	public String getWordCode(TwoBytes tb) {
		String codeWord = paddByte(tb.bytes[0]) + paddByte(tb.bytes[1]);
		int codeSize = this.hashStringMap.get(tb).length();
		while (codeSize > 1) { // Encoding the code length of the current word in unary base
			codeWord += '1';
			codeSize--;
		}
		codeWord += '0';
		codeWord += hashStringMap.get(tb);
		return codeWord;
	}

	public static String paddByte(byte b) {
		String last = Integer.toBinaryString(b);
		if (last.length() < 8) {
			int padd = 8 - last.length();
			char[] paddingChar = new char[padd];
			Arrays.fill(paddingChar, '0');
			String lastWithPadd = new String(paddingChar);
			lastWithPadd += last;
			return lastWithPadd;
		} else {
			return last;
		}
	}

	public static String paddPreludeSize(int size) {
		String sizeInBytes = Integer.toBinaryString(size);
		if (sizeInBytes.length() < 16) {
			int padd = 16 - sizeInBytes.length();
			char[] paddingChar = new char[padd];
			Arrays.fill(paddingChar, '0');
			String lastWithPadd = new String(paddingChar);
			lastWithPadd += sizeInBytes;
			return lastWithPadd;
		} else {
			return sizeInBytes;
		}
	}

	public void writeOutputFile(String path) {
		File file = new File(path);
		byte[] compressedCode = this.bitset.toByteArray();
		try {
			OutputStream os = new FileOutputStream(file);
			os.write(compressedCode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getLastByte() {
		BitSet last = this.bitset.get(1, 9);
		String strByte = "";
		for (int i = 0; i < last.length(); i++) {
			strByte += (last.get(i) == true) ? 1 : 0;
		}
		this.lastByte = (byte) Integer.parseInt(strByte, 2);
	//	this.lastByte = Byte.parseByte(strByte, 2);
	}

	public int getNumOfWords() {
		BitSet wordsNum = this.bitset.get(9, 25);
		String strSize = "";
		for (int i = 0; i < 16; i++) {
			strSize += (wordsNum.get(i) == true) ? 1 : 0;
		}
		return Integer.parseInt(strSize, 2);
		// return (int) Byte.parseByte(strSize, 2);
	}

	public void getCode(int numOfWords) {
		this.hashDecodeMap = new HashMap<>();
		this.bitSetIndex = 25;
		int codeSize = 1;
		String decode = "";
		for (int i = 0; i < numOfWords; i++) {
			TwoBytes word = getWord(this.bitSetIndex); // function receive index of word start
			this.bitSetIndex += 16;
			while (this.bitset.get(this.bitSetIndex++)) { // get size of code for this word (in unary base)
				codeSize++;
			}
			while (codeSize > 0) { // get the code of this word
				boolean add = this.bitset.get(this.bitSetIndex++);
				decode += (add == true) ? '1' : '0';
				codeSize--;
			}
			HuffmanNode node = new HuffmanNode(word, 0);
			node.setCode(decode);
			this.hashDecodeMap.put(decode, node);
			codeSize = 1;
			decode = "";
		}
	}

	public TwoBytes getWord(int index) {
		BitSet char1 = new BitSet(8);
		char1 = this.bitset.get(index, index + 8);
		String strChar1 = "";
		for (int i = 0; i < 8; i++) {
			strChar1 += (char1.get(i) == true) ? 1 : 0;
		}
		byte byte1 = (byte) Integer.parseInt(strChar1, 2);

		BitSet char2 = this.bitset.get(index + 8, index + 16);
		String strChar2 = "";
		for (int i = 0; i < 8; i++) {
			strChar2 += (char2.get(i) == true) ? 1 : 0;
		}
		byte byte2 = (byte) Integer.parseInt(strChar2, 2);
		return new TwoBytes(byte1, byte2);
	}

	public void treeReconstruction() {
		this.huffmanTree = new HuffmanNode();
		HuffmanNode root = this.huffmanTree;
		Collection<HuffmanNode> nodes = this.hashDecodeMap.values();
		for (HuffmanNode node : nodes) {
			int codeSize = node.getCode().length();
			for (int i = 0; i < codeSize; i++) {
				if (node.getCode().charAt(i) == '0') {
					if (root.getLeftChild() == null) {
						root.setLeftChild(new HuffmanNode());
					}
					root = root.getLeftChild();
				} else {
					if (root.getRightChild() == null) {
						root.setRightChild(new HuffmanNode());
					}
					root = root.getRightChild();
				}
			}
			root.setCode(node.getCode());
			root.setWord(node.getWord());
			root = this.huffmanTree;
		}
	}

	public ArrayList<TwoBytes> decodeByTree() {
		ArrayList<TwoBytes> decompressedCode = new ArrayList<>();
		HuffmanNode root = this.huffmanTree;
		for (int i = this.bitSetIndex; i < this.bitset.length() - 1; i++) {
			System.out.print(this.bitset.get(i) ? 1 : 0);
			if (this.bitset.get(i)) {
				root = root.getRightChild();
			} else {
				root = root.getLeftChild();
			}
			if (root.isLeaf()) {
				decompressedCode.add(root.getWord());
				root = this.huffmanTree;
				System.out.println();
			}
		}
		return decompressedCode;
	}

	private static class TwoBytes implements Comparable<TwoBytes> {
		private final byte[] bytes = new byte[2];
		private int freq;

		public TwoBytes(byte b0, byte b1) {
			bytes[0] = b0;
			bytes[1] = b1;
			this.freq = 0;
		}

		public int getFreq() {
			return freq;
		}

		public void setFreq(int freq) {
			this.freq = freq;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof TwoBytes))
				return false;
			TwoBytes twoBytes = (TwoBytes) obj;
			return Arrays.equals(bytes, twoBytes.bytes);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(bytes);
		}

		@Override
		public int compareTo(TwoBytes other) {
			return Integer.compare(this.getFreq(), other.getFreq());
		}

		@Override
		public String toString() {
			return Arrays.toString(bytes);
		}

	}

	private static class HuffmanNode implements Comparable<HuffmanNode> {

		private TwoBytes word;
		private int freq;
		private String code = "";
		private HuffmanNode leftChild = null;
		private HuffmanNode rightChild = null;

		public HuffmanNode() {
			this.word = null;
		}

		public HuffmanNode(TwoBytes word, int freq) {
			this.word = word;
			this.freq = freq;
		}

		public TwoBytes getWord() {
			return word;
		}

		public void setWord(TwoBytes word) {
			this.word = word;
		}

		public int getFreq() {
			return freq;
		}

		public void setFreq(int freq) {
			this.freq = freq;
		}

		public HuffmanNode getLeftChild() {
			return leftChild;
		}

		public void setLeftChild(HuffmanNode leftChild) {
			this.leftChild = leftChild;
		}

		public HuffmanNode getRightChild() {
			return rightChild;
		}

		public void setRightChild(HuffmanNode rightChild) {
			this.rightChild = rightChild;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public boolean isLeaf() {
			return (this.getLeftChild() == null && this.getRightChild() == null);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof HuffmanNode))
				return false;
			HuffmanNode node = (HuffmanNode) obj;
			return this.getWord().equals(node.getWord());
		}

		@Override
		public int hashCode() {
			return this.getWord().hashCode();
		}

		@Override
		public String toString() {
			return "HuffmanNode [word=" + word.toString() + ", freq=" + freq + "]";
		}

		@Override
		public int compareTo(HuffmanNode other) {
			return Integer.compare(this.getFreq(), other.getFreq());
		}
	}
}
