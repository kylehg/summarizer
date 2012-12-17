""" lex rank summarizer written by hawka """

from os import listdir
from os.path import join
from subprocess import call
from nltk.tokenize import sent_tokenize, word_tokenize

def ls(path):
    return [join(path, item) for item in listdir(path)]

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

# TODO
