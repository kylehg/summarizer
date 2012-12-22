package arkref.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import arkref.data.Document;
import arkref.data.EntityGraph;
import arkref.data.Mention;
import arkref.data.Sentence;
import arkref.data.Word;
import arkref.data.EntityGraph.Entity;
import arkref.parsestuff.U;


import edu.stanford.nlp.trees.Tree;

/** This is a kinda lame, hard-to-read XML format: 
 * a list of entities, each containing their referent mentions.
 */
public class WriteEntityXml {
	public static void go(EntityGraph eg, PrintWriter pw) throws FileNotFoundException {


		pw.printf("<entities>\n");
		List<Entity> ents = eg.sortedEntities();
		for (Entity e : ents) {
			pw.printf("<entity id=\"%s\">\n", e.id);
			for (Mention m : e.sortedMentions()) {
				pw.printf("  <mention ");
				pw.printf(" id=\"%s\"", m.ID());
				Sentence s = m.getSentence();
				pw.printf(" sentence=\"%s\"", s.ID());
				pw.printf(">\n");
				
				if (m.node() != null) {
					pw.printf("    <tokens>%s</tokens>\n", StringEscapeUtils.escapeXml(
						m.node().yield().toString()));
				}
				pw.printf("  </mention>\n");	
			}
			pw.printf("</entity>\n");
		}
		pw.printf("</entities>\n");
		
		pw.close();
		
	}
	
	public static void writeTaggedDocument(Document d, PrintWriter pw) throws FileNotFoundException {
		EntityGraph eg = d.entGraph();

		//pw.printf("<doc>\n");
		
		int sentnum = 0;
		for(Sentence s: d.sentences()){
			//pw.printf("<sentence>\n");
			int wordnum = 0;
			for(Tree leaf : s.rootNode().getLeaves()){

				if(wordnum > 0){
					pw.printf(" ");
				}
				
				for (Mention m : d.mentions()){
					List<Tree> mentionLeaves = m.node().getLeaves();
					if(mentionLeaves.get(0) == leaf){
						pw.printf("<mention mentionid=\"%d\" entityid=\"%s\">", m.ID(), eg.entName(m));
					}
				}
				
				pw.printf(leaf.yield().toString());
				
				for (Mention m : d.mentions()){
					List<Tree> mentionLeaves = m.node().getLeaves();
					if(mentionLeaves.get(mentionLeaves.size()-1) == leaf){
						pw.printf("</mention>", eg.entName(m));
					}
				}
				
				
				wordnum++;
			}
			
			pw.printf("\n");
			//pw.printf("</sentence>\n");
			sentnum++;
		}

		//pw.printf("</doc>\n");
		
		pw.close();
		
	}
	
}
