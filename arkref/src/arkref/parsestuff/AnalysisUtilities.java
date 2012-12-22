package arkref.parsestuff;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;

import arkref.analysis.ARKref;
import arkref.sent.SentenceBreaker;

import com.aliasi.util.Strings;



//import net.didion.jwnl.data.POS;
//import net.didion.jwnl.dictionary.Dictionary;

import edu.cmu.ark.DiscriminativeTagger;
import edu.cmu.ark.LabeledSentence;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.*;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;

//import net.didion.jwnl.*;

/** Various NL analysis utilities, including ones wrapping Stanford subsystems and other misc stuff **/
public class AnalysisUtilities {
	public static boolean DEBUG = true;
	private AnalysisUtilities(){
		parser = null;
		sst = null;
		dp = new DocumentPreprocessor(false);
		
		
//		try{
//			JWNL.initialize(new FileInputStream(properties.getProperty("jwnlPropertiesFile", "config/file_properties.xml")));
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		conjugator = new VerbConjugator();
//		conjugator.load(properties.getProperty("verbConjugationsFile", "verbConjugations.txt"));
		headfinder = new CollinsHeadFinder();
		tree_factory = new LabeledScoredTreeFactory();
		tlp = new PennTreebankLanguagePack();
	}
	
	
	protected static String preprocess(String sentence) {
		sentence = sentence.trim();
		if(!sentence.matches(".*\\.['\"]*$")){//charAt(sentence.length()-1) != '.'){
			sentence += ".";
		}
		
		sentence = sentence.replaceAll("can't", "can not");
		sentence = sentence.replaceAll("won't", "will not");
		sentence = sentence.replaceAll("n't", " not"); //aren't shouldn't don't isn't
		
		return sentence;
	}
	
	
	protected static String preprocessTreeString(String sentence) {
		sentence = sentence.replaceAll(" n't", " not");
		sentence = sentence.replaceAll("\\(MD ca\\)", "(MD can)");
		sentence = sentence.replaceAll("\\(MD wo\\)", "(MD will)");
		sentence = sentence.replaceAll("\\(MD 'd\\)", "(MD would)");
		sentence = sentence.replaceAll("\\(VBD 'd\\)", "(VBD had)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBZ 's\\)", "(VBZ is)");
		sentence = sentence.replaceAll("\\(VBP 're\\)", "(VBP are)");
		
		return sentence;
	}
	
	
	public static int[] alignTokens(String rawText, List<arkref.data.Word> words) {
		String[] tokens = new String[words.size()];
		for (int i=0; i < words.size(); i++) {
			tokens[i] = words.get(i).token;
		}
		return alignTokens(rawText, tokens);
	}
	public static int[] alignTokens(String rawText, String[] tokens) {
		int MAX_ALIGNMENT_SKIP = 100;
		int[] alignments = new int[tokens.length];
		int curPos = 0;
		
		tok_loop:
		
		for (int i=0; i < tokens.length; i++) {
			String tok = tokens[i];
//			U.pf("TOKEN [%s]  :  ", tok);
			for (int j=0; j < MAX_ALIGNMENT_SKIP; j++) {
				boolean directMatch  = rawText.regionMatches(curPos + j, tok, 0, tok.length());
				if (!directMatch)
					directMatch = rawText.toLowerCase().regionMatches(curPos + j, tok.toLowerCase(), 0, tok.length());
				boolean alternateMatch = false;
				if (!directMatch) {
					int roughLast = curPos+j+tok.length()*2+10;
					String substr = StringUtils.substring(rawText, curPos+j, roughLast);
					Matcher m = tokenSurfaceMatches(tok).matcher(substr);
//					U.pl("PATTERN "+ tokenSurfaceMatches(tok));
					alternateMatch = m.find() && m.start()==0;
				}
				
//				U.pl("MATCHES "+ directMatch + " " + alternateMatch);
				if (directMatch || alternateMatch) {
					alignments[i] = curPos+j;
					if (directMatch)
						curPos = curPos+j+tok.length();
					else
						curPos = curPos+j+1;
//					U.pf("\n  Aligned to pos=%d : [%s]\n", alignments[i], U.backslashEscape(StringUtils.substring(rawText, alignments[i], alignments[i]+10)));
					continue tok_loop;
				}
//				U.pf("%s", U.backslashEscape(StringUtils.substring(rawText,curPos+j,curPos+j+1)));
			}
			U.pf("FAILED MATCH for token [%s]\n", tok);
			U.pl("sentence: "+rawText);
			U.pl("tokens: " + StringUtils.join(tokens," "));
			alignments[i] = -1;
		}
		// TODO backoff for gaps .. at least guess the 2nd gap position or something (2nd char after previous token ends...)
		return alignments;
	}
	
	/** undo penn-treebankification of tokens.  want to match raw original form if possible. **/
	public static Pattern tokenSurfaceMatches(String tok) {
		if (tok.equals("-LRB-")) {
			return Pattern.compile("[(\\[]");
		} else if (tok.equals("-RRB-")) {
			return Pattern.compile("[)\\]]");
		} else if (tok.equals("``")) {
			return Pattern.compile("(\"|``)");
		} else if (tok.equals("''")) {
			return Pattern.compile("(\"|'')");
		} else if (tok.equals("`")) {
			return Pattern.compile("('|`)");
		}
		return Pattern.compile(Pattern.quote(tok));
	}
	
	public String[] stanfordTokenize(String str) {
		List<Word> wordToks = AnalysisUtilities.getInstance().dp.getWordsFromString(str);
		String[] tokens = new String[wordToks.size()];
		for (int i=0; i < wordToks.size(); i++)
			tokens[i] = wordToks.get(i).value();
		return tokens;
	}
	
	public static List <SentenceBreaker.Sentence> cleanAndBreakSentences(String docText) {
		// ACE IS EVIL
		docText = docText.replaceAll("<\\S+>", "");
		AlignedSub cleaner = AnalysisUtilities.cleanupDocument(docText);
		List<SentenceBreaker.Sentence> sentences = SentenceBreaker.getSentences(cleaner);
		return sentences;
	}
	public static List <String> cleanAndBreakSentencesToText(String docText) {
		List <String> sentenceTexts = new ArrayList<String>();
		for (SentenceBreaker.Sentence s : cleanAndBreakSentences(docText))
			sentenceTexts.add( s.cleanText );
		return sentenceTexts;
	}
	
	/** uses stanford library for document cleaning and sentence breaking **/
	public List<String> getSentencesStanford(String document) {
		List<String> res = new ArrayList<String>();
		String sentence;
		StringReader reader = new StringReader(cleanupDocument(document).text);
		
		List<List<? extends HasWord>> sentences = new ArrayList<List<? extends HasWord>>();
		Iterator<List<? extends HasWord>> iter1 ;
		Iterator<? extends HasWord> iter2;
		
		try{
			sentences = dp.getSentencesFromText(reader);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		iter1 = sentences.iterator();
		while(iter1.hasNext()){
			iter2 = iter1.next().iterator();
			sentence = "";
			while(iter2.hasNext()){
				String tmp = iter2.next().word().toString();
				sentence += tmp;
				if(iter2.hasNext()){
					sentence += " ";
				}
			}
			res.add(sentence);
		}
		
		return res;
	}
	
	static Pattern leadingWhitespace = Pattern.compile("^\\s+");
	/** some ACE docs have weird markup in them that serve as paragraph-ish markers **/
	public static AlignedSub cleanupDocument(String document) {
		AlignedSub ret = new AlignedSub(document);
		ret = ret.replaceAll("<\\S+>", "");
		ret = ret.replaceAll(leadingWhitespace, ""); // sentence breaker char offset correctness sensitive to this
		return ret;
	}
	public static AlignedSub moreCleanup(String str) {
		AlignedSub ret = new AlignedSub(str);
		ret = ret.replaceAll("&(amp|AMP);", "&");
		ret = ret.replaceAll("&(lt|LT);", "<");
		ret = ret.replaceAll("&(gt|GT);", ">");
		return ret;
	}
	
	
//	public VerbConjugator getConjugator(){
//		return conjugator;
//	}
	
	
	public CollinsHeadFinder getHeadFinder(){
		return headfinder;
	}
	
	
	public static AnalysisUtilities getInstance(){
		if(instance == null){
			instance = new AnalysisUtilities();
		}
		return instance;
	}
	
	public double getLastParseScore(){
		return lastParseScore;
	}
	
	public Double getLastParseScoreNormalizedByLength() {
		double length = lastParse.yield().length();
		double res = lastParseScore;
		if(length <= 0){
			res = 0.0;
		}else{
			res /= length;
		}
		return res;
	}
	
	public static class ParseResult {
		public boolean success;
		public Tree parse;
		public double score;
		public ParseResult(boolean s, Tree p, double sc) { success=s; parse=p; score=sc; }
	}
	
	public ParseResult parseSentence(String sentence) {
		String result = "";
		
		//see if a parser socket server is available
        int port = new Integer(ARKref.getProperties().getProperty("parserServerPort","5556"));
        String host = "127.0.0.1";
        Socket client;
        PrintWriter pw;
        BufferedReader br;
        String line;
		try{
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pw.println(sentence);
			pw.flush(); //flush to complete the transmission
            while((line = br.readLine())!= null){
                //if(!line.matches(".*\\S.*")){
                //        System.out.println();
                //}
                if(br.ready()){
                	line = line.replaceAll("\n", "");
                    line = line.replaceAll("\\s+", " ");
                	result += line + " ";
                }else{
                	lastParseScore = new Double(line);
                }
            }

			br.close();
			pw.close();
			client.close();
			
			System.err.println("parser output:"+ result);
			
			lastParse = readTreeFromString(result);
			boolean success = !Strings.normalizeWhitespace(result).equals("(ROOT (. .))");
			return new ParseResult(success, lastParse, lastParseScore);
		} catch (Exception ex) {
			
			//ex.printStackTrace();
		}
        
		//if socket server not available, then use a local parser object
		if (parser == null) {
			if(DEBUG) System.err.println("Could not connect to parser server.  Loading parser...");
			try {
				Options op = new Options();
				String serializedInputFileOrUrl = ARKref.getProperties().getProperty("parserGrammarFile", "lib/englishPCFG.ser.gz");
				parser = new LexicalizedParser(serializedInputFileOrUrl, op);
				int maxLength = new Integer(ARKref.getProperties().getProperty("parserMaxLength", "40")).intValue();
				parser.setMaxLength(maxLength);
				parser.setOptionFlags("-outputFormat", "oneline");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try{
			if (parser.parse(sentence)) {
				lastParse = parser.getBestParse();
				lastParseScore = parser.getPCFGScore();
				TreePrint tp = new TreePrint("penn","",new PennTreebankLanguagePack());
				StringWriter sb = new StringWriter();
				pw = new PrintWriter(sb);
				tp.printTree(lastParse, pw);
				pw.flush();
				lastParse = readTreeFromString(sb.getBuffer().toString());
						
				return new ParseResult(true, lastParse, lastParseScore);
			}
		}catch(Exception e){
		}

		lastParse = readTreeFromString("(ROOT (. .))");
        lastParseScore = -99999.0;
        return new ParseResult(false, lastParse, lastParseScore);
	}
	
//	@SuppressWarnings("unchecked")
//	public String getLemma(Tree tensedverb){
//		if(tensedverb == null){
//			return "";
//		}
//		
//		String res = "";
//		Pattern p = Pattern.compile("\\(\\S+ ([^\\)]*)\\)");
//		Matcher m = p.matcher(tensedverb.toString());
//		m.find();
//		res = m.group(1);
//		
//		if(res.equals("is") || res.equals("are") || res.equals("were") || res.equals("was")){
//			res = "be";
//		}else{
//			try{
//				Iterator<String> iter = Dictionary.getInstance().getMorphologicalProcessor().lookupAllBaseForms(POS.VERB, res).iterator();
//				
//				int maxCount = -1;
//				int tmpCount;
//				while(iter.hasNext()){
//					String lemma = iter.next();
//					tmpCount = conjugator.getBaseFormCount(lemma);
//					//System.err.println("lemma: "+lemma + "\tcount: "+tmpCount);
//					if(tmpCount > maxCount){
//						res = lemma;
//						maxCount = tmpCount;
//					}
//				}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}		
//		
//		return res;
//	}
	
	
	

	public List<String> annotateSentenceWithSupersenses(Tree sentence) {
		List<String> result = new ArrayList<String>();
		
		int numleaves = sentence.getLeaves().size();
		if(numleaves <= 1){
			return result;
		}
		LabeledSentence labeled = generateSupersenseTaggingInput(sentence);
		
		//see if a NER socket server is available
        int port = new Integer(ARKref.getProperties().getProperty("supersenseServerPort","5557"));
        String host = "127.0.0.1";
        Socket client;
        PrintWriter pw;
        BufferedReader br;
        String line;
		try{
			client = new Socket(host, port);

			pw = new PrintWriter(client.getOutputStream());
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String inputStr = "";
			for(int i=0;i<labeled.length(); i++){
				String token = labeled.getTokens().get(i);
				String stem = labeled.getStems().get(i);
				String pos = labeled.getPOS().get(i);
				inputStr += token+"\t"+stem+"\t"+pos+"\n";
			}
			pw.println(inputStr);
			pw.flush(); //flush to complete the transmission

			while((line = br.readLine())!= null){
				String [] parts = line.split("\\t");
				result.add(parts[2]);
			}
			br.close();
			pw.close();
			client.close();
			
		} catch (Exception ex) {
			if(ARKref.Opts.debug) System.err.println("Could not connect to SST server.");
			//ex.printStackTrace();
		}
		
		//if socket server not available, then use a local NER object
		if(result.size() == 0){
			try {
				if(sst == null){
					DiscriminativeTagger.loadProperties(ARKref.getPropertiesPath());
					sst = DiscriminativeTagger.loadModel(ARKref.getProperties().getProperty("supersenseModelFile", "config/supersenseModel.ser.gz"));
				}
				sst.findBestLabelSequenceViterbi(labeled, sst.getWeights());
				for(String pred: labeled.getPredictions()){
					result.add(pred);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		//add a bunch of blanks if necessary
		while(result.size() < numleaves) result.add("0");
		
		if(ARKref.Opts.debug) System.err.println("annotateSentenceSST: "+result);
		return result;
	}
	
	
	private LabeledSentence generateSupersenseTaggingInput(Tree sentence){
		LabeledSentence res = new LabeledSentence();
		List<Tree> leaves = sentence.getLeaves();
		
		for(int i=0;i<leaves.size();i++){
			String word = leaves.get(i).label().toString();
			Tree preterm = leaves.get(i).parent(sentence);
			String pos = preterm.label().toString();
			String stem = AnalysisUtilities.getInstance().getLemma(word, pos);
			res.addToken(word, stem, pos, "0");
		}
		
		return res;
	}


	/**
	 * Remove traces and non-terminal decorations (e.g., "-SUBJ" in "NP-SUBJ") from a Penn Treebank-style tree.
	 * 
	 * @param inputTree
	 */
	public void normalizeTree(Tree inputTree){
		inputTree.label().setFromString("ROOT");

		List<Pair<TregexPattern, TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();
		List<TsurgeonPattern> ps = new ArrayList<TsurgeonPattern>();
		String tregexOpStr;
		TregexPattern matchPattern;
		TsurgeonPattern p;
		TregexMatcher matcher;
		
		tregexOpStr = "/\\-NONE\\-/=emptynode";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		ps.add(Tsurgeon.parseOperation("prune emptynode"));
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		p = Tsurgeon.collectOperations(ps);
		ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern,p));
		Tsurgeon.processPatternsOnTree(ops, inputTree);
		
		Label nonterminalLabel;
		
		tregexOpStr = "/.+\\-.+/=nonterminal < __";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(inputTree);
		while(matcher.find()){
			nonterminalLabel = matcher.getNode("nonterminal");
			if(nonterminalLabel == null) continue;
			nonterminalLabel.setFromString(tlp.basicCategory(nonterminalLabel.value()));
		}
		

	}
	
	

	
	public static String getCleanedUpYield(Tree inputTree){
		Tree copyTree = inputTree.deeperCopy();

		if(DEBUG)System.err.println(copyTree.toString());

		String res = copyTree.yield().toString();
		if(res.length() > 1){
			res = res.substring(0,1).toUpperCase() + res.substring(1);
		}

		//(ROOT (S (NP (NNP Jaguar) (NNS shares)) (VP (VBD skyrocketed) (NP (NN yesterday)) (PP (IN after) (NP (NP (NNP Mr.) (NNP Ridley) (POS 's)) (NN announcement)))) (. .)))
		
		res = res.replaceAll("\\s([\\.,!\\?\\-;:])", "$1");
		res = res.replaceAll("(\\$)\\s", "$1");
		res = res.replaceAll("can not", "cannot");
		res = res.replaceAll("\\s*-LRB-\\s*", " (");
		res = res.replaceAll("\\s*-RRB-\\s*", ") ");
		res = res.replaceAll("\\s*([\\.,?!])\\s*", "$1 ");
		res = res.replaceAll("\\s+''", "''");
		//res = res.replaceAll("\"", "");
		res = res.replaceAll("``\\s+", "``");
		res = res.replaceAll("\\-[LR]CB\\-", ""); //brackets, e.g., [sic]

		//remove extra spaces
		res = res.replaceAll("\\s\\s+", " ");
		res = res.trim();

		return res;
	}
	
	
	public Tree readTreeFromString(String parseStr){
		//read in the input into a Tree data structure
		TreeReader treeReader = new PennTreeReader(new StringReader(parseStr), tree_factory);
		Tree inputTree = null;
		try{
			inputTree = treeReader.readTree();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return inputTree;
	}
	
	protected static boolean filterSentenceByPunctuation(String sentence) {
		//return (sentence.indexOf("\"") != -1 
				//|| sentence.indexOf("''") != -1 
				//|| sentence.indexOf("``") != -1
				//|| sentence.indexOf("*") != -1);
				return (sentence.indexOf("*") != -1);
	}
	
	
	/**
	 * Sets the parse and score.
	 * For use when the input tree is given (e.g., for gold standard trees from a treebank)
	 * 
	 * @param parse
	 * @param score
	 */
	public void setLastParseAndScore(Tree parse, double score){
		lastParse = parse;
		lastParseScore = score;
	}
	
	/** 
	 * terse representation of a (sub-)tree: 
	 * NP[the white dog]   -vs-   (NP (DT the) (JJ white) (NN dog)) 
	 **/
	public static String abbrevTree(Tree tree) {
		ArrayList<String> toks = new ArrayList();
		for (Tree L : tree.getLeaves()) {
			toks.add(L.label().toString());
		}
		return tree.label().toString() + "[" + StringUtils.join(toks, " ") + "]";
	}
	


	
	private void loadWordnetMorphologyCache() {
		morphMap = new HashMap<String, Map<String, String>>();
		
		try{
			BufferedReader br;
			String buf;
			String[] parts;
			String morphFile = ARKref.getProperties().getProperty("morphFile","config/MORPH_CACHE.gz");
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(morphFile))));
			while((buf = br.readLine())!= null){
				parts = buf.split("\\t");
				addMorph(parts[1], parts[0], parts[2]);
				addMorph(parts[1], "UNKNOWN", parts[2]);
			}
			br.close();
			addMorph("men", "NNS", "man");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void addMorph(String word, String pos, String stem){
		Map<String, String> posMap = morphMap.get(pos);
		if(posMap == null){
			posMap = new HashMap<String, String>();
			morphMap.put(pos.intern(), posMap);
		}
		
		posMap.put(word.intern(), stem.intern());
	}
	
	
	public String getLemma(String word, String pos){
		if(morphMap == null){
			loadWordnetMorphologyCache();
		}
		String res = word;
		Map<String, String> posMap = morphMap.get(pos);
		if(posMap != null){
			res = posMap.get(word.toLowerCase());
			if(res == null){
				res = word.toLowerCase();
			}
		}
		return res;
	}

	private Map<String, Map<String, String>> morphMap; //pos, word -> stem
	private DiscriminativeTagger sst;
	private LexicalizedParser parser;
	private static AnalysisUtilities instance;
//	private VerbConjugator conjugator;
	private CollinsHeadFinder headfinder;
	private LabeledScoredTreeFactory tree_factory;
	private PennTreebankLanguagePack tlp;
	private double lastParseScore;
	private Tree lastParse;
	public DocumentPreprocessor dp;
}
