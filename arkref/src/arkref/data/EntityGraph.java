package arkref.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import arkref.parsestuff.U;


/**
 * progressively add pairwise equivalences to this data structure.  
 * internally, it builds the transitive closure. 
 **/
public class EntityGraph implements Serializable {
	private static final long serialVersionUID = -3407431672317709104L;
	public Map<Mention, HashSet<Mention>> mention2corefs;
	public Set<Entity> entities = null;
	
	public class Entity implements Serializable{
		private static final long serialVersionUID = -6664324222677207904L;
		public String id;
		public Set<Mention> mentions;
		

		public int hashCode() { return id.hashCode(); }
		public boolean equals(Object _e2) {
			Entity e2 = (Entity) _e2;
			if (e2==null) return false;
			assert this.id!=null && e2.id!=null;
			return this.id.equals( e2.id );
		}
		public List<Mention> sortedMentions() {
			List<Mention> ms = new ArrayList();
			for (Mention m : mentions)
				ms.add(m);
			Collections.sort(ms, new Comparator<Mention>() {
				public int compare(Mention m1, Mention m2) {
					return new Integer(m1.ID()).compareTo(m2.ID());
				}
			});
			return ms;
		}
		public String toString() {
			Iterator<Mention> it = mentions.iterator();
			Mention m = it.next();
			String name = entName(m);
			if (mentions.size()==1)
				return "singleton_"+name;
			return "entity_"+name;
		}
		

	}
	public EntityGraph(Document d) {
		mention2corefs = new HashMap<Mention, HashSet<Mention>>();
		for (Mention m : d.mentions()) { 
			mention2corefs.put(m, new HashSet<Mention>());
			mention2corefs.get(m).add(m);
		}
	}
	
	public void addPair(Mention m1, Mention m2) {
		assert entities==null : "we're frozen, please don't addPair() anymore";
		// Strategy: always keep mention2corefs a complete record of all coreferents for that mention
		// So all we do is merge
		Set<Mention> corefs1 = (Set<Mention>) mention2corefs.get(m1).clone();
		Set<Mention> corefs2 = (Set<Mention>) mention2corefs.get(m2).clone();
		for (Mention n1 : corefs1) {
			for (Mention n2 : corefs2) {
				mention2corefs.get(n1).add(n2);
				mention2corefs.get(n2).add(n1);
			}
		}
	}
	
	/** Call this only once, and only after all addPair()ing is done. **/
	public void freezeEntities() {
		assert entities == null : "call freezeEntities() only once please";
		entities = new HashSet<Entity>();
		Set<String> bla = new HashSet<String>();
		for (Mention m : mention2corefs.keySet()) {
			Entity e = makeEntity(m);
			entities.add(e);
		}
	}
	/** helper for freezeEntities() **/
	private Entity makeEntity(Mention m) {
		Entity e = new Entity();
		e.id = entName(m);
		e.mentions = mention2corefs.get(m);
		return e;
	}
	
	public Set<Mention> getLinkedMentions(Mention m){
		return mention2corefs.get(m);
	}
	
	public boolean isSingleton(Mention m) {
		return mention2corefs.get(m).size()==1;
	}
	
	public String entName(Mention m) {
		return entName(mention2corefs.get(m));
	}
	
	public String entName(Set<Mention> corefs) {
		List<Integer> L = new ArrayList<Integer>();
		for (Mention m : corefs) {
			L.add(m.ID());
		}
		Collections.sort(L);
		return StringUtils.join(L, "_");
	}
	
	public List<Entity> sortedEntities() {
		List<Entity> ents = new ArrayList();
		for (Entity e : entities) {
			ents.add(e);
		}
		Collections.sort(ents, new Comparator<Entity>() {
			public int compare(Entity e1, Entity e2) {
				return e1.id.compareTo(e2.id);
//				List<Mention> ms1 = e1.sortedMentions();
//				List<Mention> ms2 = e2.sortedMentions();
//				if (ms1.size()==0 || ms2.size()==0)
//				e1.id
//				e1.mentions
		}});
		return ents;
	}
}
