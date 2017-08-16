package common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class Constract {
	public static double constract(String correctPath, String publishPath) {
		double Fscore = 0;;
		try {
			HashMap<String, Double> correct = new HashMap<String, Double>();
			BufferedReader r = new BufferedReader(new FileReader(correctPath));
			String row = null;
			while ((row = r.readLine()) != null) {
				row = row.trim();
				if (row.length() < 1)
					continue;
				String[] infos = row.split(":");
				correct.put(infos[0].trim(), Double.valueOf(infos[1].trim()));
			}
			r.close();
			r = new BufferedReader(new FileReader(publishPath));
			row = null;
			int count = 0;
			int common = 0;
			double su = 0;
			List<Double> lost = new ArrayList<Double>();
			while ((row = r.readLine()) != null) {
				row = row.trim();
				if (row.length() < 1)
					continue;
				++count;
				String[] infos = row.split(":");
				String key = infos[0].trim();
				if (correct.containsKey(key)) {
					++common;
					double lose = Math.abs((correct.get(key) - Double
							.valueOf(infos[1].trim()))
							/ (double) correct.get(key));
					su += lose;
					lost.add(lose);
				}
			}
			r.close();
			Collections.sort(lost);
			System.out.println("NUM: " + common);
			double precision = common / (double) count;
			double recall = common / (double) correct.size();
			System.out.println("precision: " + precision);
			System.out.println("recall: " + recall);
			Fscore = 2 * precision * recall / (precision + recall);
			System.out.println("F-score: " + Fscore);
			System.out.println("RE:" + lost.get(lost.size() / 2 - 1));
			System.out.println("Avg:" + su / (double) common);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return Fscore;
	}

	
	public static void main(String[] args) {
		
		 String strCorrectPath = "D:\\Programs\\Java\\Eclipse\\FSM2\\dataset\\msnbc\\msnbc_0.02_-1_gsp.txt";
		 String strPublishPath = "D:\\Programs\\Java\\Eclipse\\FSM2\\dataset\\msnbc_prefix_1.0l\\msnbc_prefix_1.0l_0.02_-1_gsp_ab.txt";
		 String strOriPath     = "D:\\Programs\\Java\\Eclipse\\FSM2\\dataset\\msnbc\\msnbc.dat";
		 


	}

	
}
