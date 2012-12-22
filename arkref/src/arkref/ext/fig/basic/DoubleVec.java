package arkref.ext.fig.basic;

import java.util.*;

public class DoubleVec {
  public DoubleVec() {
    this.data = new double[0];
    this.n = 0;
  }
  public DoubleVec(int cap) {
    this.data = new double[cap];
    this.n = 0;
  }

  public DoubleVec(double[] data) {
    this.data = data.clone();
    this.n = data.length;
  }

  public double get(int i) {
    if(i >= n) throw new ArrayIndexOutOfBoundsException();
    return data[i];
  }
  public double set(int i, double x) {
    if(i >= n) throw new ArrayIndexOutOfBoundsException();
    data[i] = x;
    return x;
  }
  // Set, but grow the array if necessary
  public double setGrow(int i, double x) {
    if(i >= n) {
      if(i >= data.length) setCap((i+1)*2);
      n = i+1;
    }
    data[i] = x;
    return x;
  }
  // Append an element
  public void add(double x) { setGrow(n, x); }

  public void multAll(double d) {
    for(int i = 0; i < n; i++)
      data[i] *= d;
  }

  // Set the capacity of the array
  public void setCap(int cap) {
    if(cap < n) throw new ArrayIndexOutOfBoundsException();
    double[] newData = new double[cap];
    System.arraycopy(data, 0, newData, 0, n);
    data = newData;
  }
  public void trimToSize() { setCap(n); }
  public int size() { return n; }

  private double[] data;
  private int n;

  /*public static void main(String[] args) {
    int n = Integer.parseInt(args[0]);
    int numTimes = Integer.parseInt(args[1]);
    String sel = args[2];
    for(int j = 0; j < numTimes; j++) {
      if(sel.equals("i")) {
        IntVec vec = new IntVec(n);
        for(int i = 0; i < n; i++)
          vec.add(i);
        int z = 0;
        for(int i = 0; i < vec.size(); i++) {
          z += vec.get(i);
          vec.set(i, 0);
        }
      }
      else if(sel.equals("r")) {
        int[] vec = new int[n];
        for(int i = 0; i < n; i++)
          vec[i] = i;
        int z = 0;
        for(int i = 0; i < vec.length; i++) {
          z += vec[i];
          vec[i] = 0;
        }
      }
      else {
        ArrayList<Integer> vec = new ArrayList<Integer>(n);
        for(int i = 0; i < n; i++)
          vec.add(i);
        int z = 0;
        for(int i = 0; i < vec.size(); i++) {
          z += vec.get(i);
          vec.set(i, 0);
        }
      }
    }
  }*/
}
