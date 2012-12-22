package arkref.data;

import java.io.Serializable;

import edu.stanford.nlp.trees.Tree;

/** Our notion of a word.  Integrates information from multiple sources; currently, SST, parse, and surface location.
 * Note that this is different than the stanford notion of a word (edu.stanford.nlp.ling.Word)
 * @author brendano
 */
public class Word implements Serializable {
	private static final long serialVersionUID = -5102799822220290219L;
	/** node could be null on parse failure **/
	private Tree node;
	private String ssTag;
	public int charStart = -1; // in raw original text
	public String token;
	public Sentence sentence; // enclosing sentence, just for convenience
	
	public Tree node() {
		return node;
	}
	
	public void setNode(Tree node) {
		this.node = node;
	}
	
	public String ssTag() {
		return ssTag;
	}
	
	public void setNeTag(String ssTag) {
		this.ssTag = ssTag;
	}
	
	public String toString() { 
		String s = token;
//		s += "/" + (node!=null ? node.parent().value() : "null"); // wrong
		s += "/" + ssTag;
		return s;
	}
}