package de.schlund.pfixxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pustefixframework.util.javascript.JSUtils;

public class IncludeSizeParser {
	
	private final static Pattern START_PATTERN = Pattern.compile("^\\s*<span class=\"pfx_inc_start\"/>");
	private final static Pattern END_PATTERN = Pattern.compile("^\\s*<span class=\"pfx_inc_end pfx_inc_ro\" title=\"([^\"]*)\"/>");
	private final static Pattern TITLE_PATTERN = Pattern.compile("\\{pfx:getDynIncInfo\\('([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)','([^']*)'\\)\\}.*");
	
	private static Stack<Include> includeStack = new Stack<Include>();
	private static List<Include> includeList = new ArrayList<Include>();
	private static List<Include> topLevelIncludes = new ArrayList<Include>();
	
	public static IncludeStatistics parse(File file) throws IOException {
		
		FileInputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		IncludeStatistics statistics = new IncludeStatistics(file.getName(), (int)file.length());
		statistics.systemId = file.toURI().toString();
		int bytes = 0;
		String line = null;
        while((line = reader.readLine()) != null) {
        	bytes += line.getBytes("utf8").length;
			Matcher matcher = START_PATTERN.matcher(line);
			if(matcher.matches()) {
				Include include = new Include();
				if(includeStack.isEmpty()) {
					topLevelIncludes.add(include);
					statistics.childIncludes.add(include);
				} else {
					includeStack.peek().childIncludes.add(include);
				}
				include.start = bytes;
				includeStack.add(include);
				includeList.add(include);
			} else {
				matcher = END_PATTERN.matcher(line);
				if(matcher.matches()) {
					Include include = includeStack.pop();
					include.end = bytes;
					include.title = getTitle(matcher.group(1));
				}
			}
		
        }
		
		return statistics;
		
	}
	
	private static String getTitle(String str) {
		if(str.startsWith("{")) {
			Matcher matcher = TITLE_PATTERN.matcher(str);
			if(matcher.matches()) {
				return matcher.group(1) + "|" + matcher.group(2) + "|" + matcher.group(3) + "|" + matcher.group(4);
			}
		}
		return str;
	}
	
	
	public static class IncludeStatistics extends Include {
		
		String systemId;
		long size;
		
		public IncludeStatistics(String title, long size) {
			this.title = title;
			this.size = size;
		}
		
		long getRetainedSize() {
			return size;
		}
		
		public String getJSON(SortBy sortBy) {
			StringBuilder jsonBuilder = new StringBuilder();
			IncludeComparator comp = null;
			if(sortBy != null) {
				comp = new IncludeComparator(sortBy);
			}
			getJSON(this, jsonBuilder, comp);
			return jsonBuilder.toString();
		}
		
		private void getJSON(Include include, StringBuilder sb, IncludeComparator comp) {
			
			sb.append("{\"title\":\"").append(JSUtils.escape(include.title)).append("\",");
			sb.append("\"retained\":").append(include.getRetainedSize()).append(",");
			sb.append("\"shallow\":").append(include.getShallowSize()).append(",");
			if(include.childIncludes.size() > 0) {
				sb.append("\"includes\":[");
				List<Include> list;
				if(comp == null) {
					list = include.childIncludes;
				} else {
					list = new ArrayList<Include>();
					list.addAll(include.childIncludes);
					Collections.sort(list, comp);
				}
				Iterator<Include> it = list.iterator();
				while(it.hasNext()) {
					Include child = it.next();
					getJSON(child, sb, comp);
					if(it.hasNext()) {
						sb.append(",");
					}
				}
				sb.append("]");
			}
			sb.append("}");
		}
		
	}
	
	public static String formatSize(final long size) {
		float calcSize = (float)size / 1024;
		char calcUnit = 'k';
		if(calcSize >= 1024) {
			calcSize = calcSize / 1024;
			calcUnit = 'm';
		}
		DecimalFormat df =   new DecimalFormat  ( "0.#" );
		return ""+df.format(calcSize)+calcUnit;
	}
	
	private static class Include {
		
		List<Include> childIncludes = new ArrayList<Include>();
		
		int start;
		int end;
		String title;
		
		/**
		 * 
		 * @return size of part including subparts
		 */
		long getRetainedSize() {
			return end-start;
		}
		
		/**
		 * 
		 * @return size of part excluding subparts
		 */
		long getShallowSize() {
			long childSize = 0;
			for(Include childInclude: childIncludes) {
				childSize += childInclude.getRetainedSize();
			}
			return getRetainedSize() - childSize;
		}
		
		List<Include> getChildIncludes() {
			return childIncludes;
		}
		
		int getPartCount() {
			int partCount = childIncludes.size();
			System.out.println(partCount);
			for(Include childInclude: childIncludes) {
				partCount += childInclude.getPartCount();
			}
			return partCount;
		}
		
	}
	
	public static enum SortBy { TITLE, RETAINED, SHALLOW };
	
	static class IncludeComparator implements Comparator<Include> {
		
		SortBy sortBy;
		
		IncludeComparator(SortBy sortBy) {
			this.sortBy = sortBy;
		}
		
		public int compare(Include o1, Include o2) {
			if(sortBy == null || sortBy == SortBy.TITLE) {
				return o1.title.compareTo(o2.title);
			} else {
				long s1 = 0;
				long s2 = 0;
				if(sortBy == SortBy.RETAINED) {
					s1 = o1.getRetainedSize();
					s2 = o2.getRetainedSize();
				} else if(sortBy == SortBy.SHALLOW) {
					s1 = o1.getShallowSize();
					s2 = o2.getShallowSize();
				}
				if(s1 > s2) {
					return -1;
				} else if(s1 < s2) {
					return 1;
				}
				return 0;
			}
		}
	}
	
	static class EntryCounter {
	    
	    private String name;
	    private int total;
	    private int max;
	    private Map<String, Value> map = new HashMap<String, Value>();
	    
	    public EntryCounter(String name) {
	        this.name = name;
	    }
	    
	    public String getName() {
	        return name;
	    }
	    
	    public void increment(String key) {
	        if(key != null) {
	            Value value = map.get(key);
	            if(value == null) {
	                value = new Value();
	                value.key = key;
	                map.put(key, value);
	            }
	            value.no++;
	            if(value.no > max) {
	                max = value.no;
	            }
	            total++;
	        }
	    }
	    
	    public List<Value> getTop(int size) {
	        List<Value> topList = new ArrayList<Value>();
	        for(Value value: map.values()) {
	            if(topList.isEmpty()) {
	                topList.add(value);
	            } else {
	                int ind;
	                for(ind=topList.size()-1; ind>-1; ind--) {
	                    Value refValue = topList.get(ind);
	                    if(value.no <= refValue.no) {
	                        break;
	                    }
	                }
	                ind++;
	                if(ind < size) {
	                    topList.add(ind, value);
	                    if(topList.size() > size) {
	                        topList.remove(topList.size()-1);
	                    }
	                }
	            }
	        }
	        return topList;
	    }
	    
	    public int getTotal() {
	        return total;
	    }
	    
	    public int getMax() {
	        return max;
	    }
	    
	}
	    
	static class Value {
		
		public String key;
		public int no;
	    
	}

}
