#!/bin/bash

grep = | ruby -e '
module Enumerable
  def sum
    inject(nil) { |sum,x| sum ? sum+x : x }
  end
  def mean
    return nil if size == 0
    sum * 1.0 / size
  end
end

precs = []
recs = []
for line in STDIN
  if line =~ /Precision = (\S+)/
    precs << $1.to_f
  elsif line =~ /Recall = (\S+)/
    recs << $1.to_f
  else
    next
  end
end

puts "Avg Doc Precision = %.3f" % [precs.mean]
puts "Avg Doc Recall = %.3f" % [recs.mean]
precs.size==recs.size or raise "wtf"
fs = (0...precs.size).map{|i|
  f1 = 2 * precs[i] * recs[i] *1.0 / (precs[i] + recs[i])
  if f1.nan?
    0
  else
    f1
  end
}.compact

puts "Avg Doc F1 = %.3f" % [fs.mean]
'
