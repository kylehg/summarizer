package arkref.ace;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import arkref.analysis.Preprocess;
import arkref.analysis.SyntacticPaths;
import arkref.data.Document;
import arkref.data.Sentence;
import arkref.data.Word;
import arkref.parsestuff.AnalysisUtilities;
import arkref.parsestuff.U;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;

/** 
 * like analysis.FindMentions except use exclusively ACE's opinions of what the mentions are
 * the tricky bits are figuring out how to reconcile ACE's mentions to our parsetree-defined mentions
 * @author brendano
 */
public class FindAceMentions {
	public static class AlignmentFailed extends Exception {
		public AlignmentFailed() { super(); }
		public AlignmentFailed(String s) { super(s); }
	}


	public static void main(String[] args) throws Exception {
		for (String path : args) {
			path = Preprocess.shortPath(path);
			U.pf("DOC\t%s\n", path);
			Document myDoc    = Document.loadFiles(path);
			myDoc.ensureSurfaceSentenceLoad(path);
			AceDocument aceDoc= AceDocument.load(path);
			go(myDoc, aceDoc);	
		}
		
		
	}
	public static void go(Document myDoc, AceDocument aceDoc) throws Exception {
		// (1) align our tokens to raw text char offsets
		// (2) calibrate ACE offsets to real text offsets
		// (3) map ACE mentions to Stanford token spans
		// (4) and map those to appropriate parse nodes
		// Issues
		//  * What about sentences that didn't parse?
		
		
		
		// Step (1)
		myDoc.doTokenAlignments(aceDoc.text);

		U.pl("***  ACE alignments ***\n");
		// Step (2)
		int aceOffsetCorrection = calculateAceOffsetCorrection(myDoc, aceDoc);
		
		// Steps (3), (4)
		List<AceDocument.Mention> aceMentions = aceDoc.document.getMentions();
		AceDocument.mentionsHeadSort(aceMentions);
		
		alignToTree(myDoc, aceOffsetCorrection, aceMentions);
		
		aceDoc.freezeMyMentions();
		
	}
	
	
	private static void displayAceMentions(
			List<AceDocument.Mention> aceMentions,
			Map<AceDocument.Mention, Word> ace2word) {
		Sentence curS = null;
		for (AceDocument.Mention m : aceMentions) {
			Word w = ace2word.get(m);
			assert w != null : "wtf every mention needs to map to something";
			if (w.sentence != curS) {
				curS = w.sentence;
				U.pf("S%-2s  %s\n", curS.ID(), curS.text());
			}
			U.pf("  %-4s | %s\n", m.entity.mentions.size()==1 ? "" : m.entity.ID(), m);
		}
	}
	
	private static void alignToTree(Document myDoc, int aceOffsetCorrection,
			List<AceDocument.Mention> aceMentions) throws Exception {

		for (AceDocument.Mention aceM : aceMentions) {
			int aceExtentStart = aceM.extent.charseq.start - aceOffsetCorrection;
			Sentence sent = myDoc.getSentenceContaining(aceExtentStart);
//			U.pl("\nSENTENCE "+sent.surfSent.cleanText);
//			U.pl("EXTENT < " + aceM.head.charseq.text + " | " + aceM.extent.charseq.text + ">");
			
			if ( ! sent.hasParse) {
				U.pl("No parse, getting null subtree match");
				aceM.myMention = myDoc.newMention(sent, null);
				continue;
			}
//			U.pf("EXTENT %d to %d\n", aceM.extent.charseq.start, aceM.extent.charseq.end);
			
			// Compute position of extent in this sentence
			//int start = aceM.head.charseq.start - aceOffsetCorrection - sent.surfSent.charStart;
			String [] tokens = AnalysisUtilities.getInstance().stanfordTokenize(aceM.head.charseq.text);

			tokens[tokens.length-1].length();
			int end = aceM.head.charseq.end - aceOffsetCorrection + 1 - sent.surfSent.charStart;
			int start = end - tokens[tokens.length-1].length();
			
			// sentence breaking errors can lead to the following
			if (start<0 && end>=sent.surfSent.rawText.length())
				throw new AlignmentFailed("both ACE extent bounds outside the sentence, weird");
			boolean weird=false;
			if (start<0) {start=0; weird=true;}
			if (end>sent.surfSent.rawText.length()) {end=sent.surfSent.rawText.length(); weird=true;}
			
			// Sanity check
			String pick = sent.surfSent.rawText.substring(start, end);
//			U.pf("EXTENT PICK: [%s]\n", U.backslashEscape(pick));
			pick = AnalysisUtilities.moreCleanup(pick).text;
			//assert weird || pick.equals( aceM.extent.charseq.text ) : "["+pick+"] -vs- <"+aceM.extent.charseq.text+">";
			if (weird)  U.pl("WEIRD:  "+"["+pick+"] -vs- <"+aceM.extent.charseq.text+">");
			
//			U.pf("ADJUSTED EXTENT:  %d to %d\n", start,end);
			
			// Find the span around this extent
			int leftW=-1, rightW=-1;
			for (int wi=0; wi < sent.words.size(); wi++) {
				Word w = sent.words.get(wi);
				int leftPos = w.charStart - sent.surfSent.charStart;
				int rightPos = (wi < sent.words.size() - 1) ? 
						sent.words.get(wi+1).charStart - sent.surfSent.charStart: 
						sent.surfSent.charEnd - sent.surfSent.charStart;

//				U.pf("word [%s] : %d to %d  =  [%s]\n", w, leftPos, rightPos, sent.surfSent.rawText.substring(leftPos,rightPos));
				
				if (leftPos <= start && start < rightPos) {
					assert leftW == -1;
					leftW = wi;
				}
				if (rightPos >= end  &&  leftPos < end) {
					assert rightW == -1;
					rightW = wi;
				}
			}
			if (weird) {
				// sometimes not resolved then.
				if (rightW==-1) rightW = sent.words.size()-1;
				if (leftW==-1)  leftW  = 0;
			}
			assert leftW!=-1 && rightW!=-1 : "leftW,rightW = "+leftW+","+rightW;
			assert rightW >= leftW : "leftW,rightW = "+leftW+","+rightW;

			Tree[] aceLeaves = new Tree[rightW - leftW + 1];
			for (int wi=leftW; wi<=rightW; wi++)  {
				aceLeaves[wi-leftW] = sent.words.get(wi).node();
			}
//			U.pf("ACE head leaves [size %2d]:  %s\n", aceLeaves.length, StringUtils.join(aceLeaves," "));
						
			// Shoehorn into the parsetree
//			if (leftW == rightW) {
//				
//				Tree parent = sent.words.get(leftW).node().parent(sent.rootNode());
//				if (parent.label().equals("JJ")) {
//					U.pl("Adjectival Mention " + aceM);
//					// TODO dont do following stuff
//				}
//			}
			
			
			Tree subtree = myDoc.findNodeThatCoversSpan(sent, leftW, rightW);
			Tree maxProjection = SyntacticPaths.getMaximalProjection(subtree, sent.rootNode());
			
			aceM.myMention = myDoc.newMention(sent, maxProjection);
			aceM.myMention.aceMention = aceM;
//			U.pl("Extracted Mention:\t" + maxProjection);
			
			
			/*int subtreeSize = subtree.getLeaves().size();
			
			if (subtree.label().value().equals("JJ")) {
				U.pl("OMG adjectival mention " + subtree);
				aceM.myMention = myDoc.newMention(sent, subtree);
			} else if (subtreeSize == rightW-leftW+1) {
				U.pl("Happy parse alignment size "+subtreeSize+"  :  " + subtree);
				data.Mention m = myDoc.newMention(sent, subtree);
				aceM.myMention = m;
			} else {
				U.pf("HMMM, ACE extent doesn't match lowest subtree:\n size %2d | %s\n size %2d | %s\n",
						aceLeaves.length, StringUtils.join(aceLeaves," "),
						subtreeSize,subtree
						);
				
				HeadFinder hf = AnalysisUtilities.getInstance().getHeadFinder();
				Tree subtreeHead = subtree.headTerminal(hf);
				U.pl("Subtree head: " + subtreeHead);
				for (Tree aceLeaf : aceLeaves) {
					if (aceLeaf.equals(subtreeHead)) {
						U.pl("It's a head projection of the ACE leaves, so we'll just use it");
						aceM.myMention = myDoc.newMention(sent, subtree);
						continue;
					}
				}
				U.pl("UHOH, it seems to cross between non-head-equivalent subtrees (e.g. PP attachment error can cause this)");
				aceM.myMention = myDoc.newMention(sent, null);
				
//				U.pf("UHOH, ACE extent leaves [size %-2d]:  %s\n", aceLeaves.length, StringUtils.join(aceLeaves," "));
//				U.pf("UHOH, lowest subtree    [size %-2d]:  %s\n", subtreeSize, subtree);
			}*/
		}
	}
	
//	private static void makeMention(Document myDoc, Mention aceM, int sentI, Tree subtree) {
//		
//		// 
//		
//	}
	
	/**
	 * ACE offsets are usually too high, by like 50-100 or so. 
	 * Estimate this offset correction by trying to find several crappy 
	 * string equality alignments then plurality vote **/
	public static int calculateAceOffsetCorrection(Document myDoc, AceDocument aceDoc) {
		IntCounter<Integer> offsetDiffs = new IntCounter();
		IntCounter<String> headCounts = new IntCounter();
		
		List<AceDocument.Mention> aceMentions = aceDoc.document.getMentions();
		AceDocument.mentionsHeadSort(aceMentions);
		
		for (AceDocument.Mention m : aceMentions) {
			headCounts.incrementCount(m.head.charseq.text);
		}
		assert !headCounts.keysAt(1).isEmpty() : "no singleton mention heads, alignment is hard.";
//		U.pl(headCounts);
		Set<String> uniqueHeads = headCounts.keysAt(1);
		for (AceDocument.Mention m : aceMentions) {
			if ( ! uniqueHeads.contains(m.head.charseq.text)) continue;
			if (offsetDiffs.size() > 5) break;
			for (Sentence s : myDoc.sentences()) {
				for (Word w : s.words) {
					if (m.head.charseq.text.equals(w.token)) {  //  crudeMatch_AceHead_vs_Token(m,w)) {
						offsetDiffs.incrementCount( m.head.charseq.start - w.charStart );
//						break sent_loop;
					}
				}
			}
		}
	
		
		

//		for (int i=0; i<aceMentions.size() && (i < 15 || offsetDiffs.max() < 2); i++) {
//			AceDocument.Mention m = aceMentions.get(i);
//			// find our first token that matches ace head
//			sent_loop:
//			for (Sentence s : myDoc.sentences()) {
//				for (Word w : s.words) {
//					if (crudeMatch_AceHead_vs_Token(m,w)) {
//						offsetDiffs.incrementCount( m.head.charseq.start - w.charStart );
////						break sent_loop;
//					}
//				}
//			}
//		}
		U.pl("ace offset diff histogram: " + offsetDiffs);
		U.pl("Using offset: " + offsetDiffs.argmax());
		return offsetDiffs.argmax();
	}
	
	public static boolean crudeMatch_AceHead_vs_Token(AceDocument.Mention m, Word w) {
		// rules can differ for, at the very least:
		// * whether punctuation is included:  [Mr] vs [Mr.]
		// * multiwords:  [Jeb Bush] vs [Jeb]
		
		String aceHead = m.head.charseq.text;
		String tok = w.token;
		
		if (aceHead.length()==1 && tok.length()==1) {
			return aceHead.equals(tok);
		} else if (aceHead.length()==1 || tok.length()==1) {
			// tiny tokens as substring matches is very false positive-y
			return false;
		} else {
			return tok.contains(aceHead) || aceHead.contains(tok);	
		}
	}
	
}
