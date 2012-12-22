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

/**
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   LingPipe1.0
 */
class MyIndoEuropeanTokenizer extends Tokenizer {

    private final char[] mChars;
    private final int mLastPosition;
    private final int mStartPosition;

    private int mPosition;
    private int mTokenStart;
    private int mLastTokenIndex;
    
    private int mLastTokenStartPosition = -1;
    private int mLastTokenEndPosition = -1;

    /**
     * Construct a tokenizer from the specified character range.  The
     * characters are not copied, so they should not be modified during
     * tokenization.
     *
     * @param ch Characters to tokenize.
     * @param offset Index of first character to tokenize.
     * @param length Number of characters to tokenize.
     * @throws IllegalArgumentException If the slice parameters are
     * out of bounds.
     */
    public MyIndoEuropeanTokenizer(char[] ch, int offset, int length) {
        if (offset < 0 || offset + length > ch.length) {
            String msg = "Illegal slice."
                + " cs.length=" + ch.length
                + " offset=" + offset
                + " length=" + length;
            throw new IllegalArgumentException(msg);
        }
        mChars = ch;
        mPosition = offset;
        mLastPosition = offset+length;
        mTokenStart = -1;
        mLastTokenIndex = -1;
        mStartPosition = offset;
    }

    /**
     * Creates a tokenizer from the specified string.
     *
     * @param chars Characters to tokenize.
     */
    public MyIndoEuropeanTokenizer(String chars) {
        this(chars.toCharArray(),0,chars.length());
    }

    /**
     * Create a tokenizer from the specified string buffer.  The
     * contents of the buffer are copied, so modifications to the
     * buffer do not affect tokenization.
     *
     * @param chars String buffer whose characters are tokenized.
     */
    public MyIndoEuropeanTokenizer(StringBuilder chars) {
        this(chars.toString());
    }

    @Override
    public int lastTokenStartPosition() {
        return mLastTokenStartPosition;
    }

    @Override
    public int lastTokenEndPosition() {
        return mLastTokenEndPosition;
    }


    /**
     * Returns the next whitespace.  Returns the same result for
     * subsequent calls without a call to <code>nextToken</code>.
     *
     * @return The next space.
     */
    @Override
    public String nextWhitespace() {
        StringBuilder sb = new StringBuilder();
        while (hasMoreCharacters()
               && Character.isWhitespace(currentChar())) {
            sb.append(currentChar());
            ++mPosition;
        }
        return sb.toString();
    }

    /**
     * Returns <code>true</code> if the specified character is a
     * letter as determined by {@link Character#isLetter(char)} or is
     * a Devanagari character in the unicode range <code>0x0900</code>
     * to <code>0x097F</code>.
     *
     * @param c Character to test.
     * @return <code>true</code> if the character is a Java letter or
     * a Devanagari character.
     */
    private static boolean isLetter(char c) {
        return Character.isLetter(c) || devanagari(c);
    }

    /**
     * Returns <code>true</code> if the specified character is in the
     * Devanagari range, unicode <code>0x0900</code> to
     * <code>0x097F</code>, inclusive.
     *
     * @param code Code number to test.
     * @return <code>true</code> if
     */
    private static boolean devanagari(char unicode) {
        return (unicode >= 0x0900 && unicode <= 0x097F);
    }

    /**
     * Returns the next token in the stream, or <code>null</code> if
     * there are no more tokens.  Flushes any whitespace that has
     * not been returned.
     *
     * @return The next token, or <code>null</code> if there are no
     * more tokens.
     */
    @Override
    public String nextToken()  {
        skipWhitespace();
        if (!hasMoreCharacters()) return null;
        mTokenStart = mPosition;
        ++mLastTokenIndex;
        char startChar = mChars[mPosition++];
        // update to deal with initial period digits properly
        if (startChar == '.') {
            while (currentCharEquals('.')) ++mPosition;
            return currentToken();
        }
        if (startChar == '-') {
            while (currentCharEquals('-')) ++mPosition;
            return currentToken();
        }
        if (startChar == '=') {
            while (currentCharEquals('=')) ++mPosition;
            return currentToken();
        }
        if (startChar == '\'') {
            if (currentCharEquals('\'')) ++mPosition;
            return currentToken();
        }
        if (startChar == '`') {
            if (currentCharEquals('`')) ++mPosition;
            return currentToken();
        }
        if (isLetter(startChar)) return alphaNumToken();
        if (Character.isDigit(startChar)) return numToken();
        return currentToken(); // other single character symbol
    }

    /**
     * Returns <code>true</code> if there are more characters
     * in the input character sequence.
     *
     * @return <code>true</code> if there are more characters
     * to be tokenized.
     */
    private boolean hasMoreCharacters() {
        return mPosition < mLastPosition;
    }

    /**
     * Returns the character in the underlying sequence at
     * the current position.
     *
     * @return The character in the underlying sequence at
     * the current position.
     */
    private char currentChar() {
        return mChars[mPosition];
    }

    /**
     * Returns <code>true</code> if there are more characters and the
     * current character is equal to the specified character.
     *
     * @param c Character to test.
     * @return <code>true</code> if the current character is equal to
     * the specified character.
     */
    private boolean currentCharEquals(char c) {
        return hasMoreCharacters() && currentChar() == c;
    }

    /**
     * Advances the position to the first character of the
     * next token, or to the end of the file if there are
     * no more tokens.
     */
    private void skipWhitespace()  {
        while (hasMoreCharacters()
               && Character.isWhitespace(currentChar()))
            ++mPosition;
    }

    /**
     * Returns the current token as a string.
     *
     * @return Current token as a string.
     */
    private String currentToken() {
        mLastTokenStartPosition = mTokenStart - mStartPosition;
        mLastTokenEndPosition = mPosition - mStartPosition;
        return new String(mChars,mTokenStart,mPosition-mTokenStart);
    }

    /**
     * Completes and returns a token that begins with the previous
     * letter character.
     *
     * @return Longest token extending the previous character.
     */
    private String alphaNumToken() {
        while (hasMoreCharacters()
               && (isLetter(currentChar())
                   || Character.isDigit(currentChar()))) ++mPosition;
        return currentToken();
    }


    /**
     * Completes and returns a token that begins with the previous
     * digit character.
     *
     * @return Token beginning at previous character, and extending
     * to all subsequent digits, commas, and periods.
     */
    private String numToken() {
        while (hasMoreCharacters()) {
            if (isLetter(currentChar())) {
                ++mPosition;
                return alphaNumToken();
            }
            if (Character.isDigit(currentChar())) {
                ++mPosition;
                continue;
            }
            if (currentChar() == '.' || currentChar() == ',') {
                return numPunctToken();
            }
            return currentToken();
        }
        return currentToken();
    }

    /**
     * Completes and returns a token that begins with previous
     * numbers and commas or periods.
     *
     * @return Token beginning at previous character, and extending
     * to all subsequent digits, commas, and periods.
     */
    private String numPunctToken() {
        while (hasMoreCharacters()) {
            if (Character.isDigit(currentChar())) {
                ++mPosition;
            } else if (currentChar() == '.'
                       || currentChar() == ',') {
                ++mPosition;
                if (!hasMoreCharacters() || !Character.isDigit(currentChar())) {
                    --mPosition;
                    return currentToken();
                }
            } else {
                return currentToken();
            }
        }
        return currentToken();
    }

    /**
     * Returns a tokenized version of the specified string.
     *
     * @param phrase Characters to tokenize.
     * @return Array of tokens generated by characters.
     */
    public static String[] tokenize(String phrase) {
        return new MyIndoEuropeanTokenizer(phrase).tokenize();
    }

}
