package arkref.ext.fig.basic;

import java.io.Serializable;

public class IntBoolPair implements Serializable {
  private static final long serialVersionUID = 42;

  public IntBoolPair() { }
  public IntBoolPair(int first, boolean second) {
    this.first = first;
    this.second = second;
  }

  public String toString() {
    return first + "," + second;
  }

	public int hashCode() { return 29*first + (second ? 1 : 0); }
  public boolean equals(Object o) {
    IntBoolPair p = (IntBoolPair)o;
    return first == p.first && second == p.second;
  }

  public int first;
  public boolean second;
}
