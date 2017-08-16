package pfs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SampleDataBase {

	static Random rd = new Random();

	public static void sampleDataBase(String src, String prefix, int pieces) {
		prefix += "slice_";
		int begin = 2;
		
		// Generate empty files
		try {
			for (int i = 0; i < pieces; ++i) {
				String path = prefix + (begin + i) + ".txt";
				FileWriter w = new FileWriter(path);
				w.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Randomly write to files
		try {
			BufferedReader r = new BufferedReader(new FileReader(src));
			String row = null;
			while ((row = r.readLine()) != null) {
				row = row.trim();
				if (row.length() < 1)
					continue;
				int rn = rd.nextInt(pieces) + begin;
				String destAddr = prefix + rn + ".txt";
				FileWriter w = new FileWriter(destAddr, true);

				w.write(row + "\r\n");
				w.close();
			}
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
