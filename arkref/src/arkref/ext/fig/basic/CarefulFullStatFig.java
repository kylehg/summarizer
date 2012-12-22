package arkref.ext.fig.basic;

import java.util.*;

/**
 * Handle NaN's and infinities separately.
 */
public class CarefulFullStatFig extends FullStatFig {
  private int numNaN, numPosInf, numNegInf;

  public CarefulFullStatFig() { }
  public CarefulFullStatFig(Iterable<Double> c) {
    super(c);
  }

  public int numNaN() { return numNaN; }
  public int numPosInf() { return numPosInf; }
  public int numNegInf() { return numNegInf; }

  public void add(double x) {
    if(Double.isNaN(x)) numNaN++;
    else if(x == Double.NEGATIVE_INFINITY) numNegInf++;
    else if(x == Double.POSITIVE_INFINITY) numPosInf++;
    else super.add(x);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    if(numNaN > 0) sb.append(" NaN:"+numNaN);
    if(numPosInf > 0) sb.append(" +Inf:"+numPosInf);
    if(numNegInf > 0) sb.append(" -Inf:"+numNegInf);
    return sb.toString();
  }
}
