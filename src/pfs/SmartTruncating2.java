package pfs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.StrUtil;

public class SmartTruncating2 {
	int nCnt;
	public Map<String, Integer> smartCount(List<String> candidateList,
			String src, int limit, int fi) {
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		// Add child nodes
		Node root = Node.constructTree(candidateList);
		HashSet<String> itemOccured = Node.itemSet;
		try {
			BufferedReader r = new BufferedReader(new FileReader(src));
			String row = null;
			nCnt = 0;
			while ((row = r.readLine()) != null) {
				nCnt++;
				row = row.trim();
				if (row.length() < 1)
					continue;
				
				if (StrUtil.strLen(row, " ") > limit)
					row = shortRecord(row, itemOccured, fi);
				List<Node> preKLevelNode = new ArrayList<Node>();
				if (row.length() < 1)
					continue;

				Node subTree = generateSubTree(root, row, preKLevelNode, fi);
				
				if (1 == fi || StrUtil.strLen(row, " ") <= limit) {

					HashMap<String, Integer> tempMap = subTree.getSeqCount(row,
							fi);
					for (String key : tempMap.keySet()) {
						if (countMap.containsKey(key)) {
							countMap.put(key,
									countMap.get(key) + tempMap.get(key));
						} else {
							countMap.put(key, tempMap.get(key));
						}
					}
				} else {
					HashMap<String, Node> preKLevelMap = new HashMap<String, Node>();
					for (Node t : preKLevelNode) {
						preKLevelMap.put(t.getTotleName(), t);
					}

					HashMap<String, Integer> mNoUse = new HashMap<String, Integer>();
					row = truncate(subTree, preKLevelNode, preKLevelMap, limit,
							mNoUse, fi);
				}
				
				// 统计出现
				for (String candidate : candidateList) {
					if (StrUtil.strContain(row, candidate, " ")) {
						if (countMap.containsKey(candidate)) {
							countMap.put(candidate, countMap.get(candidate) + 1);
						} else {
							countMap.put(candidate, 1);
						}
					}
				}
				
			}
			r.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return countMap;
	}

	/**
	 * truncate the sequence, and count the candidate by the way
	 * 
	 * @param preKLevelNode
	 * @param preKLevelMap
	 * @param limit
	 * @param countMap
	 * @return
	 */
	private String truncate(Node root, List<Node> preKLevelNode,
			HashMap<String, Node> preKLevelMap, int limit,
			HashMap<String, Integer> countMap, int level) {
		List<String> candidateList = new ArrayList<String>();

		for (Node node : preKLevelNode) {
			node = node.getChild();
			while (node != null) {
				// System.out.println(node.getTotleName());
				candidateList.add(node.getTotleName());
				node = node.getSibling();
			}
		}

		String result = "";
		// greedy growth
		while (StrUtil.strLen(result, " ") < limit) {
			if (preKLevelMap.size() == 0)
				break;
			List<Node> tmpSequence = getAllContainedSeq(result, preKLevelMap);
			if (tmpSequence.size() == 0) {
				// get the candidate sequence who owns most number of children
				String maxCandidate = getMaxCandidate(root, preKLevelNode,
						candidateList, level);
				if (StrUtil.strLen(result + " " + maxCandidate, " ") > limit)
					break;

				candidateList.remove(maxCandidate);
				// and get the child of k-1 node who can generate most k-1
				// nodes' children
				if (countMap.containsKey(maxCandidate))
					countMap.put(maxCandidate, countMap.get(maxCandidate) + 1);
				else
					countMap.put(maxCandidate, 1);
				int spaceIndex = maxCandidate.lastIndexOf(" ");
				String preKstr = maxCandidate.substring(0, spaceIndex);
				String lastItem = maxCandidate.substring(spaceIndex + 1);
				Node preNode = preKLevelMap.get(preKstr);
				Node child = preNode.getChild();
				if (child.getName().equals(lastItem))
					preNode.setChild(child.getSibling());
				else {
					while (child.getSibling() != null) {
						if (child.getSibling().getName().equals(lastItem)) {
							child.setSibling(child.getSibling().getSibling());
							break;
						} else
							child = child.getSibling();
					}
				}
				result = StrUtil.strAdd(result, maxCandidate, " ");
				continue;
			} else {
				// get the most frequent item occurred in k-1 nodes' children
				HashMap<String, Integer> itemCountMap = new HashMap<String, Integer>();
				HashMap<String, List<Node>> itemSequenceMap = new HashMap<String, List<Node>>();
				for (Node t : tmpSequence) {
					Node child = t.getChild();
					while (child != null) {
						String childName = child.getName();
						if (!itemCountMap.containsKey(childName))
							itemCountMap.put(childName, 1);
						else
							itemCountMap.put(childName,
									itemCountMap.get(childName) + 1);
						if (!itemSequenceMap.containsKey(childName)) {
							List<Node> temp = new ArrayList<Node>();
							temp.add(t);
							itemSequenceMap.put(childName, temp);
						} else {
							List<Node> temp = itemSequenceMap.get(childName);
							temp.add(t);
							itemSequenceMap.put(childName, temp);
						}
						child = child.getSibling();
					}
				}
				String maxItem = null;
				int maxValue = 0;
				for (String key : itemCountMap.keySet()) {
					if (itemCountMap.get(key) > maxValue) {
						maxValue = itemCountMap.get(key);
						maxItem = key;
					}
				}
				if (maxItem != null) {

					List<Node> maxPreNodeList = itemSequenceMap.get(maxItem);
					// update candidates count
					for (Node maxPreNode : maxPreNodeList) {
						String temp = StrUtil.strAdd(maxPreNode.getTotleName(),
								maxItem, " ");

						candidateList.remove(temp);
						if (countMap.containsKey(temp))
							countMap.put(temp, countMap.get(temp) + 1);
						else
							countMap.put(temp, 1);
					}
					// update preKLevelMap
					result = StrUtil.strAdd(result, maxItem, " ");
					updatePreKLevelMap(preKLevelMap, preKLevelNode,
							itemSequenceMap.get(maxItem), maxItem);
				} else
					break;
			}
		}


		return result;
	}

	/**
	 * TODO improve effectivity
	 * 
	 * @param root
	 * @param preKLevelNode
	 * @param candidateList
	 * @param level
	 * @return
	 */
	private String getMaxCandidate(Node root, List<Node> preKLevelNode,
			List<String> candidateList, int level) {
		int maxChileNum = 0;
		List<String> maxCandidateList = new ArrayList<String>();
		for (String candidate : candidateList) {
			int curChildNum = 0;
			List<Node> quene = new ArrayList<Node>();
			root.setSeq(candidate);
			quene.add(root);
			while (quene.size() > 0) {
				Node temp = quene.remove(0);
				if (temp.getLevel() == level - 1) {
					curChildNum += temp.getChildNum();
				} else {
					Node tempChild = temp.getChild();
					while (tempChild != null) {
						String fstr = " " + temp.getSeq() + " ";
						int index = fstr.indexOf(" " + tempChild.getName()
								+ " ");
						if (index != -1) {
							String substr = fstr.substring(
									index + tempChild.getName().length() + 1)
									.trim();
							tempChild.setSeq(substr);
							quene.add(tempChild);
						}
						tempChild = tempChild.getSibling();
					}
				}
			}

			if (curChildNum > maxChileNum) {
				maxChileNum = curChildNum;
				maxCandidateList = new ArrayList<String>();
				maxCandidateList.add(candidate);
			} else if (curChildNum == maxChileNum) {
				maxCandidateList.add(candidate);
			}
		}
		if (maxCandidateList.size() == 1)
			return maxCandidateList.get(0);
		else {
			int maxItemNum = 0;
			String maxStr = null;
			for (String maxCandidate : maxCandidateList) {
				int curItemNum = getCandidateItemNum(maxCandidate);
				if (curItemNum > maxItemNum) {
					maxItemNum = curItemNum;
					maxStr = maxCandidate;
				}
			}
			return maxStr;
		}
	}

	private int getCandidateItemNum(String candidate) {
		String[] items = candidate.split("\\s+");
		HashSet<String> itemSet = new HashSet<String>(Arrays.asList(items));
		return itemSet.size();
	}
	

	private void updatePreKLevelMap(HashMap<String, Node> preKLevelMap,
			List<Node> preKLevelNode, List<Node> list, String item) {
		for (Node node : list) {
			String str = node.getTotleName();
			Node child = node.getChild();
			if (child != null && child.getName().equals(item))
				node.setChild(child.getSibling());
			else {
				while (child.getSibling() != null) {
					if (child.getSibling().getName().equals(item)) {
						child.setSibling(child.getSibling().getSibling());
						break;
					}
					child = child.getSibling();
				}
			}
			if (node.getChild() != null)
				preKLevelMap.put(str, node);
			else {
				preKLevelNode.remove(node);
				preKLevelMap.remove(str);
			}
		}
	}

	private List<Node> getAllContainedSeq(String result,
			HashMap<String, Node> preKLevelMap) {
		List<Node> resultList = new ArrayList<Node>();
		for (String str : preKLevelMap.keySet())
			if (StrUtil.strContain(result, str, " ")) {
				resultList.add(preKLevelMap.get(str));
			}
		return resultList;
	}

	/**
	 * generate all contained preKLevelNode
	 * 
	 * @param node
	 * @param row
	 * @param preKLevelNode
	 * @return
	 */
	private Node generateSubTree(Node node, String row,
			List<Node> preKLevelNode, int k) {
		List<Node> quene1 = new ArrayList<Node>();
		List<Node> quene2 = new ArrayList<Node>();
		Node result = new Node(node);
		result.setChild(null);
		result.setSibling(null);
		node.setSeq(row);
		quene1.add(node);
		quene2.add(result);
		while (quene1.size() > 0) {
			Node t1 = quene1.remove(0);
			Node t2 = quene2.remove(0);
			Node child = t1.getChild();
			while (child != null) {
				String f_str = " " + t1.getSeq() + " ";
				int index = f_str.indexOf(" " + child.getName() + " ");
				if (index != -1) {
					String subStr = f_str.substring(
							index + child.getName().length() + 1).trim();
					child.setSeq(subStr);
					Node c_temp = new Node(child);
					c_temp.setChild(null);
					c_temp.setSibling(null);
					t2.addChild(c_temp);
					quene1.add(child);
					quene2.add(c_temp);
				}
				child = child.getSibling();
			}
		}
		// get k-1 level nodes
		quene2.clear();
		quene2.add(result);
		while (quene2.size() > 0) {
			Node t = quene2.remove(0);
			Node child = t.getChild();
			while (child != null) {
				if (child.getLevel() == k - 1 && child.getChild() != null)
					preKLevelNode.add(child);
				quene2.add(child);
				child = child.getSibling();
			}
		}
		return result;
	}

	public static String shortRecord(String row, HashSet<String> itemOccured,
			int fi) {
		LinkedList<String> items = new LinkedList<String>(Arrays.asList(row
				.trim().split("\\s+")));
		items = oneShort(items, itemOccured, fi);
		items = twoShort(items, fi);
		items = threeShort(items, fi);
		String result = "";
		result = StrUtil.strAdd(result, items, 1, " ");
		return result;
	}

	private static LinkedList<String> oneShort(LinkedList<String> items,
			HashSet<String> itemOccured, int fi) {
		LinkedList<String> temp = new LinkedList<String>();
		String pre = null;
		int count = 0;
		for (String item : items) {
			if (!itemOccured.contains(item))
				continue;
			else if (pre == null || !pre.equals(item)) {
				temp.add(item);
				pre = item;
				count = 1;
			} else {
				if (count < fi)
					temp.add(item);
				++count;
			}
		}
		return temp;
	}

	private static LinkedList<String> twoShort(LinkedList<String> items, int fi) {
		LinkedList<String> temp = new LinkedList<String>();
		ArrayList<String> t = new ArrayList<String>();
		int count = 0;
		int index = 0;
		for (String item : items) {
			if (t.size() == 0) {
				temp.add(item);
				t.add(item);
			} else if (t.size() == 1) {
				if (!t.get(0).equals(item)) {
					t.add(item);
					count = 1;
				}
				temp.add(item);
			} else {
				if (t.get(index).equals(item)) {

						temp.add(item);
						++index;
						if (index == 2) {
							++count;
							if( count > fi ){
								temp.removeLast();
								temp.removeLast();
							}
							index = 0;
						}
				} else {
					temp.add(item);
					t.remove(0);
					t.add(item);
					index = 0;
					count = 1;
				}
			}
		}
		return temp;
	}

	private static LinkedList<String> threeShort(LinkedList<String> items,
			int fi) {
		LinkedList<String> temp = new LinkedList<String>();
		ArrayList<String> t = new ArrayList<String>();
		int count = 0;
		int index = 0;
		for (String item : items) {
			if (t.size() <= 1) {
				temp.add(item);
				t.add(item);
			} else if (t.size() == 2) {
				if (!(t.get(0).equals(item) && t.get(1).equals(item))) {
					t.add(item);
					count = 1;
				}
				temp.add(item);
			} else {
				if (t.get(index).equals(item)) {
						temp.add(item);
						++index;
						if (index == 3) {
							++count;
							if( count > fi ){
								temp.removeLast();
								temp.removeLast();
								temp.removeLast();
							}
							index = 0;
						}
				} else {
					temp.add(item);
					t.remove(0);
					t.add(item);
					index = 0;
					count = 1;
				}
			}
		}
		return temp;
	}
}
