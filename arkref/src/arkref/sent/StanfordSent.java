package arkref.sent;

import java.io.FileNotFoundException;

import arkref.parsestuff.AnalysisUtilities;
import arkref.parsestuff.U;


public class StanfordSent {
	public static void main(String[] args) throws FileNotFoundException {
		String text = U.readFile(args[0]);
		for(String s : AnalysisUtilities.getInstance().getSentencesStanford(text)) {
			U.pl(s);
		}
	}
}
