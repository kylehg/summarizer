package arkref.ace;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.core.Persister;


import arkref.parsestuff.U;

import com.aliasi.util.Strings;

/**
 * A fairly thin wrapper around the APF XML data structures.
 * 
 * http://www.ldc.upenn.edu/Catalog/docs/LDC2005T09/README
 * 
 * also APF_V4_0_1.DTD though it's not super helpful
 * 
 * The XML parser: http://simple.sourceforge.net/download/stream/doc/tutorial/tutorial.php
 */
public class AceDocument {
	public Document document;
	public String text;
	private Map<arkref.data.Mention, AceDocument.Mention> myMention2aceMention;
	
	public void freezeMyMentions() {
		assert myMention2aceMention==null : "freeze only once!";
		myMention2aceMention = new HashMap();
		for (Mention aceM : document.getMentions()) {
			if (aceM.myMention != null)
				myMention2aceMention.put(aceM.myMention, aceM);
		}
	}
	public AceDocument.Mention getAceMention(arkref.data.Mention myMention) {
		return myMention2aceMention.get(myMention);
	}
	public static AceDocument load(String path) throws Exception {
		String apfPath = path + "_APF.XML";
		String textPath= path + ".txt";
		AceDocument aceDoc = new AceDocument();
		
		aceDoc.text = U.readFile(textPath);
		aceDoc.document = parseFile(apfPath);
	
		return aceDoc;
	}
	
	public static Document parseFile(String apfXmlFile) throws Exception { 
		Serializer serializer = new Persister();
		File source = new File(apfXmlFile);
		SourceFile sf = null;
		sf = serializer.read(SourceFile.class, source);
		for (Entity en : sf.document.entities ) {
			for (Mention m : en.mentions) {
				assert en.ID().replace("E","").equals(m.aceID.replaceFirst("-.*",""));
				m.entity = en;
			}
		}
		return sf.document;
	}
	
	public static void main(String args[]) throws Exception {
		for (String f : args) {
			Document d = parseFile(f);
			for (Entity en : d.entities ) {
				for (Mention m : en.mentions) {
					U.pl(m.aceID +" | "+m.ID()+" | "+m.head.charseq.text+" | "+m.extent.charseq.text);
				}
			}
		}
	}
	
	
	
	////////////   APF XML structures  ////////////
	
	@Root(strict=false)
	public static class SourceFile {
		@Element(name="document")
		Document document;
	}
	@Root(strict=false)
	public static class Document {
		@ElementList(inline=true, entry="entity")
		List <Entity> entities;
		@Attribute(name="DOCID")
		String docid;
		
		
		public ArrayList<Mention> getMentions() {
			ArrayList <Mention> mentions = new ArrayList<Mention>();
			for (Entity en : entities ) {
				for (Mention m : en.mentions) {
					mentions.add(m);
				}
			}
			return mentions;
		}
	}
	public static void mentionsHeadSort(List<Mention> mentions) {
		Collections.sort(mentions, 
				new Comparator<AceDocument.Mention>() {
				public int compare(Mention m1, Mention m2) {
					return Integer.valueOf(m1.head.charseq.start).compareTo(m2.head.charseq.start);
				}
		});
	}
	public static void mentionsExtentSort(List<Mention> mentions) {
		Collections.sort(mentions, 
				new Comparator<AceDocument.Mention>() {
				public int compare(Mention m1, Mention m2) {
					return Integer.valueOf(m1.extent.charseq.start).compareTo(m2.extent.charseq.start);
				}
		});
	}
	@Root(name="entity",strict=false)
	public static class Entity {
		@Attribute(name="ID")
		private String aceID;
		@ElementList(inline=true)
		List <Mention> mentions;
		public String ID() { return aceID.replaceFirst(".*-E", "E"); }
		public String toString() { return String.format("%-3s", ID()); }
	}
	@Root(name="entity_mention",strict=false)
	public static class Mention {
		@Attribute(name="ID")
		public String aceID;
		@Element
		public Phrase extent;
		@Element
		public Phrase head;
		
		public Entity entity;
		
		/** Convenience for later processing: the data.Mention this ACE mention corresponds to. **/
		public arkref.data.Mention myMention = null;
		
		public int ID() { return Integer.parseInt(aceID.replaceFirst(".*-","")); }
		
		public boolean isSingleton() {
			assert entity.mentions.size() != 0;
			return entity.mentions.size() == 1;
		}
		 
		public String toString() { 
			if (myMention != null) {
				String ex = Strings.normalizeWhitespace(extent.charseq.text);
				String h = Strings.normalizeWhitespace(head.charseq.text);
				if (ex.equals(h))
					return String.format("M%-2d <%s>", myMention.ID(), ex);
				else
					return String.format("M%-2d <%s | %s>", myMention.ID(), ex, h);
			} else {
				return String.format("AM%-2d | %s", ID(),
						Strings.normalizeWhitespace(extent.charseq.text));	
			}
//			return String.format("AM%-3d | %s | %s", ID(),
//				Strings.normalizeWhitespace(head.charseq.text), Strings.normalizeWhitespace(extent.charseq.text));
		}
	}
	@Root(strict=false)
	public static class Phrase {
		@Element(name="charseq")
		public Charseq charseq;
	}
	@Root
	public static class Charseq {
		// these start and ends are consistent with one another, but it's a complete mystery what they're counting from
		// e.g. start=0 is a random-ass place in the SGML file.
		@Attribute(name="START")
		public int start;
		@Attribute(name="END")
		public int end;
		@Text
		public String text;
	}
	
}
