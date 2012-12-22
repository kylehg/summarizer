package arkref.parsestuff;

import edu.stanford.nlp.trees.tregex.*;
import java.util.*;

public class TregexPatternFactory {
	protected TregexPatternFactory(){
		map = new HashMap<String, TregexPattern>();
	}
	
	public static TregexPattern getPattern(String tregex){
		if(instance == null){
			instance = new TregexPatternFactory();
		}
		Map<String, TregexPattern> myMap = instance.getMap();
		TregexPattern pattern = myMap.get(tregex);
		if(pattern == null){
			try{
				pattern = TregexPattern.compile(tregex);
				myMap.put(tregex, pattern);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return pattern;
	}
	
	private Map<String, TregexPattern> getMap(){
		return map;
	}
	
	private static TregexPatternFactory instance;
	private Map<String, TregexPattern> map;
}
