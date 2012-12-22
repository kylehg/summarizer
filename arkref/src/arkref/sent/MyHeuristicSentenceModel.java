/*
 * LingPipe v. 3.8
 * Copyright (C) 2003-2009 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package arkref.sent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import arkref.parsestuff.U;
import arkref.parsestuff.RegexUtil.R;

import com.aliasi.sentences.AbstractSentenceModel;

/**
 * A <code>HeuristicSentenceModel</code> determines sentence
 * boundaries based on sets of tokens, a pair of flags, and an
 * overridable method describing boundary conditions.

 * <P>There are three sets of tokens specified for a heuristic model:
 *
 * <UL>
 *
 * <LI> <b>Possible Stops</b>: These are tokens that are allowed
 * to be the final token in a sentence.  This set typically includes
 * sentence-final punctuation tokens such as periods (<code>.</code>)
 * and double quotes (<code>&quot;</code>).
 *
 * <LI> <b>Impossible Penultimates</b>: These are tokens that may
 * <i>not</i> be the penultimate (second-to-last) token in a sentence.
 * This set is typically made up of abbreviations or acronyms such as
 * <code>&quot;Mr&quot;</code>.
 *
 * <LI> <b>Impossible Starts</b>: These are tokens that may <i>not</i>
 * be the first token in a sentence.  This set typically includes
 * punctuation characters that should be attached to the previous
 * sentence such as end quotes (<code>''</code>).  Note that there is
 * a method, described below, which may enforce additional conditions
 * on start tokens.
 *
 * </UL>
 *
 * Note that all of these sets perform <i>case insensitive</i> tests.
 *
 * <P>There are also two flags in the constructor that determine
 * aspects of sentence boundary detection:
 *
 * <UL>

 * <LI> <b>Balance Parentheses</b>: If parentheses are being balanced,
 * then as long as there are open parentheses that have not been
 * closed, the current sentence may not end.  Square brackets
 * (<code>"[", "]"</code>) and round brackets (<code>"(", ")"</code>),
 * are balanced separately.  The brackets need not be nested, and
 * extra close parentheses (<code>")"</code>) and brackets
 * (<code>"]"</code>) are ignored.
 *
 * <LI> <b>Force Final Boundary</b>: If this flag is set to
 * <code>true</code>, the final token in any input is taken to be a
 * sentence terminator, whether or not is a possible stop token.  This
 * is useful for dealing with truncated inputs, such as those in
 * MEDLINE abstracts.
 *
 * </UL>
 *
 * A further condition is imposed on sentence initial tokens by method
 * {@link #possibleStart(String[],String[],int,int)}.  This method
 * checks a given token in sequence of tokens and whitespaces to
 * determine if it is a possible sentence start.  The default
 * implementation in this class is to rule out tokens that start with
 * lowercase letters.
 *
 * <P>The final condition is that a token cannot be a stop unless it
 * is followed by non-empty whitespace.
 *
 * <p> The resulting model will miss tokens as boundaries that act as
 * both sentence boundaries and end-of-abbreviation markers for known
 * abbreviations.  It will add spurious sentence boundaries that
 * appear after unknown abbreviations and are followed by whitespace
 * and a capitalized word.
 *
 * <p>Our approach is loosely based on the article:
 *
 * <blockquote>
 * Mikheev, Andrei. 2002.
 * <a href="http://acl.ldc.upenn.edu/J/J02/J02-3002.pdf">Periods, Capitalized Words, etc.</a>
 * <i>Computational Linguistics</i> <b>28</b>(3):289-318.
 * </blockquote>
 *
 * @author  Mitzi Morris
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class MyHeuristicSentenceModel extends AbstractSentenceModel {
	// see original code for more comments

    Set<String> mPossibleStops;
    Set<String> mBadPrevious;
    Set<String> mBadFollowing;
    Set<Pattern> mStopPatterns;
    private final boolean mForceFinalStop;
    private final boolean mBalanceParens;
    private final boolean mUsingCapitalizationConventions;
    
    /**
     * Construct a heuristic sentence model with the specified sets
     * of possible stop tokens, impossible penultimate tokens, impossible
     * start tokens, and flags for whether the final token is forced
     * to be a stop, and whether parentheses are balanced.  Note that
     * the token sets are <i>case insensitive</i>.
     *
     * @param possibleStops Possible tokens on which to stop a sentence.
     * @param impossiblePenultimate Tokens that may not precede a stop.
     * @param impossibleStarts Tokens that may not follow a stop.

    */
    public MyHeuristicSentenceModel(
    		Set<String> possibleStops,
    		Set<String> stopPatterns,						
			Set<String> impossiblePenultimate,
			Set<String> impossibleStarts,
			boolean forceFinalStop,
			boolean balanceParens,
			boolean usingCapitalizationConventions
          ) {
		mPossibleStops = toLowerCase(possibleStops);
		mStopPatterns = convertStopPatterns(stopPatterns);
        mBadPrevious = toLowerCase(impossiblePenultimate);
        mBadFollowing = toLowerCase(impossibleStarts);	
        mForceFinalStop = forceFinalStop;
        mBalanceParens = balanceParens;
        mUsingCapitalizationConventions = usingCapitalizationConventions;
    }
    
    private Set<Pattern> convertStopPatterns(Set<String> stopPatterns) {
    	Set<Pattern> ret = new HashSet<Pattern>();
    	for (String pat : stopPatterns) {
    		Pattern p = Pattern.compile(pat);
    		ret.add(p);
    	}
    	return ret;
    }
    
    /**
     * Returns <code>true</code> if this model treats any input-final
     * token as a stop.  This ensures that in truncated inputs, all
     * tokens are or are followed by a sentence boundary.  For
     * instance, if the input is the array of tokens
     * <code>{&quot;a&quot;, &quot;b&quot;, &quot;.&quot;,
     * &quot;c&quot;, &quot;d&quot;}</code>, then if
     * <code>&quot;d&quot;</code> is <i>not</i> in the set of possible
     * stops, then the tokens <code>&quot;c&quot;</code> and
     * <code>&quot;d&quot;</code> will not be assigned to a sentence.
     * If the allow-any-final-token flag is <code>true</code>, then in
     * the case where the <code>&quot;d&quot;</code> is final in the
     * input, it will be taken to end a sentence.
     *
     * <P>The value is set in the constructor {@link
     * #HeuristicSentenceModel(Set,Set,Set,boolean,boolean)}.
     * See the class documentation for more information.
     *
     * @return <code>true</code> if any token may be a stop if
     * it is final in the input.
     */
    public boolean forceFinalStop() {
        return mForceFinalStop;
    }

    /**
     * Returns <code>true</code> if this model does parenthesis
     * balancing.  Note that the value is set in the constructor
     * {@link #HeuristicSentenceModel(Set,Set,Set,boolean,boolean)}.
     * See the class documentation for more information.
     *
     *
     * @return <code>true</code> if this model does parenthesis
     * balancing.
     */
    public boolean balanceParens() {
        return mBalanceParens;
    }
    
    public boolean isPossibleStop(int tokPos, String[] tokens, String[] whites) {
		boolean oneTokMatch = mPossibleStops.contains(tokens[tokPos].toLowerCase());
		if (oneTokMatch) return true;
		String tokPair = tokens[tokPos-1] + whites[tokPos] + tokens[tokPos];
		for (Pattern p : mStopPatterns) {
//			U.pl(p);
//			U.pl("["+tokPair+"]");
			Matcher m = p.matcher(tokPair);
			if (m.find()) return true;
		}
		return false;
    }


    /**
     * Adds the sentence final token indices as <code>Integer</code>
     * instances to the specified collection, only considering tokens
     * between index <code>start</code> and <code>end-1</code>
     * inclusive.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @param start Index of first token to annotate.
     * @param length Number of tokens to annotate.
     * @param indices Collection into which to write the boundary
     * indices.
     */
    @Override
    public void boundaryIndices(String[] tokens, String[] whitespaces,
                                int start, int length,
                                Collection<Integer> indices) {
        if (length == 0) return;

        if (length == 1) {
            if (mForceFinalStop || isPossibleStop(start, tokens, whitespaces)) {
                indices.add(Integer.valueOf(start));
            }
            return;
        }
        
        // run from second to penultimate tag (first can't be stop)
        boolean inParens = false;
        if (tokens[start].equals("(")) inParens = true;
        boolean inBrackets = false;
        if (tokens[start].equals("[")) inBrackets = true;
        int end = start+length-1;
        for (int i = start+1; i < end; ++i) {
            // check paren balancing
            if (mBalanceParens) {
                if (tokens[i].equals("(")) {
                    inParens=true;
                    continue;
                }
                if (tokens[i].equals(")")) {
                    inParens = false;
                    continue;
                }
                if (tokens[i].equals("[")) {
                    inBrackets=true;
                    continue;
                }
                if (tokens[i].equals("]")) {
                    inBrackets=false;
                    continue;
                }
                // don't break if we're in parenthetical or bracketed
                if (inParens || inBrackets) continue;
            }

            // check that token is good end of sentence token
            if (!isPossibleStop(i,tokens,whitespaces)) continue;

            // only break after whitespace
            if (whitespaces[i+1].length() == 0) continue;

            // check that previous token is OK sentence end
            if (mBadPrevious.contains(tokens[i-1].toLowerCase())) continue;

            // check that following token is OK sentence start
            if (mBadFollowing.contains(tokens[i+1].toLowerCase())) continue;

            // check following tokens, as needed
            if (!possibleStart(tokens,whitespaces,i+1,end)) continue;

            indices.add(Integer.valueOf(i));
        }

        // deal with case of last tag
        if (mForceFinalStop
            || (isPossibleStop(end, tokens, whitespaces)
                 && !mBadPrevious.contains(tokens[end-1].toLowerCase())))
            indices.add(Integer.valueOf(end));
    }

    /**
     * Return <code>true</code> if the specified start index can
     * be a sentence start in the specified array of tokens and
     * whitespaces running up to the end token.
     *
     * <P>The implementation in this class requires the first token to
     * be non-empty and have a first character that is not lower case
     * according to {@link Character#isLowerCase(char)}.
     *
     * <P>The start and end indices should be within range for the
     * tokens and whitespaces as a precondition to this method being
     * called.  For a precise definition, see {@link
     * #verifyBounds(String[],String[],int,int)}.  All calls from the
     * abstract sentence model obey this constraint.
     *
     * @param tokens Array of tokens to check.
     * @param whitespaces Array of whitespaces to check.
     * @param start Index of first token to check.
     * @param end Index of last token to check.
     */
    protected boolean possibleStart(String[] tokens, String[] whitespaces,
                                    int start, int end) {
        String tok = tokens[start];
        if (mUsingCapitalizationConventions)
        	return tok.length() > 0 && !Character.isLowerCase(tok.charAt(0));
        else
        	return tok.length() > 0;
        	
    }

    static Set<String> toLowerCase(Set<String> xs) {
        Set<String> result = new HashSet<String>();
        for (String s : xs)
            result.add(s.toLowerCase());
        return result;
    }
}
