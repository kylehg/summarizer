package arkref.ext.fig.basic;

import java.io.Serializable;

public class IntTriple implements Serializable {
  private static final long serialVersionUID = 42;

  public IntTriple() { }
  public IntTriple(int first, int second, int third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public String toString() {
    return first + "," + second + "," + third;
  }

	public int hashCode() { return 17*(29*first + second) + third; }
  public boolean equals(Object o) {
    IntTriple p = (IntTriple)o;
    return first == p.first && second == p.second && third == p.third;
  }

  public int first, second, third;
}
