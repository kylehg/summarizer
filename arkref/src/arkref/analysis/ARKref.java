package arkref.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import arkref.ace.AceDocument;
import arkref.ace.AcePreprocess;
import arkref.ace.Eval;
import arkref.ace.FindAceMentions;
import arkref.data.Document;
import arkref.ext.fig.basic.Option;
import arkref.ext.fig.basic.OptionsParser;
import arkref.parsestuff.U;

public class ARKref {
	
	public static class Opts {
		@Option(gloss="Take input from STDIN and produce output on STDOUT")
		public static boolean stdin = false;
		@Option(gloss="Input document paths, with or without extentions. e.g. data/*.sent")
		public static String[] input;
		//@Option(gloss="Write mention-tagged XML sentence output to .tagged")
		//public static boolean writeTagged = false;
		@Option(gloss="Debug output")
		public static boolean debug = false;
		@Option(gloss="Use ACE eval pipeline")
		public static boolean ace = false;
		@Option(gloss="Force preprocessing")
		public static boolean forcePre = false;
		@Option(gloss="Oracle semantics ... for analysis only")
		public static boolean oracleSemantics = false;
		@Option(gloss="Number of sentences in possible antecedent window")
		public static int sentenceWindow = 999;
		@Option(gloss="Properties file path")
		public static String propertiesFile = "config/arkref.properties";
	}
	

	public static boolean showDebug() {
		return Opts.debug;
	}
	
	public static void main(String[] args) throws Exception {		

	
		OptionsParser op = new OptionsParser(Opts.class);
		op.doParse(args);
		if (Opts.ace)  Opts.debug = true;
		

		if(Opts.propertiesFile != null){
			ARKref.setPropertiesPath(Opts.propertiesFile);
			ARKref.loadProperties(Opts.propertiesFile);
		}
		
		if (!Opts.stdin && (Opts.input == null || Opts.input.length==0)) {
			System.err.println(
			"Please specify -input or -stdin for input.\n"+
//			"\nLeaving off extension is OK.  "+
//			"We assume other files are in same directory with different extensions; "+
//			"if they don't exist we will make them." +
			"For all options, see: ./arkref.sh -help");
			System.exit(-1);
		}
		
		//if (!(Opts.debug || Opts.writeTagged)) {
		//	System.err.println("Need to specify some sort of output, e.g. -writeTagged or -debug");
		//	System.exit(-1);
		//}
		
		U.pl("=Options=\n" + op.doGetOptionPairs());
		
		//take input on stdin, store parses, split sentences, and SS tags
		//in temporary files.
		if(Opts.stdin){ 
			
			//create temporary files and have them be deleted 
			//when the program exits
			File tmpFile = File.createTempFile("arkref-temp-file", ".txt");
			tmpFile.deleteOnExit();
			String tmpPath = tmpFile.getAbsolutePath();
			String tmpPrefix = tmpPath.substring(0, tmpPath.lastIndexOf(".")); 
			File tmpParseFile = new File(tmpPrefix + ".parse");
			tmpParseFile.deleteOnExit();
			File tmpSSTFile = new File(tmpPrefix + ".sst");
			tmpSSTFile.deleteOnExit();
			File tmpSentFile = new File(tmpPrefix + ".osent");
			tmpSentFile.deleteOnExit();
						
			PrintWriter pw = new PrintWriter(new FileOutputStream(tmpFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String buf;
			while((buf = br.readLine()) != null){
				pw.println(buf);
			}
			pw.close();
			
			Opts.input = new String[1];
			Opts.input[0] = tmpFile.getAbsolutePath();
		}else{
//			System.err.println("INPUT:"+Opts.input[0]);
		}
		
		boolean dots = Opts.input.length > 1;
		for (String path : Opts.input) {
			path = Preprocess.shortPath(path);
			if (dots) System.err.print("."); 

			U.pl("\n***  Input "+path+"  ***\n");
			
			Document d;

			if (Opts.ace) {
				if (Opts.forcePre || !Preprocess.alreadyPreprocessed(path)) {
					AcePreprocess.go(path);
					Preprocess.go(path);
				}
				d = Document.loadFiles(path);
				AceDocument aceDoc = AceDocument.load(path);
				d.ensureSurfaceSentenceLoad(path);
				FindAceMentions.go(d, aceDoc);
				Resolve.go(d);
				RefsToEntities.go(d);
				Eval.pairwise(aceDoc, d.entGraph());
			} else {
				if (Opts.forcePre || !Preprocess.alreadyPreprocessed(path)) {
					Preprocess.go(path);
				}
				d = Document.loadFiles(path);
				FindMentions.go(d);
				Resolve.go(d);
				RefsToEntities.go(d);
			}
			
//			if (Opts.writeEntityMentionXml){
//				WriteEntityMentionXml.go(d.entGraph(), path);
//			}
			if (!Opts.debug){
				PrintWriter pw = null;
				
				if(Opts.stdin){
					pw = new PrintWriter(System.out);
				}else{
					String filename = path + ".tagged";
					File file = new File(filename);
					pw = new PrintWriter(new FileOutputStream(file));
					U.pl("Writng resolutions to " + filename);
				}
				WriteEntityXml.writeTaggedDocument(d, pw);
			}
		}
		if (dots) System.err.println("");
	}

	public static Properties getProperties() {
		if(properties == null){
			loadProperties(ARKref.Opts.propertiesFile);
		}
		
		return properties;
	}
	
	private static void loadProperties(String propertiesFile) {
		properties = new Properties();
		try{
			properties.load(new FileInputStream(propertiesFile));
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		
	}

	public static String getPropertiesPath() {
		return propertiesPath;
	}

	public static void setPropertiesPath(String propertiesPath) {
		ARKref.propertiesPath = propertiesPath;
	}
	
	private static String propertiesPath = "config/arkref.properties";
	private static Properties properties = null;

}
