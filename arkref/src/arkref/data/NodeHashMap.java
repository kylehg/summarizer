package arkref.data;

import java.io.Serializable;
import java.util.HashMap;

import edu.stanford.nlp.trees.Tree;

/**
 * This lets you use parse nodes as keys in a HashMap.
 * 
 * If you try to do this with Stanford Node's, it thinks e.g.
 * (DT the) in multiple sentences can sometimes be the same.
 * This class is a workaround because we care about cross-sentence comparisons.
 * You have to pass the node's enclosing sentence to compute the key.
 * 
 * Theoretically we could wrap all hashmap operations here.
 *  
 * @author brendano
 * @param <ValueT>
 *
 */
public class NodeHashMap<ValueT> implements Serializable {
	private static final long serialVersionUID = -381382347554050836L;
	private HashMap<String, ValueT> map;
	public NodeHashMap() {
		map = new HashMap();
	}
	public static String nodeKey(Sentence s, Tree node) {
		return String.format("sent_%s_node_%s_%s", s.ID(), s.rootNode().leftCharEdge(node), node.hashCode());
	}


	public ValueT get(Sentence s, Tree node) {
		String key = nodeKey(s,node);
		return map.get(key);
	}

	public void put(Sentence s, Tree node, ValueT value) {
		String key = nodeKey(s,node);
		map.put(key, value);
	}

	public boolean containsKey(Sentence s, Tree node) {
		return map.containsKey(nodeKey(s,node));
	}

}
