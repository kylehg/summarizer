package arkref.ext.fig.basic;

import java.util.*;

/**
 * For keeping track of statistics.
 * Just keeps average, sum, min, max.
 */
public class BigStatFig extends StatFig {
  public BigStatFig() { min = Double.POSITIVE_INFINITY; max = Double.NEGATIVE_INFINITY; }
  public void add(double x) {
    super.add(x);
    min = Math.min(min, x);
    max = Math.max(max, x);
  }
  public String toString() {
    if(min == Double.POSITIVE_INFINITY) return "NaN (0)";
    return Fmt.D(min) + "/ << " + Fmt.D(mean()) + " >> /" + Fmt.D(max) + " (" + n + ")";
  }

  public double getMin() { return min; }
  public double getMax() { return max; }
  public double range() { return max-min; }

  double min, max;
}
