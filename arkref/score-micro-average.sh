#!/bin/bash


# these actually work but not using
# P=`grep -E '[0-9]+/[0-9]+ *bad' $1 | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`
# R=`grep -E '[0-9]+/[0-9]+ *missing' $1 | sed 's/\// /' | awk '{num+=$1; denom+=$2} END{print 1-num/denom}'`
# 
# echo "" | awk "END{print \"PRECISION:\t$P\nRECALL:\t$R\nF1:\t\"($P*$R*2)/($P+$R)\"\n\"}"

zless $1 | grep '^Pairwise Eval' | gawk '{print $3,$4,$5}'|perl -pe 's/\S+=//g' | gawk '
  {tp+=$1; fp+=$2; fn+=$3}  
END { 
prec=tp/(tp+fp); rec=tp/(tp+fn)
print "PRECISION: ", prec
print "RECALL:    ",rec
print "F1:        ", 2*prec*rec/(prec+rec)
}'
