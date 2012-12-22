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

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * An <code>IndoEuropeanTokenizerFactory</code> creates tokenizers
 * with built-in support for alpha-numerics, numbers, and other
 * common constructs in Indo-European langauges.
 *
 * <p>The tokenization rules
 * are roughly based on those used in MUC-6, but are necessarily finer
 * grained, because the MUC tokenizers were based on lexical and
 * semantic information such as whether a string was an abbreviation.
 *
 * <p>A token is any sequence of characters satisfying one of the
 * following patterns.
 *
 * <blockquote>
 * <table cellpadding="5" border="1">
 *   <tr> <td width="25%"><b>Pattern</b></td>
 <td width="75%"><b>Description</b></td> </tr>
 *   <tr> <td>AlphaNumeric</td>
 <td>Any sequence of upper or lowercase letters or digits,
 as defined by {@link Character#isDigit(char)} and
 {@link Character#isLetter(char)}, and including the
 Devanagari characters
 (unicode <code>0x0900</code> to <code>0x097F</code>)</td>
 </tr>
 <tr> <td>Numerical</td>
 <td>Any sequence of numbers, commas, and periods.</td>
 </tr>
 <tr> <td>Hyphen Sequence</td>
 <td>Any number of hyphens (<code>-</code>)</td>
 </tr>
 <tr> <td>Equals Sequence</td>
 <td>Any number of equals signs (<code>=</code>)</td>
 </tr>
 <tr> <td>Double Quotes</td>
 <td>Double forward quotes (<code>``</code>) or
 double backward quotes(<code>''</code>)
 </tr>
 * </table>
 * </blockquote>
 *
 * Whitespaces are defined as any sequence of whitespace characters,
 * including the unicode non-breakable space (unicode
 * <code>160</code>).  The tokenizer operates in a longest-leftmost
 * fashion, returning the longest possible token starting at the
 * current position in the underlying character array.  </p>
 *
 * <h3>Thread Safety</h3>
 *
 * The Indo-European tokenizer factory is completely thread safe.
 *
 * <h3>Singleton</h3>
 *
 * All instances of Indo-European tokenizer factories behave the same
 * way.  Because they are thread safe, use the singleton {@link
 * #INSTANCE} instead of constructing a fresh instance.
 * 
 * <h3>Serialization and Compilation</h3>
 *
 * <p>The serialized and compiled versions of this class deserialize
 * to a new instance and the factory instance respectively.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public class MyIndoEuropeanTokenizerFactory
    implements Compilable, TokenizerFactory, Serializable {

    static final long serialVersionUID = -5608280781322140944L;


    /**
     * The singleton instance of an Indo-European tokenizer factory.
     */
    public static final TokenizerFactory INSTANCE
        = new MyIndoEuropeanTokenizerFactory();

    /**
     * An instance of an Indo-European tokenizer factory.   This
     * is the same instance as provided by {@link #INSTANCE}.
     *
     * @deprecated Use {@link #INSTANCE} instead.
     */
    @Deprecated
    public static final TokenizerFactory FACTORY
        = INSTANCE;


    /**
     * Construct a tokenizer for Indo-European languages.
     *
     * <p><i>Implementation Note:</i> All Indo-European tokenizer
     * factories behave the same way, and they are thread safe, so the
     * constant {@link #FACTORY} may be used anywhere a freshly
     * constructed character tokenizer factory is used, without loss
     * of performance.
     *
     * @deprecated Use singleton instance {@link #INSTANCE} instead.
     */
    @Deprecated
    public MyIndoEuropeanTokenizerFactory() {
        /* do nothing */
    }

    /**
     * Returns a tokenizer for Indo-European for the specified
     * subsequence of characters.
     *
     * @param ch Characters to tokenize.
     * @param start Index of first character to tokenize.
     * @param length Number of characters to tokenize.
     */
    public Tokenizer tokenizer(char[] ch, int start, int length) {
        return new MyIndoEuropeanTokenizer(ch,start,length);
    }

    /**
     * Returns tha name of this class.
     *
     * @return The name of this class.
     */
    @Override public String toString() {
    return getClass().getName();
    }

    /**
     * Compiles this tokenizer factory to the specified object output.
     * The tokenizer factory read back in is reference identical
     * to the static constant {@link #FACTORY}.
     *
     * @param objOut Object output to which this tokenizer factory is
     * compiled.
     * @throws IOException If there is an I/O error during the write.
     * @deprecated Use the {@link Serializable} interface instead.
     */
    @Deprecated
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer());
    }

    private static class Externalizer extends AbstractExternalizable {
        static final long serialVersionUID = 3826670589236636230L;
        public Externalizer() {
            /* do nothing */
        }
        @Override
        public void writeExternal(ObjectOutput objOut) {
            /* do nothing */
        }
        @Override
        public Object read(ObjectInput objIn) { 
            return FACTORY; 
        }
    }
}
