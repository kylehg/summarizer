package arkref.ext.fig.basic;
//package fig.basic;
//
//import java.io.*;
//import java.util.*;
//import java.util.concurrent.*;
//
//import fig.basic.*;
//import fig.exec.*;
//import fig.prob.*;
//import fig.record.*;
//import static fig.basic.LogInfo.*;
//
//public class Parallelizer<T> {
//  public interface Processor<T> {
//    public void process(T x, int i, int n, boolean log);
//  }
//
//  int numThreads;
//  private Thread primaryThread;
//
//  public Parallelizer(int numThreads) {
//    this.numThreads = numThreads;
//  }
//
//  public void process(final List<T> points, final Processor<T> processor) {
//    // Loop over examples in parallel
//    final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
//    final Ref<Throwable> exception = new Ref(null);
//    primaryThread = null;
//    for(int i = 0; i < points.size(); i++) {
//      final int I = i;
//      final T x = points.get(i);
//      executor.execute(new Runnable() {
//        public void run() {
//          if(Execution.shouldBail()) return;
//          try {
//            if(exception.value == null) {
//              setPrimaryThread();
//              processor.process(x, I, points.size(), isPrimary());
//            }
//          } catch(Throwable t) {
//            exception.value = t; // Save exception
//          }
//        }
//      });
//    }
//    executor.shutdown();
//    try {
//      while(!executor.awaitTermination(1, TimeUnit.SECONDS));
//    } catch(InterruptedException e) {
//      throw Exceptions.bad("Interrupted");
//    }
//    if(exception.value != null) throw new RuntimeException(exception.value);
//  }
//
//  public synchronized void setPrimaryThread() {
//    if(primaryThread == null) primaryThread = Thread.currentThread();
//  }
//  public synchronized boolean isPrimary() { return Thread.currentThread() == primaryThread; }
//}
