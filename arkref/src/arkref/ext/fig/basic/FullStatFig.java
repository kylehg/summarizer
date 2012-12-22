package arkref.ext.fig.basic;

import java.util.*;

/**
 * For keeping track of statistics.
 * Keeps all the data around (can be memory expensive).
 */
public class FullStatFig extends BigStatFig {
  public FullStatFig() { }
  public FullStatFig(Iterable<Double> c) {
    for(double x : c) add(x);
  }
  public void add(double x) {
    super.add(x);
    data.add(x);
  }

  public double entropy() {
    double e = 0;
    for(double x : data) {
      x /= sum;
      if(x > 0)
        e += -x * Math.log(x);
    }
    return e;
  }

  public double variance() {
    double v = 0;
    double m = mean();
    for(double x : data)
      v += (x-m)*(x-m);
    return v/n;
  }
  public double stddev() { return Math.sqrt(variance()); }

  public List<Double> getData() { return data; }

  // Return for each lag, the correlation
  public double[] computeAutocorrelation(int maxLag) {
    double mean = mean();
    double stddev = stddev();
    double[] normData = new double[n];
    for(int i = 0; i < n; i++)
      normData[i] = (data.get(i) - mean) / stddev;
    double[] autocorrelations = new double[maxLag+1];
    for(int lag = 0; lag <= maxLag; lag++) {
      double sum = 0;
      int count = 0;
      for(int i = 0; i+lag < n; i++) {
        sum += normData[i] * normData[i+lag];
        count++;
      }
      autocorrelations[lag] = sum / count;
    }
    return autocorrelations;
  }

  public String toString() {
    return Fmt.D(min) + "/ << " + Fmt.D(mean()) + "~" + Fmt.D(stddev()) + " >> /" + Fmt.D(max) + " (" + n + ")";
  }

  private ArrayList<Double> data = new ArrayList<Double>();
}
