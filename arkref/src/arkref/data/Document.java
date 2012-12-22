package arkref.data;

import java.util.*;
import java.io.*;

import com.aliasi.util.Math;

import arkref.analysis.ARKref;
import arkref.analysis.FindMentions;
import arkref.analysis.Preprocess;
import arkref.parsestuff.AlignedSub;
import arkref.parsestuff.AnalysisUtilities;
import arkref.parsestuff.TregexPatternFactory;
import arkref.parsestuff.U;
import arkref.sent.SentenceBreaker;


import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.StringUtils;

public class Document implements Serializable{
	private static final long serialVersionUID = 55739275200700333L;
	private ArrayList<Sentence> sentences;
	private ArrayList<Mention> mentions;
	public NodeHashMap<Mention> node2mention;
	private RefGraph refGraph;
	private Tree docTree = null; //tree that includes all the trees for the sentences, in order, under a dummy node	
	private EntityGraph entGraph;


	public Document() {
		sentences = new ArrayList<Sentence>();
		mentions = new ArrayList<Mention>();
		node2mention = new NodeHashMap<Mention>();
		refGraph = new RefGraph();
	}

	
	public Document(List<Tree> trees, List<String> entityStrings) {
		sentences = new ArrayList<Sentence>();
		mentions = new ArrayList<Mention>();
		node2mention = new NodeHashMap<Mention>();
		refGraph = new RefGraph();
		
		for(int i=0; i<trees.size(); i++){
			Sentence sent = new Sentence(i);
			Tree t = trees.get(i);
			String entityString = entityStrings.get(i);
			boolean parseSuccess = !t.getChild(0).label().toString().equals(".");
			sent.setStuff(t, entityString, parseSuccess);
			sentences.add(sent);
		}
	}
	
	
	/**
	 * NOT USED
	 * 
	 * if there is no mention for the given node, this will walk up the tree 
	 * to try to find one, as in H&K EMNLP 09.   Such a method is necessary
	 * because the test data coref labels may not match up with constituents exactly
	 * 
	 * @param s
	 * @param node
	 * @return
	 */
	public Mention findMentionDominatingNode(int sentenceIndex, Tree node) {
		Mention res = null;
		Tree tmpNode = node;
		
		if (sentenceIndex >= sentences.size()){
			return null;
		}
		
		Sentence s = sentences.get(sentenceIndex);
		
		do {
			res = node2mention.get(s, tmpNode);
			tmpNode = tmpNode.parent(s.rootNode());
		} while(res == null && tmpNode != null);
			
		return res;
	}
	
	
	/**
	 * Given a span defined by indexes for the sentence, start token, and end token,
	 * this method returns the smallest node that includes that span. 
	 * 
	 * @param sentenceIndex
	 * @param spanStart inclusive
	 * @param spanEnd inclusive
	 * @return
	 */
	public Tree findNodeThatCoversSpan(int sentenceIndex, int spanStart, int spanEnd){
		if(sentenceIndex >= sentences.size()) {
			return null;
		}
		Sentence sent = sentences.get(sentenceIndex);
		return findNodeThatCoversSpan(sent, spanStart, spanEnd);
	}
	
	public Tree findNodeThatCoversSpan(Sentence sent, int spanStart, int spanEnd) {
		List<Tree> leaves = sent.rootNode().getLeaves();
		if(spanStart < 0 || leaves.size() == 0 || spanEnd >= leaves.size()) {
			return null;
		}

		Tree startLeaf = leaves.get(spanStart);
		Tree endLeaf = leaves.get(spanEnd);
		return findNodeThatCoversSpan(sent, startLeaf, endLeaf);
	}
	public Tree findNodeThatCoversSpan(Sentence sent, Tree startLeaf, Tree endLeaf) {
		Tree cur = startLeaf;
		while(cur != null) {
			if (cur.dominates(startLeaf) && cur.dominates(endLeaf))
				return cur;
			cur = cur.parent(sent.rootNode());
		}
		assert false : "got to top without finding covering span";
		return cur;
	}
	public Tree getLeaf(int sentenceIndex, int leafIndex) {
		Sentence sent = sentences.get(sentenceIndex);
		List<Tree> leaves = sent.rootNode().getLeaves();
		return leaves.get(leafIndex);
	}


	public static Document loadFiles(String path) throws IOException {
		Document d = new Document();

		String shortpath = Preprocess.shortPath(path);

		String parseFilename = shortpath + ".parse";
		String neFilename = path = shortpath + ".sst";
		BufferedReader parseR = new BufferedReader(new FileReader(parseFilename));
		BufferedReader sstR = new BufferedReader(new FileReader(neFilename));
		
		String parseLine, sst;
		int curSentId = 0;
		while ( (parseLine = parseR.readLine()) != null) {
			Sentence sent = new Sentence(++curSentId);
			
			parseLine = parseLine.replace("=H ", " ");
			Tree tree = null;
			if (parseLine.split("\t").length == 1) {
				// old version: just the parse
				tree = AnalysisUtilities.getInstance().readTreeFromString(parseLine);
				sent.hasParse = true;
			} else {
				tree = AnalysisUtilities.getInstance().readTreeFromString(parseLine.split("\t")[2]);
				sent.hasParse = !parseLine.split("\t")[0].equals("ERROR");
			}
			
			Document.addNPsAbovePossessivePronouns(tree);
			Document.addInternalNPStructureForRoleAppositives(tree);

			sst = sstR.readLine();
			sent.setStuff(tree, sst, sent.hasParse);
			d.sentences.add(sent);
		}
		return d;
	}
	
	/** do sentence breaking (again) on the .txt file for surface info, after parses etc. have been loaded 
	 * @throws FileNotFoundException **/
	public void loadSurfaceSentences(String path) throws FileNotFoundException {
		if (! new File(path+".txt").exists()) {
			throw new FileNotFoundException("Need the .txt file to re-break");
		}
		int i=0;
		for (SentenceBreaker.Sentence s : AnalysisUtilities.cleanAndBreakSentences(U.readFile(path+".txt"))) {
			sentences.get(i).surfSent = s;
			i++;
		}
		
	}
	
	public void ensureSurfaceSentenceLoad(String path) throws FileNotFoundException {
		if (sentences.size()>0 && sentences.get(0).surfSent == null) {
			loadSurfaceSentences(path);
		}
	}
	
	public Sentence getSentenceContaining(int charOffset) {
		for (Sentence s : sentences) {
			if (s.surfSent.charStart <= charOffset  &&  charOffset < s.surfSent.charEnd) {
				return s;
			}
		}
		assert false : "no sentence for char offset "+charOffset;
		return null;
	}


	public static void addNPsAbovePossessivePronouns(Tree tree) {
		TreeFactory factory = new LabeledScoredTreeFactory(); //TODO might want to keep this around to save time
		String patS = "NP=parentnp < /^PRP\\$/=pro"; //needs to be the maximum projection of a head word
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		TregexMatcher matcher = pat.matcher(tree);
		while (matcher.find()) {
			Tree parentNP = matcher.getNode("parentnp");
			Tree pro = matcher.getNode("pro");
			Tree newNP = factory.newTreeNode("NP", new ArrayList<Tree>());
			int index = parentNP.indexOf(pro);

			newNP.addChild(pro);
			parentNP.removeChild(index);
			parentNP.addChild(index, newNP);

		}
	}


	public static void addInternalNPStructureForRoleAppositives(Tree tree) {
		TreeFactory factory = new LabeledScoredTreeFactory(); //TODO might want to keep this around to save time
		String patS = "NP=parentnp < (NN|NNS=role . NNP|NNPS)";
		TregexPattern pat = TregexPatternFactory.getPattern(patS);
		TregexMatcher matcher = pat.matcher(tree);
		Tree newNode;

		while (matcher.find()) {
			Tree parentNP = matcher.getNode("parentnp");
			Tree roleNP = matcher.getNode("role");
			Tree tmpTree;
			
			newNode = factory.newTreeNode("NP", new ArrayList<Tree>());
			int i = parentNP.indexOf(roleNP);
			while(i>=0){
				tmpTree = parentNP.getChild(i);
				if(!tmpTree.label().value().matches("^NN|NNS|DT|JJ|ADVP$")){
					break;
				}
				newNode.addChild(0, tmpTree);
				parentNP.removeChild(i);
				i--;
			}
			
			parentNP.addChild(i+1, newNode);
			
		}
	}



	/** goes backwards through document **/
	public Iterable<Mention> prevMentions(final Mention start) {
		return new Iterable<Mention>() {
			public Iterator<Mention> iterator() {
				return new MentionRevIterIter(start);
			}
		};
	}
	public class MentionRevIterIter implements Iterator<Mention> {
		int mi = -1;
		int startingSentence = -1;
		public MentionRevIterIter(Mention start) {
			startingSentence = start.getSentence().ID();
			for (int i=0; i < mentions.size(); i++) {
				if (mentions.get(i) == start) {
					this.mi = i;
					break;
				}
			} 
			assert mi != -1;
		}

		@Override
		public boolean hasNext() {
			if (mi==0)
				return false;
			Mention mNext = mentions.get(mi-1);
			if (startingSentence - mNext.getSentence().ID() > ARKref.Opts.sentenceWindow)
				return false;
			return true;
		}

		@Override
		public Mention next() {
			mi--;
			assert mi != -1;
			Mention m = mentions.get(mi);
			return m;

		}
		@Override
		public void remove() {
			throw new RuntimeException("can't remove from the mention iterator!");
		}

	}


	/**
	 * make a right branching tree out of all the sentence trees
	 * e.g., (DOCROOT T1 (DOCROOT T2 (DOCROOT T3))) 
	 * This will make sure that ndoes in t3 are further from nodes in t1 
	 * than they are fromnodes in t2.
	 * 
	 * @return
	 */
	public Tree getTree() {
		if(docTree == null){
			TreeFactory factory = new LabeledScoredTreeFactory();
			docTree = factory.newTreeNode("DOCROOT", new ArrayList<Tree>());
			Tree tmpTree1 = docTree;
			Tree tmpTree2;
			for(int i=0; i<sentences.size(); i++){
				tmpTree1.addChild(sentences.get(i).rootNode());
				if(i<sentences.size()-1){ 
					tmpTree2 = factory.newTreeNode("DOCROOT", new ArrayList<Tree>());
					tmpTree1.addChild(tmpTree2);
					tmpTree1 = tmpTree2;
				}
			}

		}
		return docTree;
	}
	
	/** 
	 * saves doc-level token alignments in the analysis.Word objects
	 * Requires surfSent's in the document's sentences.
	 **/
	public void doTokenAlignments(String docText) {
		U.pl("*** Stanford <-> Raw Text alignment ***\n");
		for (Sentence s : sentences) {
			U.pf("S%-2d\t%s\n", s.ID(), StringUtils.join(s.tokens()));
//			U.pl("SENTENCE WORDS     " + s.words);
//			U.pl("" + s.surfSent);
//			U.pl("" + s.surfSent.rawText);
			AlignedSub cleanText = AnalysisUtilities.moreCleanup(s.surfSent.rawText); 
			int[] wordAlignsInSent = AnalysisUtilities.alignTokens(cleanText.text, s.words);
			for (int i=0; i<wordAlignsInSent.length; i++)
				if (wordAlignsInSent[i] != -1)
					wordAlignsInSent[i] = cleanText.alignments[wordAlignsInSent[i]];
			// adjust to doc position
			for (int i=0; i < s.words.size(); i++) {
				if (wordAlignsInSent[i]==-1) {
					s.words.get(i).charStart = -1;
				} else {
					s.words.get(i).charStart = s.surfSent.alignments[ wordAlignsInSent[i] ];
				}
			}
			if (s.words != null && s.words.size()>0 && s.words.get(0).charStart==-1) {
				s.words.get(0).charStart = s.surfSent.alignments[0];
			}
			for (int i=1; i < s.words.size(); i++) {
				if (s.words.get(i).charStart==-1) {
					Word prev = s.words.get(i-1);
					s.words.get(i).charStart = prev.charStart + prev.token.length();	
				}
				
			}
				
		}
	}
	
	public List<Word> allWords() {
		List<Word> allWords = new ArrayList<Word>();
		for (Sentence s : sentences) {
			for (Word w : s.words){ 
				allWords.add(w);
			}
		}
		return allWords;
	}

	public List<Mention> mentions() {
		return mentions;
	}

	public List<Sentence> sentences() {
		return sentences;
	}

	public RefGraph refGraph() {
		return refGraph;
	}

	public void setEntGraph(EntityGraph entGraph) {
		this.entGraph = entGraph;
	}

	public EntityGraph entGraph() {
		return entGraph;
	}

	public Mention newMention(Sentence s, Tree subtree) {
		Mention mention = new Mention(mentions.size()+1, s, subtree);
		mentions.add(mention);
		if (subtree != null)
			node2mention.put(s, subtree, mention);
		return mention;
	}

}
