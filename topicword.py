""" topic word summarizer written by hawka """

from os import listdir
from os.path import join
from subprocess import call
from nltk.tokenize import sent_tokenize, word_tokenize, line_tokenize
from nltk.probability import FreqDist

def ls(path):
    return [join(path, item) for item in listdir(path)]

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
    for line in lines:
        pair = line.split(" ")
        # no extra cutoff (0.1) CHANGE
        topic_words_dict[pair[0]] = float(pair[1])
    return topic_words_dict

def load_file_sents(path):
    return [sent.lower() for sent in sent_tokenize(open(path).read())]

def load_collection_sents(path):
    sents = []
    for file in ls(path):
        sents.extend(load_file_sents(file))
    return sents

def get_sentences(path): 
    """ loads sentences from the given path (collection or file) """
    sents = []
    try:
        # treat as a single file
        open(path).read()
        sents = load_file_sents(path)
    except IOError:
        # it's a directory!
        sents = load_collection_sents(path)
    return sents

def count_topicwords(sentence, tw_dict):
    tw_count = 0
    for word in word_tokenize(sentence):
        if word in tw_dict.keys():
            tw_count += 1
    return tw_count

def generate_sentence_dict(all_sents, tw_dict):
    sent_dict = dict()
    for sent in all_sents:
        # scoring number one CHANGE
        sent_dict[sent] = count_topicwords(sent, tw_dict)
    return sent_dict

def generate_summary(topic_file, to_summarize):
    all_sents = get_sentences(to_summarize)
    tw_dict = load_topic_words(topic_file)
    sent_dict = generate_sentence_dict(all_sents, tw_dict)
    # TODO add redundancy removal
    top_sents = sorted(sent_dict.items(), key=lambda t: t[1], reverse=True)
    pretty = []
    for sent in top_sents:
        pretty.append(sent[0])
    # return 100 words
    return " ".join(word_tokenize(" ".join(pretty))[:100])
