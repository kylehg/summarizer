package arkref.parsestuff;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class RegexUtil {
	public static class R {
		public static String or(String[] disjuncts) {
			return "(?:" + StringUtils.join(disjuncts, "|") + ")";
		}
		public static String or(Collection<String> disjuncts) {
			return or(disjuncts.toArray(new String[0]));
		}
		
		
		public static String ahead(String pat) { return "(?=" + pat + ")"; }
		public static String notAhead(String pat) { return "(?!" + pat + ")"; }
		public static String behind(String pat) { return "(?<=" + pat + ")"; }
		public static String notBehind(String pat) { return "(?<!" + pat + ")"; }
		public static String quote(String str) { return Pattern.quote(str); }
		/** non-capturing group **/
		public static String ncGroup(String pat) { return "(?:" + pat + ")"; }
		/** capturing group **/
		public static String cGroup(String pat) { return "(" + pat + ")"; } 
		public static String optional(String pat) { return ncGroup(pat) + "?"; }
		
		/** only use on greedy optional() etc. quantifiers, modify them to become reluctant **/
		public static String reluc(String pat) { return pat+"?"; }
		
		/////////////////////
		
		public static String[] quote(String[] strs) {
			String[] ret = new String[strs.length];
			for(int i=0; i<strs.length; i++)   ret[i] = quote(strs[i]);
			return ret;
		}
		
	}
	public static void main(String[] args) {
		String[] d = new String[]{ "a", "b", "c" };
		U.pl(R.or(d));
		Set<String> ds = new HashSet();
		ds.add("a"); ds.add("b"); ds.add("c");
		U.pl(R.or(ds));
	}
}
