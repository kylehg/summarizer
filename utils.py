"""Common utilities for working with text, etc."""

import os
import math
from nltk import tokenize
from memoizer import memoized


PROJECT_ROOT = '/home1/c/cis530/final_project'
INPUT_ROOT = PROJECT_ROOT + '/input'
MODELS_ROOT = PROJECT_ROOT + '/models'
BASELINE_ROOT = PROJECT_ROOT + '/baseline'

STEMMED_IDF_FILE = PROJECT_ROOT + '/global/bgIdfValues.unstemmed.txt'
UNSTEMMED_IDF_FILE = PROJECT_ROOT + '/global/bgIdfValues.unstemmed.txt'

# The max and min word count to consider for a summary sentance.
MIN_SENT_LEN = 10
MAX_SENT_LEN = 35

# The maximum similarity between two sentences that one should be
# considered a duplicate of the other.
MAX_SIM_CUTOFF = 0.3



# Token and document utils
# ------------------------

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


def get_toks(path):
    return [tokenize.word_tokenize(sent) for sent in get_sentences(path)]



def get_collections():
    """Return a list of tuples of (documents, summaries, baselines)
    for each collection."""
    docs = sorted(ls(collection) for collection in ls(INPUT_ROOT))
    models = sorted(ls(collection) for collection in ls(MODELS_ROOT))
    baselines = sorted(ls(BASELINE_ROOT))
    return zip(docs, models, baselines)


# Vectors and similarities
# ------------------------

def cosine_sim(x, y, vect_fun=None):
    """Return the cosine similarity between two vectors, defined as:

    (sum over X, Y of (x * w)) /
    sqrt(sum over X of x^2) * sqrt(sum over Y of y^2)

    If a vectorize function is provided, assumes that x, y are a list 
    of tokens and compares by vectorizing with the given vector function.
    """
    if vect_fun:
        feat_space = feature_space(x, y)
        x, y = vect_fun(feat_space, x), vect_fun(feat_space, y)

    assert len(x) == len(y), 'Vectors are not the same length.'
    zipped = zip(x, y)
    top = float(sum(v * w for v, w in zipped))
    bot = (math.sqrt(sum(pow(v, 2) for v in x))
           * math.sqrt(sum(pow(w, 2) for w in y)))
    try:
        return top / bot
    except ZeroDivisionError:
        return top / 0.00001


def binary_vectorize(feature_space, doc):
    """Given a set of words as a feature space and a tokenized document,
    return a (binary) vector representation of that document."""
    return [1 if point in doc else 0 for point in feature_space]


def freq_vectorize(feature_space, doc):
    freqs = defaultdict(lambda: 0)
    for word in doc:
        freqs[word] += 1
    return [freq[point] if point in freqs else 0
            for point in feature_space]

@memoized
def load_idf_weights():
    f = open(UNSTEMMED_IDF_FILE, 'r')
    f.readline() # Ignore first line
    return {line.spilt()[0]: line.split()[1] for line in f}


def tfidf_vectorize(feature_space, doc):
    idfs = load_idf_weights()
    return [freq * idfs[point] if point in idfs else 0
            for freq, point in zip(freq_vectorize, feature_space)]


def feature_space(doc1, doc2):
    """Given two lists of tokens, return a common feature set."""
    return sorted(set(doc1) | set(doc2))


# Summarizer utils
# ----------------

def is_valid_sent_len(sent, min_len=MIN_SENT_LEN, max_len=MAX_SENT_LEN):
    """Takes a list of tokens, returns if valid token length."""
    return min_len <= len(sent) <= max_len


def is_repeat(sent, sents, vect_fun=tfidf_vectorize, max_sim=MAX_SIM_CUTOFF):
    """Given a tokenized sentence and a list of tokenized sentences,
    return whether the sentences overlaps too highly in content with any
    of the others."""
    # TODO: Incorporate synonyms to better discern similarity
    for other_sent in sents:
        feat_space = feature_space(sent, other_sent)
        x, y = vect_fun(feat_space, sent), vect_fun(feat_space, other_sent)
        if cosine_sim(x, y) > max_sim:
            return True
    return False


if __name__ == '__main__':
    print get_collections()[0]
