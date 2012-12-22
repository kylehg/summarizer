package arkref.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arkref.data.Document;
import arkref.data.Mention;
import arkref.parsestuff.AnalysisUtilities;
import arkref.parsestuff.TregexPatternFactory;


import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class SyntacticPaths {


	/**
	 * finds the closest candidate by looking at the syntactic path distance
	 * 
	 * @param mention 
	 * @param candidates other mentions that appeared previously
	 * @return
	 */
	public static Mention findBestCandidateByShortestPath(Mention mention, List<Mention> candidates, Document document) {
		int minLength = 1000000;
		int minIndex = 0;
		Mention res; 

		List<Integer> pathLengths = scoreCandidatesByPathLength(mention, candidates, document);

		//Mention tmpCandidate;
		int tmp;
		for(int i=0; i<pathLengths.size(); i++){
			tmp = pathLengths.get(i);
			String tmpS = "";
			if(candidates.get(i).node() != null){
				tmpS = candidates.get(i).node().yield().toString();
			}
//			U.pl("distance:"+tmp+"\t"+tmpS);
			
			if(tmp < minLength){
				minLength = tmp;
				minIndex = i;
			}
		}

		res = candidates.get(minIndex);
		
		return res;
	}


	public static List<Integer> scoreCandidatesByPathLength(Mention mention, List<Mention> candidates, Document doc) {
		List<Integer> pathLengths = new ArrayList<Integer>();

		Iterator<Mention> iter = candidates.iterator();
		Mention tmpCandidate;
		while(iter.hasNext()){
			tmpCandidate = iter.next();
			pathLengths.add(computePathLength(mention.node(), tmpCandidate.node(), doc.getTree()));

		}

		return pathLengths;
	}




	/**
	 * 
	 * @param node1
	 * @param node2
	 * @param commonRoot should contain both node1 and node2
	 * @return
	 */
	public static int computePathLength(Tree node1, Tree node2, Tree commonRoot) {
		int res = 1000;

		/*
		//find the node in the tree that dominates both input nodes
		int len1 = 0;
		int len2 = 0;
		Tree tmpNode = node1;
		List<Tree> dominationPath;
		while(tmpNode != null){
			dominationPath = tmpNode.dominationPath(node2);
			if(dominationPath != null){
				len2 = dominationPath.size()-1;
			}

			tmpNode = tmpNode.parent(commonRoot);
			len1++;
		}

		//sum the distances from each input node to their common ancestor
		res = len1+len2;*/

		List<Tree> path = commonRoot.pathNodeToNode(node1, node2);
		if(path != null){
			res = path.size()-1;
		}
		//System.err.println(res+"\t"+node2.toString());
		return res;
	}


	public static boolean aIsDominatedByB(Mention A, Mention B) {
		boolean bDominatesA = B.node().dominates(A.node());


		return bDominatesA;
	}


	public static Tree getMaximalProjection(Tree parent, Tree root) {
		Tree res = parent;
		Tree tmp = parent;
		HeadFinder hf = AnalysisUtilities.getInstance().getHeadFinder();
		Tree parentHead = parent.headTerminal(hf);
		while(tmp != null){
			if(tmp.headTerminal(hf) == parentHead && tmp.parent(root) != null){
				res = tmp;
			}else{
				break;
			}
			tmp = res.parent(root);
			//System.err.println("\tp:"+parent.toString()+"\tpHead:"+parentHead+"\ttmp:"+tmp);
		}
		//System.err.println("node:"+parent.toString()+"\tmaxProjection:"+res.toString());
		
		return res;
	}

	
	/**
	 * Objects (and other verb arguments) can't refer with the subjects of the same clause,
	 * unless the object is reflexive.
	 * 
	 * e.g., in "The man gave him a book.", "him" != "man"
	 * 
	 * @param m1
	 * @param m2
	 * @return
	 */
	public static boolean inSubjectObjectRelationship(Mention m1, Mention m2) {
		Tree t = m2.node();
		Tree root = m2.getSentence().rootNode();

		//return false if these mentions are not in the same sentence
		if(root != m1.getSentence().rootNode()){
			return false;
		}
		Tree ancestor = t.parent(root);

		//find the subject of the clause that m2 is part of (try to do so even if there is embedding)
		while(ancestor != null && ancestor != root){
			if(ancestor.label().value().equals("S")){
				TregexPattern pat = TregexPatternFactory.getPattern("S < (NP=subject !$,, NP) < VP");
				TregexMatcher matcher = pat.matcher(ancestor);
				if (matcher.find()) {
					Tree subj = matcher.getNode("subject");
					return m1.node() == subj;
				}				
			}else if(ancestor.label().value().equals("NP")){ 
				//return false if m2 is not a maximally projected node.
				//This accounts for cases like Nintendo introduced its new console
				return false;
			}
			ancestor = ancestor.parent(root);
		}

		return false;
	}
	
	
	/**
	 * Subjects cannot refer to NPs in non-finite subordinate clauses, prepositional phrases, etc.
	 * modifying the same main clause
	 * 
	 * e.g., in "To call John, he picked up the phone" he != John  
	 * in "To John, he was a stranger." he != John  
	 * in "Because John likes cars, he bought a Ferrari." he might be John
	 * 
	 * @param m2
	 * @param m1
	 * @return
	 */
	public static boolean isSubjectAndMentionInAdjunctPhrase(Mention m1, Mention m2) {
		Tree t = m1.node();
		Tree root = m1.getSentence().rootNode();

		Tree clause = t.parent(root);
		if(!clause.label().value().equals("S")){
			return false;
		}
		
		TregexPattern pat = TregexPatternFactory.getPattern("NP=np !>> (S < NP < VP >> S)");
		TregexMatcher matcher = pat.matcher(clause);
		while(matcher.find()) {
			Tree np = matcher.getNode("np");
			if(np == m2.node()) return true;
		}		

		return false;
	}


	public static boolean isInQuotation(Mention m){
		//find all quote nodes, and see if any of them c-command the mention node
		
		TregexPattern pat = TregexPatternFactory.getPattern("``");
		TregexMatcher matcher = pat.matcher(m.getSentence().rootNode());
		while(matcher.find()) {
			Tree quote = matcher.getMatch();
			if(cCommands(quote, m.node(), m.getSentence().rootNode())){
				return true;
			}
		}	
		
		return false;
	}
	
	/**
	 * There is a bug in the stanford Tree.cCommands method, I think
	 * @return
	 */
	public static boolean cCommands(Tree n1, Tree n2, Tree root){
		Tree n1Parent = n1.parent(root);
		
		for(Tree sibling: n1Parent.getChildrenAsList()){
			if(sibling == n1){
				continue;
			}
			
			if(sibling.dominates(n2)){
				return true;
			}
		}
		
		return false;
	}
	
}
