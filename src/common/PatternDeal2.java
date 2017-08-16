package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PatternDeal2 {

	public static void main(String[] args) {
		getAllSubSequence("a", " ");
		// isPatternContained("a", "a", " ");
	}

	/**
	 * get all subsequence of the pattern, but don't contain itself
	 * 
	 * @param pattern
	 * @param delimiter
	 *            ³¤¶ÈÎª1
	 * @return
	 */
	public static List<String> getAllSubSequence(String pattern,
			String delimiter) {
		if(pattern.length() == 0)
			return null;
		HashSet<String> subSequenceSet = new HashSet<String>();
		String[] eles = pattern.split(delimiter);
		if (eles.length == 1) {
			//subSequenceSet.add(eles[0]);
		} else {
			for (int i = 0; i < eles.length; i++) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < eles.length; j++) {
					if (i == j)
						continue;
					buffer.append(eles[j] + delimiter);
				}
				subSequenceSet.add(buffer.substring(0, buffer.length() - 1));
			}
		}
		//System.out.println(pattern+":"+subSequenceSet.size());
		return new ArrayList<String>(subSequenceSet);
	}

	/**
	 * judge whether the pattern contain the subPattern
	 * 
	 * @param pattern
	 * @param subPattern
	 * @return
	 */
	public static boolean isPatternContained(String pattern, String subPattern,
			String delimiter) {
		if (subPattern.length() > pattern.length())
			return false;
		int fromIndex = -1;
		boolean flag = false;
		String[] eles = subPattern.split(delimiter);
		pattern = delimiter + pattern.trim() + delimiter;
		for (int i = 0; i < eles.length; i++) {
			String ch = delimiter + eles[i] + delimiter;
			fromIndex = pattern.indexOf(ch);
			if (fromIndex == -1) {
				flag = true;
				break;
			}
			pattern = pattern.substring(fromIndex + ch.length() - 1);
			// System.out.println('?' + pattern + '?');
		}
		if (flag)
			return false;
		else
			return true;
	}
	
}
