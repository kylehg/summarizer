cd $(dirname $0)
set -x

java -server -mx1g -cp bin:arkref.jar:lib/stanford-parser-2008-10-26.jar arkref.parsestuff.StanfordParserServer lib/englishPCFG.ser.gz &
#java -server -mx1g -cp bin:arkref.jar:lib/stanford-parser-2008-10-26.jar arkref.parsestuff.StanfordParserServer lib/englishFactored.ser.gz &
java -Xmx500m -cp lib/supersense-tagger.jar edu.cmu.ark.SuperSenseTaggerServer  --port 5557 --model config/superSenseModelAllSemcor.ser.gz --properties config/arkref.properties &


wait
