package arkref.ace;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arkref.parsestuff.U;

public class AcePreprocess {
	public static void go(String path1) throws IOException {
		String shortpath = arkref.analysis.Preprocess.shortPath(path1);
		
		shortpath = shortpath.replace("_APF.XML", "");
		String sgmlFilename = shortpath + ".SGM";
		assert new File(sgmlFilename).exists();
		String sgml = U.readFile(sgmlFilename);
		Pattern p = Pattern.compile("<TEXT>(.*)</TEXT>", Pattern.DOTALL);
		Matcher m = p.matcher(sgml);
		m.find();
		String text = m.group(1);
		U.writeFile(text, shortpath + ".txt");
	}
	
	public static void main(String args[]) throws IOException {
		for (String arg : args) {
			if (args.length > 1)  U.pf("DOC\t%s\n", arg);
			go(arg);
		}
	}
}
