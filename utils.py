"""Some utilities for working with text, etc."""

import os
from nltk import tokenize


def ls(path):
    return [os.path.join(path, item) for item in os.listdir(path)]


def load_file_sents(path):
    return [sent.lower()
            for sent in tokenize.sent_tokenize(open(path).read())]


def load_collection_sents(path):
    sents = []
    for f in ls(path):
        sents.extend(load_file_sents(f))
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
