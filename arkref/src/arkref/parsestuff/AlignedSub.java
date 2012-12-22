package arkref.parsestuff;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;

/**
 * Do a regex substitution on a string and remember its alignments to the original.
 * 
 * This class represents a string ready to have such a computation, as well as its result.
 * The result can have further operations done to it and it maintains references
 * to the original string.  Thus chaining is supported.
 * 
 * Alignments are represented as an integer array parallel to the new string, each one
 * being the position in the original.  This is only really true for fragments of the string
 * which were present in the original.  New fragments from the substitution are all assigned
 * to be the original starting position.
 * 
 * Capturing groups are not supported in the replacement.
 * 
 * Example:
 * 
 * AlignedSub cleanText = new AlignedSub(text).replaceAll("<\\S+>","");
 * AlignedSub better = cleanText.replaceAll("\\s+"," ").replaceAll(":)","[HAPPY]");
 * 
 * Also run main() for more examples.
 * 
 * @author Brendan O'Connor (http://anyall.org)
 *
 */
public class AlignedSub {
	public String text;
	/** parallel to 'text'. **/
	public int[] alignments = null;
	
	public AlignedSub(String s) {
		text = s;
	}
	public static AlignedSub selfAligned(String text) {
		// has identity alignment
		List <Integer> alignments = new ArrayList<Integer>();
		AlignedSub as = new AlignedSub(text);
		for (int i=0; i<text.length(); i++) alignments.add(i);
		as.alignments = convert(alignments);
		return as;
	}
	public String replace(CharSequence target, CharSequence replacement)  {
		throw new NotImplementedException();
	}
	public AlignedSub replaceAll(String regex, String replacement) {
		Pattern p = Pattern.compile(regex);
		return replaceAll(p, replacement);
	}
	public AlignedSub replaceAll(Pattern pattern, String replacement) {
		AlignedSub as = replace(this.text, pattern, replacement, false);
		if (this.alignments != null)
			as.alignments = project(as.alignments, this.alignments);
		return as;
	}
	public AlignedSub replaceFirst(String regex, String replacement) {
		throw new NotImplementedException();
	}
	
	private static AlignedSub replace(String text, Pattern pattern, String replacement, boolean justOne) {
		Matcher m = pattern.matcher(text);
		boolean result = m.find();
		if (!result) {
			return selfAligned(text);
		}
		List <Integer> alignments = new ArrayList<Integer>();
        StringBuffer sb = new StringBuffer();
        
        int i = 0;
        do {
        	sb.append( text.substring(i, m.start()) );
        	if (i < m.start()) {
        		do { alignments.add(i); } while (++i < m.start());
        	}
        	sb.append(replacement);
        	for (int j=0; j<replacement.length(); j++) {
        		alignments.add(m.start());
        	}
        	i = m.end();
        	
        	if (justOne) break;
        	
        	result = m.find();
        } while (result);
        
        sb.append( text.substring(i, text.length()));
        do { alignments.add(i); } while (++i < text.length());
        
        AlignedSub as = new AlignedSub(sb.toString());
        as.alignments = convert(alignments);
        return as;
	}
	/** 
	 * pipe x through map.  output is parallel to x.  
	 * if map indices don't span the full range of x values, an error will happen.
	 * this is equivalent to R/Matlab/Python:  map[x]
	 **/
	public static int[] project(int[] x, int[] map) {
		int[] ret = new int[x.length];
		for (int i=0; i < ret.length; i++) {
			ret[i] = map[x[i]];
		}
		return ret;
	}
	private static int[] convert(List<Integer> list) {
		// true magic
		return ArrayUtils.toPrimitive((Integer []) list.toArray(new Integer[0]));
	}
	
	
	public String toString() {
		String s = "";
		for (int i=0; i < text.length(); i++) {
			s += String.format("%-2s", text.substring(i,i+1));
		}
		s += "\n";
		for (int i=0; i < text.length(); i++) {
			s += String.format("%-2d", alignments[i]);
		}
		return s;
	}
	
	public static void main(String[] args) {
		AlignedSub s, s2;
		s = selfAligned("hello world");
		s2 = s.replaceAll("wor", "ZZ");
		U.pl(s);
		U.pl(s2);
		s2 = s.replaceAll("w", "ABCDEFG");
		U.pl(s2);
		s2 = s.replaceAll("12345","");
		U.pl(s2);
		s2 = s.replaceAll("llo","");
		U.pl(s2);
		U.pl(s.replaceAll("h","H"));
		s2 = s.replaceAll("h","H").replaceAll("e","E");
		U.pl(s2);
		s2 = s.replaceAll("hell","WOW").replaceAll("OWo","=======");
		U.pl(s2);
	}
	
	
}
