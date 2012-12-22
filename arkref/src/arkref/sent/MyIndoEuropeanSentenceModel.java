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

import java.util.HashSet;
import java.util.Set;

import arkref.parsestuff.RegexUtil.R;
import arkref.sent.MyHeuristicSentenceModel;


/**
 * HACKED UP FROM LINGPIPE ORIGINAL BY BRENDAN
 * 
 * 
 * 
 * 
 * An <code>IndoEuropeanSentenceModel</code> is a heuristic sentence
 * designed primarily for English.  Whehter or not it balances
 * parentheses or forces the last token to be a boundary may be
 * set in the constructor.  It uses the default implementation of
 * possible sentence starts and the following token sets:
 *
 * <blockquote>
 * <table border='0' cellpadding='20'>
 * <tr>
 *
 *   <td valign='top' width='33%'>
 *   <table border="1" cellpadding="3" width='100%'>
 *     <tr><td><b>Possible Stops</b></td></tr>
 *     <tr><td><code><b>.</b></code></td></tr>
 *     <tr><td><code><b>..</b></code></td></tr>
 *     <tr><td><code><b>!</b></code></td></tr>
 *     <tr><td><code><b>?</b></code></td></tr>
 *     <tr><td><code><b>"</b></code></td></tr>
 *     <tr><td><code><b>''</b></code></td></tr>
 *     <tr><td><code><b>).</b></code></td></tr>
 *   </table>
 *   </td>
 *
 *   <td valign='top' width='33%'>
 *   <table border="1" cellpadding="3" width='100%'>
 *     <tr><td><b>Impossible Penultimates</b></td></tr>
 *     <tr><td><i>any single letter</i></td></tr>
 *     <tr><td><i>personal and professional titles, ranks, etc.</i></td></tr>
 *     <tr><td><i>commas, colon, and quotes</i></td></tr>
 *     <tr><td><i>common abbreviations</i></td></tr>
 *     <tr><td><i>directions</i></td></tr>
 *     <tr><td><i>corporate designators</i></td></tr>
 *     <tr><td><i>times, months, etc.</i></td></tr>
 *     <tr><td><i>U.S. political parties</i></td></tr>
 *     <tr><td><i>U.S. states (not ME or IN)</i></td></tr>
 *     <tr><td><i>shipping terms</i></td></tr>
 *     <tr><td><i>address abbreviations</i></td></tr>
 *   </table>
 *   </td>
 *
 *   <td valign='top' width='33%'>
 *   <table border="1" cellpadding="3" width='100%'>
 *     <tr><td><b>Impossible Starts</b></td></tr>
 *     <tr><td><i>possible stops (see above)</i></td></tr>
 *     <tr><td><i>close parentheses</i></td></tr>
 *     <tr><td><code><b>,</b></code></td></tr>
 *     <tr><td><code><b>;</b></code></td></tr>
 *     <tr><td><code><b>:</b></code></td></tr>
 *     <tr><td><code><b>-</b></code></td></tr>
 *     <tr><td><code><b>--</b></code></td></tr>
 *     <tr><td><code><b>---</b></code></td></tr>
 *     <tr><td><code><b>%</b></code></td></tr>
 *   </table>
 *   </td>
 *
 * </tr>
 * </table>
 * </blockquote>
 *
 * Note that all of these sets are case insensitive.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class MyIndoEuropeanSentenceModel extends MyHeuristicSentenceModel {

    public MyIndoEuropeanSentenceModel(boolean firstWordMustBeCap) {
        super(
        		POSSIBLE_STOPS,
        		STOP_PATTERNS,
                IMPOSSIBLE_PENULTIMATES,
                IMPOSSIBLE_STARTS,
                true,false, firstWordMustBeCap);
      }

    private static final Set<String> POSSIBLE_STOPS = new HashSet<String>();
    static {
        POSSIBLE_STOPS.add(".");
        POSSIBLE_STOPS.add("..");  // abbrev + stop occurs
        POSSIBLE_STOPS.add("!");
        POSSIBLE_STOPS.add("?");
        POSSIBLE_STOPS.add(").");
        POSSIBLE_STOPS.add("\u00BB"); // french close quote
        POSSIBLE_STOPS.add(">>"); // french close quote
    }    
    
    private static final Set<String> STOP_PATTERNS = new HashSet<String>();
    static {
    	String endpunct = R.or(R.quote(new String[]{ ".", "..", "!", "?" }));
    	String endquote = R.or(R.quote(new String[]{ "''", "'", "\""}));
    	String mildWhitespace = "[ \\t]*[\\r\\n]?[ \\t]*";
    	STOP_PATTERNS.add( endpunct + mildWhitespace + endquote );
    	STOP_PATTERNS.add( endquote + mildWhitespace + endpunct ); // British-y convention
    }
    
    private static final Set<String> IMPOSSIBLE_STARTS
        = new HashSet<String>();
    static {
        IMPOSSIBLE_STARTS.add(",");
        IMPOSSIBLE_STARTS.add(")");
        IMPOSSIBLE_STARTS.add("]");
        IMPOSSIBLE_STARTS.add("}");
        IMPOSSIBLE_STARTS.add(">");
        IMPOSSIBLE_STARTS.add("<");
        IMPOSSIBLE_STARTS.add(".");
        IMPOSSIBLE_STARTS.add("!");
        IMPOSSIBLE_STARTS.add("?");
        IMPOSSIBLE_STARTS.add(":");
        IMPOSSIBLE_STARTS.add(";");
        IMPOSSIBLE_STARTS.add("-");
        IMPOSSIBLE_STARTS.add("--");
        IMPOSSIBLE_STARTS.add("---");
        IMPOSSIBLE_STARTS.add("%");
    }

    private static final Set<String> IMPOSSIBLE_PENULTIMATES
        = new HashSet<String>();
    static {
        // Non abbreviations which shouldn't be penultimate
        IMPOSSIBLE_PENULTIMATES.add(",");
        IMPOSSIBLE_PENULTIMATES.add(":");
        IMPOSSIBLE_PENULTIMATES.add("''");

        // Single letters; typically middle initials or parts of acronyms
        IMPOSSIBLE_PENULTIMATES.add("A");
        IMPOSSIBLE_PENULTIMATES.add("B");
        IMPOSSIBLE_PENULTIMATES.add("C");
        IMPOSSIBLE_PENULTIMATES.add("D");
        IMPOSSIBLE_PENULTIMATES.add("E");
        IMPOSSIBLE_PENULTIMATES.add("F");
        IMPOSSIBLE_PENULTIMATES.add("G");
        IMPOSSIBLE_PENULTIMATES.add("H");
        IMPOSSIBLE_PENULTIMATES.add("I");
        IMPOSSIBLE_PENULTIMATES.add("J");
        IMPOSSIBLE_PENULTIMATES.add("K");
        IMPOSSIBLE_PENULTIMATES.add("L");
        IMPOSSIBLE_PENULTIMATES.add("M");
        IMPOSSIBLE_PENULTIMATES.add("N");
        IMPOSSIBLE_PENULTIMATES.add("O");
        IMPOSSIBLE_PENULTIMATES.add("P");
        IMPOSSIBLE_PENULTIMATES.add("Q");
        IMPOSSIBLE_PENULTIMATES.add("R");
        IMPOSSIBLE_PENULTIMATES.add("S");
        IMPOSSIBLE_PENULTIMATES.add("T");
        IMPOSSIBLE_PENULTIMATES.add("U");
        IMPOSSIBLE_PENULTIMATES.add("V");
        IMPOSSIBLE_PENULTIMATES.add("W");
        IMPOSSIBLE_PENULTIMATES.add("X");
        IMPOSSIBLE_PENULTIMATES.add("Y");
        IMPOSSIBLE_PENULTIMATES.add("Z");

        // Common Abbrevs
        IMPOSSIBLE_PENULTIMATES.add("Bros");
        IMPOSSIBLE_PENULTIMATES.add("No");  // too common ??
        IMPOSSIBLE_PENULTIMATES.add("vs");
        // TODO allow as penult depending on cap status of next token, whether it's a legitimate sentence start
        IMPOSSIBLE_PENULTIMATES.add("etc");
        IMPOSSIBLE_PENULTIMATES.add("Fig"); // thanks to MM

        // French Abbrevs:
        IMPOSSIBLE_PENULTIMATES.add("T\u00E9l");  // e + accent aigu
        IMPOSSIBLE_PENULTIMATES.add("t\u00E9l");

        // Directional Abbrevs
        IMPOSSIBLE_PENULTIMATES.add("NE");
        IMPOSSIBLE_PENULTIMATES.add("N.E");
        IMPOSSIBLE_PENULTIMATES.add("NW");
        IMPOSSIBLE_PENULTIMATES.add("N.W");
        IMPOSSIBLE_PENULTIMATES.add("SE");
        IMPOSSIBLE_PENULTIMATES.add("S.E");
        IMPOSSIBLE_PENULTIMATES.add("SW");
        IMPOSSIBLE_PENULTIMATES.add("S.W");


        // Personal Honorifics
		IMPOSSIBLE_PENULTIMATES.add("Mr");
		IMPOSSIBLE_PENULTIMATES.add("Mrs");
		IMPOSSIBLE_PENULTIMATES.add("Ms");
		IMPOSSIBLE_PENULTIMATES.add("MM");
		IMPOSSIBLE_PENULTIMATES.add("Mssrs");
		IMPOSSIBLE_PENULTIMATES.add("Messrs");

        // Professional Honorifics
        IMPOSSIBLE_PENULTIMATES.add("Dr");
        IMPOSSIBLE_PENULTIMATES.add("Gov");
        IMPOSSIBLE_PENULTIMATES.add("Hon");
        IMPOSSIBLE_PENULTIMATES.add("Rev");
        IMPOSSIBLE_PENULTIMATES.add("Pres");
        IMPOSSIBLE_PENULTIMATES.add("Prof");
        IMPOSSIBLE_PENULTIMATES.add("Ph.D");
        IMPOSSIBLE_PENULTIMATES.add("Ph");
        IMPOSSIBLE_PENULTIMATES.add("Rep");
        IMPOSSIBLE_PENULTIMATES.add("Reps");
        IMPOSSIBLE_PENULTIMATES.add("Rev");
        IMPOSSIBLE_PENULTIMATES.add("Sen");
        IMPOSSIBLE_PENULTIMATES.add("Sens");


        // Name Suffixes
        IMPOSSIBLE_PENULTIMATES.add("Jr");
        IMPOSSIBLE_PENULTIMATES.add("Sr");

        // Military Ranks
        IMPOSSIBLE_PENULTIMATES.add("PFC");
        IMPOSSIBLE_PENULTIMATES.add("Cpl");
        IMPOSSIBLE_PENULTIMATES.add("Sgt");
        IMPOSSIBLE_PENULTIMATES.add("Lt");
        IMPOSSIBLE_PENULTIMATES.add("Lieut");
        IMPOSSIBLE_PENULTIMATES.add("Capt");
        IMPOSSIBLE_PENULTIMATES.add("Cpt");
        IMPOSSIBLE_PENULTIMATES.add("Maj");
        IMPOSSIBLE_PENULTIMATES.add("Gen");
        IMPOSSIBLE_PENULTIMATES.add("Col");
        IMPOSSIBLE_PENULTIMATES.add("Cmdr");
        IMPOSSIBLE_PENULTIMATES.add("Adm");

        IMPOSSIBLE_PENULTIMATES.add("Col");

        // Corporate Designators
        IMPOSSIBLE_PENULTIMATES.add("Co");
        IMPOSSIBLE_PENULTIMATES.add("Corp");
        IMPOSSIBLE_PENULTIMATES.add("Inc");
        IMPOSSIBLE_PENULTIMATES.add("Ltd");

        // Month Abbrevs
        IMPOSSIBLE_PENULTIMATES.add("Jan");
        IMPOSSIBLE_PENULTIMATES.add("Feb");
        IMPOSSIBLE_PENULTIMATES.add("Mar");
        IMPOSSIBLE_PENULTIMATES.add("Apr");
        IMPOSSIBLE_PENULTIMATES.add("Jun");
        IMPOSSIBLE_PENULTIMATES.add("Jul");
        IMPOSSIBLE_PENULTIMATES.add("Aug");
        IMPOSSIBLE_PENULTIMATES.add("Sep");
        IMPOSSIBLE_PENULTIMATES.add("Sept");
        IMPOSSIBLE_PENULTIMATES.add("Oct");
        IMPOSSIBLE_PENULTIMATES.add("Nov");
        IMPOSSIBLE_PENULTIMATES.add("Dec");

        // Location Suffixes
        IMPOSSIBLE_PENULTIMATES.add("St");

        // Political Parties
        IMPOSSIBLE_PENULTIMATES.add("Rep");
        IMPOSSIBLE_PENULTIMATES.add("Dem");

        // Politicians
        IMPOSSIBLE_PENULTIMATES.add("Atty");

        // State Names - Post Office
        // Source: http://www.usps.com/ncsc/lookups/usps_abbreviations.html#states
        IMPOSSIBLE_PENULTIMATES.add("AL");
        IMPOSSIBLE_PENULTIMATES.add("AK");
        IMPOSSIBLE_PENULTIMATES.add("AS");
        IMPOSSIBLE_PENULTIMATES.add("AZ");
        IMPOSSIBLE_PENULTIMATES.add("AR");
        IMPOSSIBLE_PENULTIMATES.add("CA");
        IMPOSSIBLE_PENULTIMATES.add("CO");
        IMPOSSIBLE_PENULTIMATES.add("CT");
        IMPOSSIBLE_PENULTIMATES.add("DE");
        IMPOSSIBLE_PENULTIMATES.add("DC");
        IMPOSSIBLE_PENULTIMATES.add("FM");
        IMPOSSIBLE_PENULTIMATES.add("FL");
        IMPOSSIBLE_PENULTIMATES.add("GA");
        IMPOSSIBLE_PENULTIMATES.add("GU");
        IMPOSSIBLE_PENULTIMATES.add("HI");
        IMPOSSIBLE_PENULTIMATES.add("ID");
        IMPOSSIBLE_PENULTIMATES.add("IL");
        // IMPOSSIBLE_PENULTIMATES.add("IN"); too common
        IMPOSSIBLE_PENULTIMATES.add("IA");
        IMPOSSIBLE_PENULTIMATES.add("KS");
        IMPOSSIBLE_PENULTIMATES.add("KY");
        IMPOSSIBLE_PENULTIMATES.add("LA");
        // IMPOSSIBLE_PENULTIMATES.add("ME");  too common
        IMPOSSIBLE_PENULTIMATES.add("MH");
        IMPOSSIBLE_PENULTIMATES.add("MD");
        IMPOSSIBLE_PENULTIMATES.add("MA");
        IMPOSSIBLE_PENULTIMATES.add("MI");
        IMPOSSIBLE_PENULTIMATES.add("MN");
        IMPOSSIBLE_PENULTIMATES.add("MS");
        IMPOSSIBLE_PENULTIMATES.add("MO");
        IMPOSSIBLE_PENULTIMATES.add("MT");
        IMPOSSIBLE_PENULTIMATES.add("NE");
        IMPOSSIBLE_PENULTIMATES.add("NV");
        IMPOSSIBLE_PENULTIMATES.add("NH");
        IMPOSSIBLE_PENULTIMATES.add("NJ");
        IMPOSSIBLE_PENULTIMATES.add("NM");
        IMPOSSIBLE_PENULTIMATES.add("NY");
        IMPOSSIBLE_PENULTIMATES.add("NC");
        IMPOSSIBLE_PENULTIMATES.add("ND");
        IMPOSSIBLE_PENULTIMATES.add("MP");
        IMPOSSIBLE_PENULTIMATES.add("OH");
        IMPOSSIBLE_PENULTIMATES.add("OK");
        IMPOSSIBLE_PENULTIMATES.add("OR");
        IMPOSSIBLE_PENULTIMATES.add("PW");
        IMPOSSIBLE_PENULTIMATES.add("PA");
        IMPOSSIBLE_PENULTIMATES.add("PR");
        IMPOSSIBLE_PENULTIMATES.add("RI");
        IMPOSSIBLE_PENULTIMATES.add("SC");
        IMPOSSIBLE_PENULTIMATES.add("SD");
        IMPOSSIBLE_PENULTIMATES.add("TN");
        IMPOSSIBLE_PENULTIMATES.add("TX");
        IMPOSSIBLE_PENULTIMATES.add("UT");
        IMPOSSIBLE_PENULTIMATES.add("VT");
        IMPOSSIBLE_PENULTIMATES.add("VI");
        IMPOSSIBLE_PENULTIMATES.add("VA");
        IMPOSSIBLE_PENULTIMATES.add("WA");
        IMPOSSIBLE_PENULTIMATES.add("WV");
        IMPOSSIBLE_PENULTIMATES.add("WI");
        IMPOSSIBLE_PENULTIMATES.add("WY");

        // shipping terms
        IMPOSSIBLE_PENULTIMATES.add("f.o.b");
        IMPOSSIBLE_PENULTIMATES.add("c.i.f");
        IMPOSSIBLE_PENULTIMATES.add("fob");
        IMPOSSIBLE_PENULTIMATES.add("cif");

        // times
        IMPOSSIBLE_PENULTIMATES.add("A.M");
        IMPOSSIBLE_PENULTIMATES.add("P.M");


        // state names - Chicago Manual & AP
        // source: www.nyu.edu/classes/copyXediting/STABBREV.html
        IMPOSSIBLE_PENULTIMATES.add("Ala");
        IMPOSSIBLE_PENULTIMATES.add("Ariz");
        IMPOSSIBLE_PENULTIMATES.add("Ark");
        IMPOSSIBLE_PENULTIMATES.add("Calif");
        IMPOSSIBLE_PENULTIMATES.add("Colo");
        IMPOSSIBLE_PENULTIMATES.add("Conn");
        IMPOSSIBLE_PENULTIMATES.add("Del");
        IMPOSSIBLE_PENULTIMATES.add("D.C");
        IMPOSSIBLE_PENULTIMATES.add("Fla");
        IMPOSSIBLE_PENULTIMATES.add("Ga");
        IMPOSSIBLE_PENULTIMATES.add("Ill");
        IMPOSSIBLE_PENULTIMATES.add("Ind");
        IMPOSSIBLE_PENULTIMATES.add("Kan");
        IMPOSSIBLE_PENULTIMATES.add("Kans");
        IMPOSSIBLE_PENULTIMATES.add("Ky");
        IMPOSSIBLE_PENULTIMATES.add("Md");
//        IMPOSSIBLE_PENULTIMATES.add("Mass"); //too common
        IMPOSSIBLE_PENULTIMATES.add("Mich");
        IMPOSSIBLE_PENULTIMATES.add("Minn");
        IMPOSSIBLE_PENULTIMATES.add("Miss");
        IMPOSSIBLE_PENULTIMATES.add("Mo");
        IMPOSSIBLE_PENULTIMATES.add("Mont");
        IMPOSSIBLE_PENULTIMATES.add("Neb");
        IMPOSSIBLE_PENULTIMATES.add("Nebr");
        IMPOSSIBLE_PENULTIMATES.add("Nev");
        IMPOSSIBLE_PENULTIMATES.add("N.H");
        IMPOSSIBLE_PENULTIMATES.add("N.J");
        IMPOSSIBLE_PENULTIMATES.add("N.M");
        IMPOSSIBLE_PENULTIMATES.add("N.Mex");
        IMPOSSIBLE_PENULTIMATES.add("N.Y");
        IMPOSSIBLE_PENULTIMATES.add("N.C");
        IMPOSSIBLE_PENULTIMATES.add("N.Dak");
        IMPOSSIBLE_PENULTIMATES.add("Okla");
        IMPOSSIBLE_PENULTIMATES.add("Ore");
        IMPOSSIBLE_PENULTIMATES.add("Oreg");
        IMPOSSIBLE_PENULTIMATES.add("Pa");
        IMPOSSIBLE_PENULTIMATES.add("R.I");
        IMPOSSIBLE_PENULTIMATES.add("S.C");
        IMPOSSIBLE_PENULTIMATES.add("S.Dak");
        IMPOSSIBLE_PENULTIMATES.add("Tenn");
        IMPOSSIBLE_PENULTIMATES.add("Tex");
        IMPOSSIBLE_PENULTIMATES.add("Tx");
        IMPOSSIBLE_PENULTIMATES.add("Vt");
        IMPOSSIBLE_PENULTIMATES.add("Va");
//        IMPOSSIBLE_PENULTIMATES.add("Wash");
        IMPOSSIBLE_PENULTIMATES.add("W.Va");
        IMPOSSIBLE_PENULTIMATES.add("Wis");
        IMPOSSIBLE_PENULTIMATES.add("Wisc");
        IMPOSSIBLE_PENULTIMATES.add("Wyo");
        IMPOSSIBLE_PENULTIMATES.add("Wyom");
        IMPOSSIBLE_PENULTIMATES.add("Amer");
        IMPOSSIBLE_PENULTIMATES.add("C.Z");
        IMPOSSIBLE_PENULTIMATES.add("P.R");
        IMPOSSIBLE_PENULTIMATES.add("V.I");

        // Location suffixes
        // Source: http://www.usps.com/ncsc/lookups/usps_abbreviations.html#states
        IMPOSSIBLE_PENULTIMATES.add("ALY");
        IMPOSSIBLE_PENULTIMATES.add("ANEX");
        IMPOSSIBLE_PENULTIMATES.add("ANNX");
        IMPOSSIBLE_PENULTIMATES.add("ANX");
        IMPOSSIBLE_PENULTIMATES.add("ARC");
        IMPOSSIBLE_PENULTIMATES.add("AV");
        IMPOSSIBLE_PENULTIMATES.add("AVE");
        IMPOSSIBLE_PENULTIMATES.add("AVEN");
        IMPOSSIBLE_PENULTIMATES.add("AVN");
        IMPOSSIBLE_PENULTIMATES.add("BCH");
        IMPOSSIBLE_PENULTIMATES.add("BG");
        IMPOSSIBLE_PENULTIMATES.add("BGS");
        IMPOSSIBLE_PENULTIMATES.add("BLF");
        IMPOSSIBLE_PENULTIMATES.add("BLFS");
        IMPOSSIBLE_PENULTIMATES.add("BLVD");
        IMPOSSIBLE_PENULTIMATES.add("BND");
        IMPOSSIBLE_PENULTIMATES.add("BOT");
        IMPOSSIBLE_PENULTIMATES.add("BOUL");
        IMPOSSIBLE_PENULTIMATES.add("BR");
        IMPOSSIBLE_PENULTIMATES.add("BRG");
        IMPOSSIBLE_PENULTIMATES.add("BRK");
        IMPOSSIBLE_PENULTIMATES.add("BRKS");
        IMPOSSIBLE_PENULTIMATES.add("BRNCH");
        IMPOSSIBLE_PENULTIMATES.add("BURG");
        IMPOSSIBLE_PENULTIMATES.add("BURGS");
        IMPOSSIBLE_PENULTIMATES.add("BYP");
        IMPOSSIBLE_PENULTIMATES.add("BYPA");
        IMPOSSIBLE_PENULTIMATES.add("BYPS");
        IMPOSSIBLE_PENULTIMATES.add("BYU");
        IMPOSSIBLE_PENULTIMATES.add("CANYN");
        IMPOSSIBLE_PENULTIMATES.add("CEN");
        IMPOSSIBLE_PENULTIMATES.add("CENT");
        IMPOSSIBLE_PENULTIMATES.add("CIR");
        IMPOSSIBLE_PENULTIMATES.add("CIRC");
        IMPOSSIBLE_PENULTIMATES.add("CK");
        IMPOSSIBLE_PENULTIMATES.add("CLB");
        IMPOSSIBLE_PENULTIMATES.add("CLF");
        IMPOSSIBLE_PENULTIMATES.add("CLFS");
        IMPOSSIBLE_PENULTIMATES.add("CMN");
        IMPOSSIBLE_PENULTIMATES.add("CMP");
        IMPOSSIBLE_PENULTIMATES.add("CNTR");
        IMPOSSIBLE_PENULTIMATES.add("COR");
        IMPOSSIBLE_PENULTIMATES.add("CORS");
        IMPOSSIBLE_PENULTIMATES.add("CP");
        IMPOSSIBLE_PENULTIMATES.add("CPE");
        IMPOSSIBLE_PENULTIMATES.add("CR");
        IMPOSSIBLE_PENULTIMATES.add("CRCL");
        IMPOSSIBLE_PENULTIMATES.add("CRES");
//        IMPOSSIBLE_PENULTIMATES.add("CRESCENT");
        IMPOSSIBLE_PENULTIMATES.add("CRK");
        IMPOSSIBLE_PENULTIMATES.add("CRSCNT");
        IMPOSSIBLE_PENULTIMATES.add("CRSE");
        IMPOSSIBLE_PENULTIMATES.add("CRSSNG");
        IMPOSSIBLE_PENULTIMATES.add("CRST");
        IMPOSSIBLE_PENULTIMATES.add("CRT");
        IMPOSSIBLE_PENULTIMATES.add("CSWY");
        IMPOSSIBLE_PENULTIMATES.add("CT");
        IMPOSSIBLE_PENULTIMATES.add("CTR");
        IMPOSSIBLE_PENULTIMATES.add("CTRS");
        IMPOSSIBLE_PENULTIMATES.add("CTS");
        IMPOSSIBLE_PENULTIMATES.add("CV");
        IMPOSSIBLE_PENULTIMATES.add("CVS");
        IMPOSSIBLE_PENULTIMATES.add("CYN");
        IMPOSSIBLE_PENULTIMATES.add("DIV");
        IMPOSSIBLE_PENULTIMATES.add("DL");
        IMPOSSIBLE_PENULTIMATES.add("DM");
        IMPOSSIBLE_PENULTIMATES.add("DR");
        IMPOSSIBLE_PENULTIMATES.add("DRS");
        IMPOSSIBLE_PENULTIMATES.add("DV");
        IMPOSSIBLE_PENULTIMATES.add("DVD");
        IMPOSSIBLE_PENULTIMATES.add("EST");
        IMPOSSIBLE_PENULTIMATES.add("ESTS");
        IMPOSSIBLE_PENULTIMATES.add("EXP");
        IMPOSSIBLE_PENULTIMATES.add("EXPR");
        IMPOSSIBLE_PENULTIMATES.add("EXPW");
        IMPOSSIBLE_PENULTIMATES.add("EXPY");
        IMPOSSIBLE_PENULTIMATES.add("EXT");
        IMPOSSIBLE_PENULTIMATES.add("EXTN");
        IMPOSSIBLE_PENULTIMATES.add("EXTNSN");
        IMPOSSIBLE_PENULTIMATES.add("EXTS");
        IMPOSSIBLE_PENULTIMATES.add("FLD");
        IMPOSSIBLE_PENULTIMATES.add("FLDS");
        IMPOSSIBLE_PENULTIMATES.add("FLS");
        IMPOSSIBLE_PENULTIMATES.add("FLT");
        IMPOSSIBLE_PENULTIMATES.add("FLTS");
        IMPOSSIBLE_PENULTIMATES.add("FRD");
        IMPOSSIBLE_PENULTIMATES.add("FRDS");
        IMPOSSIBLE_PENULTIMATES.add("FREEWY");
        IMPOSSIBLE_PENULTIMATES.add("FRG");
        IMPOSSIBLE_PENULTIMATES.add("FRGS");
        IMPOSSIBLE_PENULTIMATES.add("FRK");
        IMPOSSIBLE_PENULTIMATES.add("FRKS");
        IMPOSSIBLE_PENULTIMATES.add("FRRY");
        IMPOSSIBLE_PENULTIMATES.add("FRST");
        IMPOSSIBLE_PENULTIMATES.add("FRT");
        IMPOSSIBLE_PENULTIMATES.add("FRWAY");
        IMPOSSIBLE_PENULTIMATES.add("FRWY");
        IMPOSSIBLE_PENULTIMATES.add("FRY");
        IMPOSSIBLE_PENULTIMATES.add("FT");
        IMPOSSIBLE_PENULTIMATES.add("FWY");
        IMPOSSIBLE_PENULTIMATES.add("GARDN");
        IMPOSSIBLE_PENULTIMATES.add("GATEWY");
        IMPOSSIBLE_PENULTIMATES.add("GDN");
        IMPOSSIBLE_PENULTIMATES.add("GDNS");
        IMPOSSIBLE_PENULTIMATES.add("GLN");
        IMPOSSIBLE_PENULTIMATES.add("GLNS");
        IMPOSSIBLE_PENULTIMATES.add("GRDN");
        IMPOSSIBLE_PENULTIMATES.add("GRDNS");
        IMPOSSIBLE_PENULTIMATES.add("GRN");
        IMPOSSIBLE_PENULTIMATES.add("GRNS");
        IMPOSSIBLE_PENULTIMATES.add("GRV");
        IMPOSSIBLE_PENULTIMATES.add("GRVS");
        IMPOSSIBLE_PENULTIMATES.add("HARB");
        IMPOSSIBLE_PENULTIMATES.add("HBR");
        IMPOSSIBLE_PENULTIMATES.add("HGTS");
        IMPOSSIBLE_PENULTIMATES.add("HIWY");
        IMPOSSIBLE_PENULTIMATES.add("HL");
        IMPOSSIBLE_PENULTIMATES.add("HLLW");
        IMPOSSIBLE_PENULTIMATES.add("HLS");
        IMPOSSIBLE_PENULTIMATES.add("HT");
        IMPOSSIBLE_PENULTIMATES.add("HTS");
        IMPOSSIBLE_PENULTIMATES.add("HVN");
        IMPOSSIBLE_PENULTIMATES.add("HWAY");
        IMPOSSIBLE_PENULTIMATES.add("HWY");
        // IMPOSSIBLE_PENULTIMATES.add("IS"); // common
        IMPOSSIBLE_PENULTIMATES.add("ISLND");
        IMPOSSIBLE_PENULTIMATES.add("ISS");
        IMPOSSIBLE_PENULTIMATES.add("JCT");
        IMPOSSIBLE_PENULTIMATES.add("JCTN");
        IMPOSSIBLE_PENULTIMATES.add("JCTNS");
        IMPOSSIBLE_PENULTIMATES.add("JCTS");
        IMPOSSIBLE_PENULTIMATES.add("KNL");
        IMPOSSIBLE_PENULTIMATES.add("KY");
        IMPOSSIBLE_PENULTIMATES.add("KYS");
        IMPOSSIBLE_PENULTIMATES.add("LA");
        IMPOSSIBLE_PENULTIMATES.add("LCK");
        IMPOSSIBLE_PENULTIMATES.add("LCKS");
        IMPOSSIBLE_PENULTIMATES.add("LDG");
        IMPOSSIBLE_PENULTIMATES.add("LDGE");
        IMPOSSIBLE_PENULTIMATES.add("LF");
        IMPOSSIBLE_PENULTIMATES.add("LGT");
        IMPOSSIBLE_PENULTIMATES.add("LGTS");
        IMPOSSIBLE_PENULTIMATES.add("LK");
        IMPOSSIBLE_PENULTIMATES.add("LKS");
        IMPOSSIBLE_PENULTIMATES.add("LN");
        IMPOSSIBLE_PENULTIMATES.add("LNDG");
        IMPOSSIBLE_PENULTIMATES.add("LNDNG");
        IMPOSSIBLE_PENULTIMATES.add("MDW");
        IMPOSSIBLE_PENULTIMATES.add("MDWS");
        IMPOSSIBLE_PENULTIMATES.add("MISSN");
        IMPOSSIBLE_PENULTIMATES.add("ML");
        IMPOSSIBLE_PENULTIMATES.add("MLS");
        IMPOSSIBLE_PENULTIMATES.add("MNR");
        IMPOSSIBLE_PENULTIMATES.add("MNRS");
        IMPOSSIBLE_PENULTIMATES.add("MNT");
        IMPOSSIBLE_PENULTIMATES.add("MNTN");
        IMPOSSIBLE_PENULTIMATES.add("MNTNS");
        IMPOSSIBLE_PENULTIMATES.add("MSN");
        IMPOSSIBLE_PENULTIMATES.add("MSSN");
        IMPOSSIBLE_PENULTIMATES.add("MT");
        IMPOSSIBLE_PENULTIMATES.add("MTN");
        IMPOSSIBLE_PENULTIMATES.add("MTNS");
        IMPOSSIBLE_PENULTIMATES.add("MTWY");
        IMPOSSIBLE_PENULTIMATES.add("NCK");
        IMPOSSIBLE_PENULTIMATES.add("OPAS");
        IMPOSSIBLE_PENULTIMATES.add("ORCH");
        IMPOSSIBLE_PENULTIMATES.add("OVL");
        IMPOSSIBLE_PENULTIMATES.add("PK");
        IMPOSSIBLE_PENULTIMATES.add("PKWAY");
        IMPOSSIBLE_PENULTIMATES.add("PKWY");
        IMPOSSIBLE_PENULTIMATES.add("PKWYS");
        IMPOSSIBLE_PENULTIMATES.add("PKY");
        IMPOSSIBLE_PENULTIMATES.add("PL");
        IMPOSSIBLE_PENULTIMATES.add("PLN");
        IMPOSSIBLE_PENULTIMATES.add("PLNS");
        IMPOSSIBLE_PENULTIMATES.add("PLZ");
        IMPOSSIBLE_PENULTIMATES.add("PLZA");
        IMPOSSIBLE_PENULTIMATES.add("PNE");
        IMPOSSIBLE_PENULTIMATES.add("PNES");
        IMPOSSIBLE_PENULTIMATES.add("PR");
        IMPOSSIBLE_PENULTIMATES.add("PRK");
        IMPOSSIBLE_PENULTIMATES.add("PRR");
        IMPOSSIBLE_PENULTIMATES.add("PRT");
        IMPOSSIBLE_PENULTIMATES.add("PRTS");
        IMPOSSIBLE_PENULTIMATES.add("PSGE");
        IMPOSSIBLE_PENULTIMATES.add("PT");
        IMPOSSIBLE_PENULTIMATES.add("PTS");
        IMPOSSIBLE_PENULTIMATES.add("RAD");
        IMPOSSIBLE_PENULTIMATES.add("RADL");
        IMPOSSIBLE_PENULTIMATES.add("RAMP");
        IMPOSSIBLE_PENULTIMATES.add("RD");
        IMPOSSIBLE_PENULTIMATES.add("RDG");
        IMPOSSIBLE_PENULTIMATES.add("RDGE");
        IMPOSSIBLE_PENULTIMATES.add("RDGS");
        IMPOSSIBLE_PENULTIMATES.add("RDS");
        IMPOSSIBLE_PENULTIMATES.add("RIV");
        IMPOSSIBLE_PENULTIMATES.add("RIVR");
        IMPOSSIBLE_PENULTIMATES.add("RNCH");
        IMPOSSIBLE_PENULTIMATES.add("RNCHS");
        IMPOSSIBLE_PENULTIMATES.add("RPD");
        IMPOSSIBLE_PENULTIMATES.add("RPDS");
        IMPOSSIBLE_PENULTIMATES.add("RST");
        IMPOSSIBLE_PENULTIMATES.add("RTE");
        IMPOSSIBLE_PENULTIMATES.add("RVR");
        IMPOSSIBLE_PENULTIMATES.add("SHL");
        IMPOSSIBLE_PENULTIMATES.add("SHLS");
        IMPOSSIBLE_PENULTIMATES.add("SHR");
        IMPOSSIBLE_PENULTIMATES.add("SHRS");
        IMPOSSIBLE_PENULTIMATES.add("SKWY");
        IMPOSSIBLE_PENULTIMATES.add("SMT");
        IMPOSSIBLE_PENULTIMATES.add("SPG");
        IMPOSSIBLE_PENULTIMATES.add("SPGS");
        IMPOSSIBLE_PENULTIMATES.add("SPNG");
        IMPOSSIBLE_PENULTIMATES.add("SQ");
        IMPOSSIBLE_PENULTIMATES.add("SQR");
        IMPOSSIBLE_PENULTIMATES.add("SQRE");
        IMPOSSIBLE_PENULTIMATES.add("SQRS");
        IMPOSSIBLE_PENULTIMATES.add("SQS");
        IMPOSSIBLE_PENULTIMATES.add("SQU");
        IMPOSSIBLE_PENULTIMATES.add("ST");
        IMPOSSIBLE_PENULTIMATES.add("STA");
        IMPOSSIBLE_PENULTIMATES.add("STATN");
        IMPOSSIBLE_PENULTIMATES.add("STN");
        IMPOSSIBLE_PENULTIMATES.add("STR");
        IMPOSSIBLE_PENULTIMATES.add("STRA");
        IMPOSSIBLE_PENULTIMATES.add("STRM");
        IMPOSSIBLE_PENULTIMATES.add("STRT");
        IMPOSSIBLE_PENULTIMATES.add("STRVN");
        IMPOSSIBLE_PENULTIMATES.add("STS");
        IMPOSSIBLE_PENULTIMATES.add("TER");
        IMPOSSIBLE_PENULTIMATES.add("TERR");
        IMPOSSIBLE_PENULTIMATES.add("TPK");
        IMPOSSIBLE_PENULTIMATES.add("TPKE");
        IMPOSSIBLE_PENULTIMATES.add("TR");
        IMPOSSIBLE_PENULTIMATES.add("TRAK");
        IMPOSSIBLE_PENULTIMATES.add("TRCE");
        IMPOSSIBLE_PENULTIMATES.add("TRFY");
        IMPOSSIBLE_PENULTIMATES.add("TRK");
        IMPOSSIBLE_PENULTIMATES.add("TRKS");
        IMPOSSIBLE_PENULTIMATES.add("TRL");
        IMPOSSIBLE_PENULTIMATES.add("TRLS");
        IMPOSSIBLE_PENULTIMATES.add("TRNPK");
        IMPOSSIBLE_PENULTIMATES.add("TRPK");
        IMPOSSIBLE_PENULTIMATES.add("TRWY");
        IMPOSSIBLE_PENULTIMATES.add("TUNL");
        IMPOSSIBLE_PENULTIMATES.add("TUNLS");
        IMPOSSIBLE_PENULTIMATES.add("TUNNL");
        IMPOSSIBLE_PENULTIMATES.add("TURNPK");
        IMPOSSIBLE_PENULTIMATES.add("UN");
        IMPOSSIBLE_PENULTIMATES.add("UNS");
        IMPOSSIBLE_PENULTIMATES.add("UPAS");
        IMPOSSIBLE_PENULTIMATES.add("VDCT");
        IMPOSSIBLE_PENULTIMATES.add("VIA");
        IMPOSSIBLE_PENULTIMATES.add("VILL");
        IMPOSSIBLE_PENULTIMATES.add("VILLE");
        IMPOSSIBLE_PENULTIMATES.add("VILLG");
        IMPOSSIBLE_PENULTIMATES.add("VIS");
        IMPOSSIBLE_PENULTIMATES.add("VIST");
        IMPOSSIBLE_PENULTIMATES.add("VISTA");
        IMPOSSIBLE_PENULTIMATES.add("VL");
        IMPOSSIBLE_PENULTIMATES.add("VLG");
        IMPOSSIBLE_PENULTIMATES.add("VLGS");
        IMPOSSIBLE_PENULTIMATES.add("VLLY");
        IMPOSSIBLE_PENULTIMATES.add("VLY");
        IMPOSSIBLE_PENULTIMATES.add("VLYS");
        IMPOSSIBLE_PENULTIMATES.add("VST");
        IMPOSSIBLE_PENULTIMATES.add("VSTA");
        IMPOSSIBLE_PENULTIMATES.add("VW");
        IMPOSSIBLE_PENULTIMATES.add("VWS");
        IMPOSSIBLE_PENULTIMATES.add("WL");
        IMPOSSIBLE_PENULTIMATES.add("WLS");
        IMPOSSIBLE_PENULTIMATES.add("WY");
        IMPOSSIBLE_PENULTIMATES.add("XING");
        IMPOSSIBLE_PENULTIMATES.add("XRD");

        // corporate designators
        // source: http://www.ldc.upenn.edu/Catalog/CatalogList/LDC2001T02/resources.corp-desigs-ref-list
        IMPOSSIBLE_PENULTIMATES.add("AB");
        IMPOSSIBLE_PENULTIMATES.add("A.B");
        IMPOSSIBLE_PENULTIMATES.add("AE");
        IMPOSSIBLE_PENULTIMATES.add("A.E.");
        IMPOSSIBLE_PENULTIMATES.add("AENP");
        IMPOSSIBLE_PENULTIMATES.add("AG");
        IMPOSSIBLE_PENULTIMATES.add("AG&COKG");
        IMPOSSIBLE_PENULTIMATES.add("AG");
        IMPOSSIBLE_PENULTIMATES.add("Co");
        IMPOSSIBLE_PENULTIMATES.add("KG");
        IMPOSSIBLE_PENULTIMATES.add("AL");
        IMPOSSIBLE_PENULTIMATES.add("A/L");
        IMPOSSIBLE_PENULTIMATES.add("AMBA");
        IMPOSSIBLE_PENULTIMATES.add("A.M.B.A.");
        IMPOSSIBLE_PENULTIMATES.add("AO");
        IMPOSSIBLE_PENULTIMATES.add("A.O");
        IMPOSSIBLE_PENULTIMATES.add("APS");
        IMPOSSIBLE_PENULTIMATES.add("A&P");
//        IMPOSSIBLE_PENULTIMATES.add("AS");
        IMPOSSIBLE_PENULTIMATES.add("A.S");
        IMPOSSIBLE_PENULTIMATES.add("A/S");
        IMPOSSIBLE_PENULTIMATES.add("AY");
        IMPOSSIBLE_PENULTIMATES.add("BA");
        IMPOSSIBLE_PENULTIMATES.add("B.A");
        IMPOSSIBLE_PENULTIMATES.add("BHD");
        IMPOSSIBLE_PENULTIMATES.add("BM");
        IMPOSSIBLE_PENULTIMATES.add("B.M");
        IMPOSSIBLE_PENULTIMATES.add("BSC");
        IMPOSSIBLE_PENULTIMATES.add("BV");
        IMPOSSIBLE_PENULTIMATES.add("B.V");
        IMPOSSIBLE_PENULTIMATES.add("BVBA");
        IMPOSSIBLE_PENULTIMATES.add("B.V.B.A");
        IMPOSSIBLE_PENULTIMATES.add("BVCV");
        IMPOSSIBLE_PENULTIMATES.add("B.V");
        IMPOSSIBLE_PENULTIMATES.add("C.V");
        IMPOSSIBLE_PENULTIMATES.add("B.V./C.V");
        IMPOSSIBLE_PENULTIMATES.add("CA");
        IMPOSSIBLE_PENULTIMATES.add("C.A");
        IMPOSSIBLE_PENULTIMATES.add("CA");
        IMPOSSIBLE_PENULTIMATES.add("Cia.");
        IMPOSSIBLE_PENULTIMATES.add("CDERL");
        IMPOSSIBLE_PENULTIMATES.add("CV");
        IMPOSSIBLE_PENULTIMATES.add("C.V");
        IMPOSSIBLE_PENULTIMATES.add("CO");
        IMPOSSIBLE_PENULTIMATES.add("CORP");
        IMPOSSIBLE_PENULTIMATES.add("CPORA");
        IMPOSSIBLE_PENULTIMATES.add("CPT");
        IMPOSSIBLE_PENULTIMATES.add("EC");
        IMPOSSIBLE_PENULTIMATES.add("E.C");
        IMPOSSIBLE_PENULTIMATES.add("EG");
        IMPOSSIBLE_PENULTIMATES.add("EGMBH");
        IMPOSSIBLE_PENULTIMATES.add("EPE");
        IMPOSSIBLE_PENULTIMATES.add("E.P.E");
        IMPOSSIBLE_PENULTIMATES.add("GMBH");
        IMPOSSIBLE_PENULTIMATES.add("Ges.m.b.H");
        IMPOSSIBLE_PENULTIMATES.add("GBR");
        IMPOSSIBLE_PENULTIMATES.add("GGMBH");
        IMPOSSIBLE_PENULTIMATES.add("GGMBH");
        IMPOSSIBLE_PENULTIMATES.add("GMK");
        IMPOSSIBLE_PENULTIMATES.add("GM.K");
        IMPOSSIBLE_PENULTIMATES.add("G.M.B.H");
        IMPOSSIBLE_PENULTIMATES.add("CO,KG");
        IMPOSSIBLE_PENULTIMATES.add("GP");
        IMPOSSIBLE_PENULTIMATES.add("G.P");
        IMPOSSIBLE_PENULTIMATES.add("GSK");
        IMPOSSIBLE_PENULTIMATES.add("HF");
        IMPOSSIBLE_PENULTIMATES.add("H.F");
        IMPOSSIBLE_PENULTIMATES.add("HMIG");
        IMPOSSIBLE_PENULTIMATES.add("H.MIJ");
        IMPOSSIBLE_PENULTIMATES.add("H.MIG");
        IMPOSSIBLE_PENULTIMATES.add("HVER");
        IMPOSSIBLE_PENULTIMATES.add("H.VER");
        IMPOSSIBLE_PENULTIMATES.add("INC");
        // IMPOSSIBLE_PENULTIMATES.add("IS"); // too common
        IMPOSSIBLE_PENULTIMATES.add("I/S");
        IMPOSSIBLE_PENULTIMATES.add("KB");
        IMPOSSIBLE_PENULTIMATES.add("KG");
        IMPOSSIBLE_PENULTIMATES.add("KGAA");
        IMPOSSIBLE_PENULTIMATES.add("K.G.A.A");
        IMPOSSIBLE_PENULTIMATES.add("KK");
        IMPOSSIBLE_PENULTIMATES.add("KS");
        IMPOSSIBLE_PENULTIMATES.add("K/S");
        IMPOSSIBLE_PENULTIMATES.add("KY");
        IMPOSSIBLE_PENULTIMATES.add("LDA");
        IMPOSSIBLE_PENULTIMATES.add("LTD");
        IMPOSSIBLE_PENULTIMATES.add("LTDAPS");
        IMPOSSIBLE_PENULTIMATES.add("LLC");
        IMPOSSIBLE_PENULTIMATES.add("L.L.C");
        IMPOSSIBLE_PENULTIMATES.add("LP");
        IMPOSSIBLE_PENULTIMATES.add("L.P");
        IMPOSSIBLE_PENULTIMATES.add("LTDA");
        IMPOSSIBLE_PENULTIMATES.add("MIJ");
        IMPOSSIBLE_PENULTIMATES.add("NL");
        IMPOSSIBLE_PENULTIMATES.add("N.L");
        IMPOSSIBLE_PENULTIMATES.add("NPL");
        IMPOSSIBLE_PENULTIMATES.add("N.P.L");
        IMPOSSIBLE_PENULTIMATES.add("NV");
        IMPOSSIBLE_PENULTIMATES.add("N.V");
        IMPOSSIBLE_PENULTIMATES.add("OHG");
        IMPOSSIBLE_PENULTIMATES.add("OE");
        IMPOSSIBLE_PENULTIMATES.add("O.E");
        IMPOSSIBLE_PENULTIMATES.add("OY");
        IMPOSSIBLE_PENULTIMATES.add("OYAB");
        IMPOSSIBLE_PENULTIMATES.add("PERJAN");
        IMPOSSIBLE_PENULTIMATES.add("PERSERO");
        IMPOSSIBLE_PENULTIMATES.add("PERUM");
        IMPOSSIBLE_PENULTIMATES.add("PLC");
        IMPOSSIBLE_PENULTIMATES.add("PN");
        IMPOSSIBLE_PENULTIMATES.add("PP");
        IMPOSSIBLE_PENULTIMATES.add("PT");
        IMPOSSIBLE_PENULTIMATES.add("PTY");
        IMPOSSIBLE_PENULTIMATES.add("PTE");
        IMPOSSIBLE_PENULTIMATES.add("PVBA");
        IMPOSSIBLE_PENULTIMATES.add("P.V.B.A");
        IMPOSSIBLE_PENULTIMATES.add("SA");
        IMPOSSIBLE_PENULTIMATES.add("S.A");
        IMPOSSIBLE_PENULTIMATES.add("SA");
        IMPOSSIBLE_PENULTIMATES.add("SA");
        IMPOSSIBLE_PENULTIMATES.add("SAC");
        IMPOSSIBLE_PENULTIMATES.add("S.A.C");
        IMPOSSIBLE_PENULTIMATES.add("SACA");
        IMPOSSIBLE_PENULTIMATES.add("S.A.C.A");
        IMPOSSIBLE_PENULTIMATES.add("SACC");
        IMPOSSIBLE_PENULTIMATES.add("S.Acc");
        IMPOSSIBLE_PENULTIMATES.add("ACC");
        IMPOSSIBLE_PENULTIMATES.add("SACCPA");
        IMPOSSIBLE_PENULTIMATES.add("S.ACC.P.A");
        IMPOSSIBLE_PENULTIMATES.add("ACC.P.A");
        IMPOSSIBLE_PENULTIMATES.add("P.A");
        IMPOSSIBLE_PENULTIMATES.add("SACEI");
        IMPOSSIBLE_PENULTIMATES.add("S.A.");
        IMPOSSIBLE_PENULTIMATES.add("C.E.I");
        IMPOSSIBLE_PENULTIMATES.add("SACIF");
        IMPOSSIBLE_PENULTIMATES.add("S.A.C.I.F");
        IMPOSSIBLE_PENULTIMATES.add("SADECV");
        IMPOSSIBLE_PENULTIMATES.add("S.A.");
        IMPOSSIBLE_PENULTIMATES.add("C.V");
        IMPOSSIBLE_PENULTIMATES.add("SAIC");
        IMPOSSIBLE_PENULTIMATES.add("S.A.I.C");
        IMPOSSIBLE_PENULTIMATES.add("SAICA");
        IMPOSSIBLE_PENULTIMATES.add("S.A.I.C.A");
        IMPOSSIBLE_PENULTIMATES.add("SALC");
        IMPOSSIBLE_PENULTIMATES.add("S.A.L.C");
        IMPOSSIBLE_PENULTIMATES.add("SALC");
        IMPOSSIBLE_PENULTIMATES.add("SANV");
        IMPOSSIBLE_PENULTIMATES.add("S.A.N.V");
        IMPOSSIBLE_PENULTIMATES.add("SARL");
        IMPOSSIBLE_PENULTIMATES.add("S.A.R.L");
        IMPOSSIBLE_PENULTIMATES.add("SAS");
        IMPOSSIBLE_PENULTIMATES.add("S.A.S");
        IMPOSSIBLE_PENULTIMATES.add("SCI");
        IMPOSSIBLE_PENULTIMATES.add("S.C.I");
        IMPOSSIBLE_PENULTIMATES.add("SCl");
        IMPOSSIBLE_PENULTIMATES.add("S.C.L");
        IMPOSSIBLE_PENULTIMATES.add("SCP");
        IMPOSSIBLE_PENULTIMATES.add("S.C.P");
        IMPOSSIBLE_PENULTIMATES.add("SCPA");
        IMPOSSIBLE_PENULTIMATES.add("S.C.p.A");
        IMPOSSIBLE_PENULTIMATES.add("SCPDEG");
        IMPOSSIBLE_PENULTIMATES.add("S.C.P");
        IMPOSSIBLE_PENULTIMATES.add("SCRL");
        IMPOSSIBLE_PENULTIMATES.add("SDERL");
        IMPOSSIBLE_PENULTIMATES.add("R.L");
        IMPOSSIBLE_PENULTIMATES.add("R.L");
        IMPOSSIBLE_PENULTIMATES.add("SDERLDECV");
        IMPOSSIBLE_PENULTIMATES.add("C.V");
        IMPOSSIBLE_PENULTIMATES.add("SENC");
        IMPOSSIBLE_PENULTIMATES.add("SENCPORA");
        IMPOSSIBLE_PENULTIMATES.add("SEND");
        IMPOSSIBLE_PENULTIMATES.add("SICI");
        IMPOSSIBLE_PENULTIMATES.add("S.I.C.I");
        IMPOSSIBLE_PENULTIMATES.add("SL");
        IMPOSSIBLE_PENULTIMATES.add("S.L.");
        IMPOSSIBLE_PENULTIMATES.add("SMA");
        IMPOSSIBLE_PENULTIMATES.add("S.M.A");
        IMPOSSIBLE_PENULTIMATES.add("SMCP");
        IMPOSSIBLE_PENULTIMATES.add("S.M.C.P");
        IMPOSSIBLE_PENULTIMATES.add("SNC");
        IMPOSSIBLE_PENULTIMATES.add("S.N.C");
        IMPOSSIBLE_PENULTIMATES.add("SPA");
        IMPOSSIBLE_PENULTIMATES.add("S.P.A");
        IMPOSSIBLE_PENULTIMATES.add("SPRL");
        IMPOSSIBLE_PENULTIMATES.add("S.P.R.L");
        IMPOSSIBLE_PENULTIMATES.add("SRL");
        IMPOSSIBLE_PENULTIMATES.add("S.R.L");
        IMPOSSIBLE_PENULTIMATES.add("SV");
        IMPOSSIBLE_PENULTIMATES.add("SZRL");
        IMPOSSIBLE_PENULTIMATES.add("S.Z.R.L");
        IMPOSSIBLE_PENULTIMATES.add("TAS");
        IMPOSSIBLE_PENULTIMATES.add("T.A.S");
        IMPOSSIBLE_PENULTIMATES.add("UPA");
        IMPOSSIBLE_PENULTIMATES.add("U.p.a");
        IMPOSSIBLE_PENULTIMATES.add("UNIV");
        IMPOSSIBLE_PENULTIMATES.add("VN");
        IMPOSSIBLE_PENULTIMATES.add("WLL");
        IMPOSSIBLE_PENULTIMATES.add("W.L.L");
    }

}
