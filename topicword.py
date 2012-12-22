""" topic word summarizer written by hawka """

from subprocess import call
from nltk.tokenize import word_tokenize, line_tokenize
from nltk.probability import FreqDist

import utils

def generate_topic_words(dir_path, topic_file):
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
    # commands to generate ts files... commented out because i prefer manual
    # topics_cmd = "java -Xmx1000m TopicSignatures config.example"
    # call(topics_cmd, shell=True)

def load_topic_words(topic_file):
    """ given a path to a .ts file returns a dictionary of type { string : float }
    mapping topic words to their chi square scores """
    topic_words_dict = dict()
    raw = open(topic_file).read()
    lines = line_tokenize(raw)
    for line in lines: # no cutoff outside of TopicS 0.1
        pair = line.split(" ")
        topic_words_dict[pair[0]] = float(pair[1])
    return topic_words_dict

def count_topicwords(sentence, tw_dict):
    tw_count = 0
    for word in word_tokenize(sentence):
        if word in tw_dict.keys():
            tw_count += 1
    return tw_count

def generate_sentence_dict(all_sents, tw_dict):
    stopwords = open("TopicWords-v1/stoplist-smart-sys.txt").readlines()
    sent_dict = dict()
    for sent in all_sents:
        # scoring number one
        # sent_dict[sent] = count_topicwords(sent, tw_dict)
        # scoring number two
        # sent_dict[sent] = count_topicwords(sent, tw_dict)/len(word_tokenize(sent))
        # scoring number three
        wordcount = 0
        for word in word_tokenize(sent):
            if word not in stopwords:
                wordcount += 1
        sent_dict[sent] =  count_topicwords(sent, tw_dict)/wordcount
    return sent_dict

def generate_summary(topic_file, to_summarize):
    all_sents = utils.get_sentences(to_summarize)
    tw_dict = load_topic_words(topic_file)
    sent_dict = generate_sentence_dict(all_sents, tw_dict)
    top_sents = sorted(sent_dict.items(), key=lambda t: t[1], reverse=True)
    pretty = []
    for sent in top_sents:
        if not utils.is_repeat(sent[0], pretty):
            pretty.append(sent[0])
    # return 100 words
    return " ".join(word_tokenize(" ".join(pretty))[:100])

if __name__ == '__main__':
    for file in utils.ls("input/"):
        name = file.split("/")[-1][0:7]
        if name[0] != ".":
            summary = generate_summary("tsfiles/" + name + ".ts", file)
            sumfile = open("tw-summaries/" + name, "w")
            sumfile.write(summary)

