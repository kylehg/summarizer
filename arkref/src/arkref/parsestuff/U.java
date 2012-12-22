package arkref.parsestuff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import arkref.analysis.ARKref;

/** general utilities for which it is convenient to have very short abbreviations **/
public class U {

	public static String readFile(String filename) throws FileNotFoundException { 
		File file = new File(filename);
		return new Scanner(file).useDelimiter("\\Z").next();
	}
	public static String readFile(File file) throws FileNotFoundException { 
		return new Scanner(file).useDelimiter("\\Z").next();
	}
	public static void writeFile(String text, String file) throws IOException {
		writeFile(text, new File(file));
	}

	public static void writeFile(String text, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
	}
	
	public static String backslashEscape(String s) {
		s = s.replace("\\", "\\\\");
		s = s.replace("\n", "\\n");
		s = s.replace("\r", "\\r");
		s = s.replace("\t", "\\t");
		return s;
	}

	public static <T> void pl(T s) {  if (ARKref.showDebug()) {  System.out.println(s);  }  }
	public static void pf(String pat) {  if (ARKref.showDebug()) {  System.out.printf(pat);  }  }

	public static <A> void pf(String pat, A a0) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0);  }  }
	public static <A> String sf(String pat, A a0) {  return String.format(pat, a0);  }
	public static <A,B> void pf(String pat, A a0, B a1) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1);  }  }
	public static <A,B> String sf(String pat, A a0, B a1) {  return String.format(pat, a0, a1);  }
	public static <A,B,C> void pf(String pat, A a0, B a1, C a2) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2);  }  }
	public static <A,B,C> String sf(String pat, A a0, B a1, C a2) {  return String.format(pat, a0, a1, a2);  }
	public static <A,B,C,D> void pf(String pat, A a0, B a1, C a2, D a3) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3);  }  }
	public static <A,B,C,D> String sf(String pat, A a0, B a1, C a2, D a3) {  return String.format(pat, a0, a1, a2, a3);  }
	public static <A,B,C,D,E> void pf(String pat, A a0, B a1, C a2, D a3, E a4) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4);  }  }
	public static <A,B,C,D,E> String sf(String pat, A a0, B a1, C a2, D a3, E a4) {  return String.format(pat, a0, a1, a2, a3, a4);  }
	public static <A,B,C,D,E,F> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5);  }  }
	public static <A,B,C,D,E,F> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5) {  return String.format(pat, a0, a1, a2, a3, a4, a5);  }
	public static <A,B,C,D,E,F,G> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6);  }  }
	public static <A,B,C,D,E,F,G> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6);  }
	public static <A,B,C,D,E,F,G,H> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7);  }  }
	public static <A,B,C,D,E,F,G,H> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7);  }
	public static <A,B,C,D,E,F,G,H,I> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8);  }  }
	public static <A,B,C,D,E,F,G,H,I> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8);  }
	public static <A,B,C,D,E,F,G,H,I,J> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9);  }  }
	public static <A,B,C,D,E,F,G,H,I,J> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9);  }
	public static <A,B,C,D,E,F,G,H,I,J,K> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17, S a18) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17, S a18) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18);  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T> void pf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17, S a18, T a19) {  if (ARKref.showDebug()) {  System.out.printf(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19);  }  }
	public static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T> String sf(String pat, A a0, B a1, C a2, D a3, E a4, F a5, G a6, H a7, I a8, J a9, K a10, L a11, M a12, N a13, O a14, P a15, Q a16, R a17, S a18, T a19) {  return String.format(pat, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19);  }

}
