package pfs;

import gsp.GSP2;

import java.io.BufferedReader;
import java.io.File;
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

import com.mathworks.toolbox.javabuilder.MWException;

import common.Computer;
import common.Constract;
import common.Distribution;
import common.StrUtil;
import computeVar.ComputeVar;

/**
 * @author zyli
 * 
 */
public class pfs {
	private List<String> superFrequentList = new ArrayList<String>();
	private List<String> globleFrequentList = new ArrayList<String>();
	private static double minsup;
	private static int dbSize;
	private static int sampleSize;
	private static int limitLen;
	private static String prefix;
	private static double lower;// 低估概率, 值小于0.5
	private static int maxFreSeqLen;
	private static ComputeVar computeVar;

	public static void main(String[] args) throws MWException {
		computeVar = new ComputeVar();
		pfs mspx = new pfs();
		String dataset = "House";
		maxFreSeqLen = 7;
		limitLen = 30;
		dbSize = 40986;
		sampleSize = (int) Math.ceil((dbSize / (double) (maxFreSeqLen - 1)));
		minsup = 0.38;
		lower = 0.2;
		double e1 = 0.1;
		double e2 = 0.45;
		double e3 = 0.45;

		
		////////////////////////////////////////////////////////////////////////////
		String src = ".\\dataset\\" + dataset + "\\"
				+ dataset + ".dat";
		String dest = ".\\dataset\\" + dataset + "\\" + dataset + "_" + minsup
				 + "_newMspx.txt";
		String correctSeqPath = ".\\dataset\\" + dataset + "\\" + dataset + "_" + minsup + "_-1_gsp.txt";
		prefix = ".\\dataset\\" + dataset + "\\"
				+ "\\sample\\" + maxFreSeqLen + "\\";
		// sample 数据库
		System.out.println("sample begin...");
		// TODO
		// SampleDataBase.sampleDataBase(src, prefix, maxFreSeqLen - 1);
		System.out.println("sample done~");
		// 获取频繁1-序列
		mspx.initial_phase(src, dest, e1);
		System.out.println("initial phase done~");
		
		// 获取 super maximal frequent sequence set
		List<String> superMFS = mspx.button_up_phase(src, dest, e2, e3);
		System.out.println("frequent: " + superMFS.size());
		System.out.println("button up phase done~");
		Constract.constract(correctSeqPath, dest);
		System.out.println("Done~");
	}

	private void initial_phase(String src, String dest, double privacy) {
		File file = new File(dest);
		try {
			if (file.exists())
				file.delete();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// frequent itemset after adding noise
		superFrequentList = firstScan(src, dest, privacy);
		if (superFrequentList != null)
			globleFrequentList.addAll(superFrequentList);
		return;
		
	}

	private List<String> firstScan(String src, String dest, double privacy) {
		List<String> resultList = new ArrayList<String>();
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		int nMaxSeqLen = 1;
		try {
			BufferedReader r = new BufferedReader(new FileReader(src));
			String seq = null;
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				int nSeqLen = StrUtil.strLen(seq, " ");
				if (nSeqLen < 1)
					continue;
				else if( nSeqLen > nMaxSeqLen ){
					nMaxSeqLen = nSeqLen;
				}
				
			 	// Trunc at given length
				Set<String> itemOccuredList = randomTrun( seq );
				
				// Count occurance globally
				for (String item : itemOccuredList) {
					if (!countMap.containsKey(item)) {
						countMap.put(item, 1);
					} else {
						countMap.put(item, countMap.get(item) + 1);
					}
				}
			}
			r.close();

			// ?
			int sensitivity = Math.min( countMap.size(), limitLen);
			System.out.println( "The sensitivity is:" + sensitivity );
			for (String item : countMap.keySet()) {
				double noisy = Distribution.laplace(privacy, sensitivity);
				double support = noisy + countMap.get(item);
				
				// Add if frequent
				if (support >= minsup * dbSize) {
					resultList.add(item);
					write(dest, item + ":" + support);
				}
			}
			System.out.println(countMap.size() + "," + resultList.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	private HashSet<String> randomTrun( String sSubOneSeq ){
		
		HashSet<String> countSet = new HashSet<String>();
		
		List<String> lSubOneSeq = Arrays.asList(sSubOneSeq.split("\\s+"));
		
		for( String seq : lSubOneSeq ){
			if( countSet.size() >= limitLen ){
				break;
			}
			
			countSet.add( seq );
		}
		
		return countSet;
	}

	/**
	 * get a superset of all frequent sequence
	 * 
	 * @param src
	 * @param sample
	 */
	private List<String> button_up_phase(String src, String dest, double pr1,
			double pr2) {
		List<String> candidateList;
		int L = 2;
		while (superFrequentList.size() > 0 && L <= maxFreSeqLen) {
			System.out.println("generating frequent-" + L + " sequence~");
			candidateList = generateCandidateSeqList(superFrequentList);   // Generating possible candidates
			if (candidateList.size() < 1)
				break;
			String sampleAddr = prefix + "slice_" + L + ".txt";
			superFrequentList = generateFrequentSeqList(src, sampleAddr, dest,
					candidateList, L, pr1, pr2 / (L - 1));
			if (superFrequentList != null)
				globleFrequentList.addAll(superFrequentList);
			++L;
		}
		return globleFrequentList;
	}

	/**
	 * 根据频繁k-序列产生候选频繁(k+1)-序列
	 * 
	 * @param patternList
	 * @param patternLen
	 * @return
	 */
	private List<String> generateCandidateSeqList(List<String> patternList) {
		GSP2 gsp = new GSP2();
		return gsp.generateCandidate(patternList);
	}

	/**
	 * 根据候选频繁k-序列产生频繁k-序列
	 * 
	 * @param originalDB
	 * @param sampleDB
	 * @param candidateSeqList
	 * @return
	 */
	private List<String> generateFrequentSeqList(String originalDB,
			String sampleDB, String dest, List<String> candidateSeqList,
			int fi, double pr1, double pr2) {
		// 在sample数据库中统计所有候选频繁k-序列的出现频度
		SmartTruncating2 st = new SmartTruncating2();
		Map<String, Integer> countMap = st.smartCount(candidateSeqList,
				sampleDB, limitLen, fi);                                      // Here!

		List<String> positiveCandidateList = new ArrayList<String>();
		List<String> negativeCandidateList = new ArrayList<String>();
		// 根据候选频繁序列在sample数据库中的出现频度，将序列划分成两部分（positive，negative），positive部分的序列直接作为频繁序列，negative部分的序列得经过再次的验证
		int sensitivity = Math.min(candidateSeqList.size(),
				Distribution.calculateFCT(limitLen, fi));
		
		System.out.println( "the sensitivity is: " + sensitivity );
		
		double support_small = Computer.computeVar2(computeVar, sampleSize,
				pr1, sensitivity, minsup, 0.5 - lower);
		System.out.println("small support: " + support_small);
		for (String candidate : candidateSeqList) {
			// TODO noisy add
			double estimateSupport = Distribution.laplace(pr1, sensitivity);
			if (countMap.containsKey(candidate))
				estimateSupport += countMap.get(candidate);
			if (estimateSupport >= support_small)
				negativeCandidateList.add(candidate);
			
		}
		System.out.println("negative size: " + negativeCandidateList.size()
				+ " candidate: " + candidateSeqList.size());
		// 在原数据集中统计所有negative部分中的候选序列的出现频度，如果序列的出现频度大于阈值，则其为频繁序列
		int countNum = negativeCandidateList.size();
		countMap = countSequenceListFrequence(originalDB,
				negativeCandidateList, fi);
		// TODO noisy add
		for (String candidate : negativeCandidateList) {
			int fre = countMap.get(candidate);
			double noisy = Distribution.laplace(pr2, countNum);
			double support = fre + noisy;

			if (support >= minsup * dbSize) {
				positiveCandidateList.add(candidate);
				write(dest, candidate + ":" + support);
			}
		}
		System.out.println("the frequent sequence number: "
				+ positiveCandidateList.size());
		return positiveCandidateList;
	}

	/**
	 * 在原始数据集中统计所有候选频繁k-序列的出现频度
	 * 
	 * @param src
	 * @param sequenceList
	 */
	private HashMap<String, Integer> countSequenceListFrequence(String src,
			List<String> sequenceList, int fi) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(src));
			String seq = null;
			Node root = new Node("");
			root.buildTree(sequenceList);
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				if (seq.length() < 1)
					continue;
				root.matchTree(seq);
			}
			r.close();
			return root.getSeqCount(fi);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void write(String dest, String seq) {
		try {
			FileWriter w = new FileWriter(dest, true);
			w.write(seq + "\r\n");
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}