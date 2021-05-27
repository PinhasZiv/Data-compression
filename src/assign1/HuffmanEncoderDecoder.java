package assign1;

/**
 * Assignment 1
 * Submitted by: 
 * Alex Chen. 	ID# 312286545
 * Pinhas Ziv. 	ID# 315709139
 */

// Uncomment if you wish to use FileOutputStream and FileInputStream for file access.
//import java.io.FileOutputStream;
//import java.io.FileInputStream;

import java.io.*;
import base.Compressor;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.*;

public class HuffmanEncoderDecoder implements Compressor {

	private byte[] fileInBytes;
	private short[] fileInShorts;
	private HashMap<HuffmanNode, Integer> hashFreqMap;
	private PriorityQueue<HuffmanNode> queueByFreq;
	private HuffmanNode huffmanTree;
	private HashMap<Short, String> hashStringMap;
	private BitSet bitset;
	private int bitSetIndex = 0;
	private byte lastByte;
	private boolean isOdd = false;
	private HashMap<String, HuffmanNode> hashDecodeMap;

	public static void main(String[] args) {

//		String path = "C:\\my11\\aaa.txt";
//		String outPath = "C:\\my11\\aaaOutput.txt";
//		String decompressed = "C:\\my11\\bbb.txt";

		String path = "C:\\my11\\Smiley.bmp";
		String outPath = "C:\\my11\\Smiley.huff";
		String decompressed = "C:\\my11\\newSmiley.bmp";

//		String path = "C:\\my11\\longFile.txt";
//		String outPath = "C:\\my11\\longFileOutput.txt";
//		String decompressed = "C:\\my11\\newLongFile.txt";

		HuffmanEncoderDecoder myCompressor = new HuffmanEncoderDecoder();
		
		String[] input = { path };
		String[] compressed = { outPath };
		String[] output = { decompressed };

		myCompressor.Compress(input, compressed);
		System.out.println();
		myCompressor.Decompress(compressed, output);
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
		fromBytesToShorts();
		fillFreqMap();
		sortByFreq();
		makeHuffmanTree();
		huffmanEncodingWords(huffmanTree, "");
		makeBitset();
		writeOutputFile(output_names[0]);
		System.out.println("The file was successfully compressed.\nCompressed file location: " + output_names[0]);
	}

	@Override
	public void Decompress(String[] input_names, String[] output_names) {
		readBytesFromFile(input_names[0]);
		this.bitset = BitSet.valueOf(this.fileInBytes);
		if (this.bitset.get(0)) {
			this.isOdd = true;
			getLastByte();
		}
		int numOfWord = getNumOfWords();
		getCode(numOfWord);
		treeReconstruction();
		ArrayList<Short> codeInShorts = decodeByTree();
		byte[] codeInBytes;
		if (this.isOdd) {
			codeInBytes = new byte[(codeInShorts.size() * 2) + 1];
			codeInBytes[codeInBytes.length - 1] = this.lastByte;
		} else {
			codeInBytes = new byte[codeInShorts.size() * 2];
		}
		int i = 0;
		for (short tb : codeInShorts) {
			codeInBytes[i++] = (byte) (tb & 0xff);
			codeInBytes[i++] = (byte) ((tb >> 8) & 0xff);
		}

		try (FileOutputStream fos = new FileOutputStream(output_names[0])) {
			fos.write(codeInBytes);

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The file was successfully restored.\nRecovered file location: " + output_names[0]);
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

	public void fromBytesToShorts() { // need to fix padding of bytes
		if (this.fileInBytes.length % 2 != 0) {
			this.isOdd = true;
			this.lastByte = fileInBytes[fileInBytes.length - 1];
		}
		this.fileInShorts = new short[this.fileInBytes.length / 2];
		for (int i = 0; 2 * i < fileInBytes.length - 1; i++) {
			byte b1 = fileInBytes[2 * i];
			byte b2 = fileInBytes[(2 * i) + 1];
			this.fileInShorts[i] = twoBytesToShort(b2, b1);
		}
	}

	public static short twoBytesToShort(byte b1, byte b2) { // from stackoverflow
		return (short) ((b1 << 8) | (b2 & 0xFF));
	}

	public void fillFreqMap() {
		for (int i = 0; i < this.fileInShorts.length; i++) {
			short currWord = this.fileInShorts[i];
			HuffmanNode currNode = new HuffmanNode(currWord, 0);
			int newValue = this.hashFreqMap.containsKey(currNode) ? this.hashFreqMap.get(currNode) + 1 : 1;
			// this.fileInTwoBytes[i].setFreq(newValue);
			currNode.setFreq(newValue);
			this.hashFreqMap.remove(currNode);
			this.hashFreqMap.put(currNode, newValue);
		}
	}

	public void sortByFreq() {
		this.queueByFreq.addAll(this.hashFreqMap.keySet());
	}

	public void makeHuffmanTree() {
		while (this.queueByFreq.size() > 1) {
			HuffmanNode right = queueByFreq.poll();
			HuffmanNode left = queueByFreq.poll();
			HuffmanNode father = makeFather(left, right);
			this.queueByFreq.add(father);
		}
		if (this.queueByFreq.peek().isLeaf()) { // If the file contains only one word in repetitions
			this.huffmanTree.setLeftChild(this.queueByFreq.poll());
		} else {
			this.huffmanTree = queueByFreq.poll();
		}

	}

	public static HuffmanNode makeFather(HuffmanNode left, HuffmanNode right) {
		HuffmanNode father = new HuffmanNode();
		father.setLeftChild(left);
		father.setRightChild(right);
		father.setFreq(left.getFreq() + right.getFreq());
		return father;
	}

	public void huffmanEncodingWords(HuffmanNode node, String code) {
		if (node.isLeaf()) {
			node.setCode(code);
			this.hashStringMap.put(node.getWord(), node.getCode());
		} else {
			if (node.getLeftChild() != null) {
				huffmanEncodingWords(node.getLeftChild(), code + '0');
			}
			if (node.getRightChild() != null) {
				huffmanEncodingWords(node.getRightChild(), code + '1');
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
		for (int i = 0; i < this.fileInShorts.length; i++) {
			String huffmanWord = this.hashStringMap.get(fileInShorts[i]);
			for (int j = 0; j < huffmanWord.length(); j++) {
				boolean write = (huffmanWord.charAt(j) == '1') ? true : false;
				this.bitset.set(this.bitSetIndex++, write);
			}
		}
		// set last bit to 1 (to prevent avoiding data of zeros).
		this.bitset.set(this.bitSetIndex);
	}

	public String getPrelude() {
		Set<Short> words = this.hashStringMap.keySet();
		int size = words.size();
		String prelude = paddPreludeSize(size);
		for (short tb : words) {
			prelude += getWordCode(tb);
		}
		return prelude;
	}

	// Returns word encoding: 16bits for the word, the encoding length of the word
	// (in unary base) and the encoding itself
	public String getWordCode(short tb) {
		byte[] word = getBytesFromShort(tb);
		String wordCode = getBinaryString(word);
		int codeSize = this.hashStringMap.get(tb).length();
		while (codeSize > 1) { // Encoding the code length of the current word in unary base
			wordCode += '1';
			codeSize--;
		}
		wordCode += '0'; // end of number in unary base
		wordCode += hashStringMap.get(tb);
		return wordCode;
	}

	public String getBinaryString(byte[] word) {
		String code = "";
		for (int i = 0; i < word.length; i++) {
			code += paddByte(word[i]);
		}
		return code;
	}

	public byte[] getBytesFromShort(short x) { // from stackoverflow
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (x & 0xff);
		bytes[1] = (byte) ((x >> 8) & 0xff);
		return bytes;
	}

	public static String paddByte(byte b) {
		String byteToPadd = Integer.toBinaryString(b);
		if (byteToPadd.length() < 8) {
			int padd = 8 - byteToPadd.length();
			char[] paddingChar = new char[padd];
			Arrays.fill(paddingChar, '0');
			String lastWithPadd = new String(paddingChar);
			lastWithPadd += byteToPadd;
			return lastWithPadd;
		} else {
			if (byteToPadd.length() > 8) { // if b is not English: Integer.toBinaryString returns string 32 bits size
				byteToPadd = byteToPadd.substring(byteToPadd.length() - 8, byteToPadd.length());
			}
			return byteToPadd;
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
		for (int i = 0; i < 8; i++) {
			strByte += (last.get(i) == true) ? 1 : 0;
		}
		this.lastByte = (byte) Integer.parseInt(strByte, 2);
	}

	public int getNumOfWords() {
		BitSet wordsNum = this.bitset.get(9, 25);
		String strSize = "";
		for (int i = 0; i < 16; i++) {
			strSize += (wordsNum.get(i) == true) ? 1 : 0;
		}
		return Integer.parseInt(strSize, 2);
	}

	public void getCode(int numOfWords) {
		this.hashDecodeMap = new HashMap<>();
		this.bitSetIndex = 25;
		int codeSize = 1;
		String decode = "";
		for (int i = 0; i < numOfWords; i++) {
			short word = getWord(this.bitSetIndex); // function receive index of word start in the bitset
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

	public short getWord(int index) {
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
		return getShortFromTwoBytes(byte1, byte2);
	}

	public short getShortFromTwoBytes(byte b1, byte b2) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(b1);
		bb.put(b2);
		return bb.getShort(0);
	}

	public void treeReconstruction() {
		this.huffmanTree = new HuffmanNode();
		HuffmanNode root = this.huffmanTree;
		Collection<HuffmanNode> nodes = this.hashDecodeMap.values();
		for (HuffmanNode node : nodes) {
			if (node.isLeaf()) {
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
	}

	public ArrayList<Short> decodeByTree() {
		ArrayList<Short> decompressedCode = new ArrayList<>();
		HuffmanNode root = this.huffmanTree;
		for (int i = this.bitSetIndex; i < this.bitset.length() - 1; i++) {
			if (this.bitset.get(i)) {
				root = root.getRightChild();
			} else {
				root = root.getLeftChild();
			}
			if (root.isLeaf()) {
				decompressedCode.add(root.getWord());
				root = this.huffmanTree;
			}
		}
		return decompressedCode;
	}

	private static class HuffmanNode implements Comparable<HuffmanNode> {

		private short word;
		private int freq;
		private String code = "";
		private HuffmanNode leftChild = null;
		private HuffmanNode rightChild = null;

		public HuffmanNode() {
		}

		public HuffmanNode(short word, int freq) {
			this.word = word;
			this.freq = freq;
		}

		public short getWord() {
			return word;
		}

		public void setWord(short word) {
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
			return this.getWord() == node.getWord();
		}

		@Override
		public int hashCode() {
			return this.getWord();
		}

		@Override
		public String toString() {
			return "HuffmanNode [word=" + word + ", freq=" + freq + "]";
		}

		@Override
		public int compareTo(HuffmanNode other) { // for Priority Queue to sort by freq
			return Integer.compare(this.getFreq(), other.getFreq());
		}
	}
}
