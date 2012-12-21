import os
from subprocess import call

def generate_config(dir_path, topic_file):
    configcontents = ("==== Do not change these values ====" + "\n"
                     "stopFilePath = stoplist-smart-sys.txt" + "\n"
                     "performStemming = N" + "\n"
                     "backgroundCorpusFreqCounts = bgCounts-Giga.txt" + "\n"
                     "topicWordCutoff = 0.1" + "\n" + "\n"
                     "==== Directory to compute topic words on ====" + "\n"
                     "inputDir = " + dir_path + "\n" + "\n"
                     "==== Output File ====" + "\n"
                     "outputFile = " + topic_file + "\n")
    configfile = open("config.example", "w")
    configfile.write(configcontents)

def ls(path):
    return [os.path.join(path, item) for item in os.listdir(path)]

if __name__ == '__main__':
    cmd = "java -Xmx1000m TopicSignatures config.example"
    for file in ls("../input"):
        generate_config(file, "../tsfiles/" + file[-11:-4] + ".ts")
        call(cmd, shell=True)
