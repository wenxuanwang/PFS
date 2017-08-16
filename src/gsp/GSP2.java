package gsp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.PatternDeal2;

public class GSP2 {
	private BufferedReader r;
	private FileWriter w;

	public static void main(String[] args) {
		//
		double correspondThreshold = 0.02;
		
		String sDataset = "msnbc";
		int    dbSize   = 989818;//83214;
		String src = "D:\\Programs\\Java\\Eclipse\\FSM2\\dataset\\" + sDataset + "\\" + sDataset + ".dat";
		String dest = "D:\\Programs\\Java\\Eclipse\\FSM2\\dataset\\" + sDataset + "\\" + sDataset + "_" + correspondThreshold + "_-1_gsp.txt";
		double threshold = dbSize * correspondThreshold;
		GSP2 gsp2 = new GSP2();
		gsp2.initWriter(dest);
		long begin = System.currentTimeMillis();
		gsp2.callGSP(src, dest, threshold);
		long end = System.currentTimeMillis();
		System.out.println("Time consuming: " + (end - begin));
		gsp2.closeWriter();
		System.out.println("Done~");
	}

	private void callGSP(String src, String dest, double threshold) {
		List<String> k_patternList = firstScan(src, threshold);
		int L = 1;
		while (k_patternList.size() > 0) {
			System.out.println("mining frequent " + ++L + "-sequence...");
			// TODO

			k_patternList = countCandidate(src,
					generateCandidate(k_patternList), threshold);
			// }
		}
	}

	/**
	 * ªÒ»°∆µ∑±k-–Ú¡–
	 * 
	 * @param src
	 * @param length
	 * @param threshold
	 * @return
	 */
	public List<String> get_frequent_k_seq_List(String src, int length,
			double threshold) {
		int L = 1;
		List<String> k_patternList = firstScan(src, threshold);
		while (L < length && k_patternList.size() > 0) {
			System.out.println("generate...");
			k_patternList = countCandidate(src,
					generateCandidate(k_patternList), threshold);
			if (++L == length)
				return k_patternList;
		}
		return k_patternList;
	}

	/**
	 * the first scan of the data set
	 * 
	 * @param src
	 * @return the 1-item sequence pattern list
	 */
	private List<String> firstScan(String src, double threshold) {
		Map<String, Integer> countMap = new TreeMap<String, Integer>();
		List<String> resultList = new ArrayList<String>();
		initReader(src);
		try {
			String seq = null;
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				if (seq.length() < 1)// || StrUtil.strLen(seq, " ") > limit)//
										// TODO
					continue;
				Set<String> itemOccuredList = new HashSet<String>(
						Arrays.asList(seq.split("\\s+")));
				for (String item : itemOccuredList) {
					if (!countMap.containsKey(item)) {
						countMap.put(item, 1);
					} else {
						countMap.put(item, countMap.get(item) + 1);
					}
				}
			}
			r.close();
			for (String item : countMap.keySet()) {
				if (countMap.get(item) >= threshold) {
					resultList.add(item);
					write(item + ":" + countMap.get(item));// +
				}
			}
			System.out.println(countMap.size() + "," + resultList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultList;
	}

	/**
	 * count the frequency of the candidate pattern sequence
	 * 
	 * @param src
	 * @param candidateList
	 * @param threshold
	 * @return pattern sequence
	 */
	public List<String> countCandidate(String src, List<String> candidateList,
			double threshold) {
		System.out.println("count candidate...");
		initReader(src);
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		List<String> resultList = new ArrayList<String>();
		try {
			String seq = null;
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				if (seq.length() < 1)// || StrUtil.strLen(seq, " ") > limit)//
										// TODO
					continue;

				// System.out.println(seq + ":");
				for (String candidate : candidateList) {
					if (PatternDeal2.isPatternContained(seq, candidate, " ")) {
						// System.out.println(candidate);
						if (countMap.containsKey(candidate)) {
							countMap.put(candidate, countMap.get(candidate) + 1);
						} else {
							countMap.put(candidate, 1);
						}
					}
				}
			}
			r.close();
			for (String candidate : countMap.keySet()) {
				if (countMap.get(candidate) >= threshold) {
					resultList.add(candidate);
					write(candidate + ":" + countMap.get(candidate)); // TODO
				}
			}
			System.out.println("Candidate size: " + candidateList.size()
					+ ", frequent size: " + resultList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultList;
	}

	/**
	 * generate candidate (L+1)-pattern sequence by L-pattern sequence
	 * 
	 * @param patternList
	 * @param patternLen
	 * @return
	 */
	public List<String> generateCandidate(List<String> patternList) {
		System.out.println("generate candidate...");
		HashSet<String> resultSet = new HashSet<String>();
		for (int i = 0; i < patternList.size(); i++) {
			for (int j = 0; j < patternList.size(); j++) {
				String str1 = patternList.get(i);
				String str2 = patternList.get(j);
				String candidate = joinPattern(str1, str2);
				if (candidate != null && judgeCandidate(candidate, patternList)) {
					resultSet.add(candidate);
				}
			}
		}
		return new ArrayList<String>(resultSet);
	}

	private String joinPattern(String str1, String str2) {
		if (str1.split("\\s+").length == 1) {
			return str1 + " " + str2;
		}
		int index = str1.indexOf(" ");
		String subStr1 = str1.substring(index + 1);
		String subStr2 = str2.substring(0, str2.lastIndexOf(" "));
		if (subStr1.equals(subStr2))
			return str1.substring(0, index) + " " + str2;
		else
			return null;
	}

	/**
	 * judge whether all the k-1 subsequence of candidate pattern sequence are
	 * contained by the k-1 pattern sequence set
	 * 
	 * @param candidate
	 * @param patternList
	 * @return
	 */
	private boolean judgeCandidate(String candidate, List<String> patternList) {
		List<String> elements = PatternDeal2.getAllSubSequence(candidate, " ");
		boolean flag = true;
		for (String element : elements) {
			if (!patternList.contains(element)) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	private void initReader(String src) {
		try {
			r = new BufferedReader(new FileReader(src));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initWriter(String dest) {
		try {
			w = new FileWriter(dest);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(String content) {
		try {
			w.write(content + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeWriter() {
		try {
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
