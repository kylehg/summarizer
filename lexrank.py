"""LexPageRank, a PageRank-inspired algorithm for generating multidocument.
sentence summaries."""

import itertools
from centrality import gen_summary_from_rankings
from utils import *
from rouge import gen_config


# The minimum similarity for sentences to be considered similar by LexPageRank.
# TODO: tune these
MIN_LEXPAGERANK_SIM = 0.2
EPSILON = 0.0001

def sim_adj_matrix(sents, min_sim=MIN_LEXPAGERANK_SIM):
    """Compute the adjacency matrix of a list of tokenized sentences,
    with an edjge if the sentences are above a given similarity."""
    # TODO: Implement TF-IDF and use that.
    # TODO: This does twice as much work as necessary - use symmetry to avoid.
    return [[1 if cosine_sim(s1, s2, binary_vectorize) > min_sim else 0
             for s2 in sents]
            for s1 in sents]


def normalize_matrix(matrix):
    """Given a matrix of number values, normalize them so that a row
    sums to 1."""
    for i, row in enumerate(matrix):
        tot = sum(row)
        matrix[i] = [x / tot for x in row]
    return matrix


def pagerank(matrix, d=0.85):
    """Given a matrix of values, run the PageRank algorithm on them
    until the values converge. See Wikipedia page for source."""
    t = 0
    matrix1 = map(lambda row: map(lambda x: 0, row), matrix)
    n = len(matrix)
    rank = [1.0 / n] * n
    new_rank =  [0.0] * n
    while not has_converged(rank, new_rank):
        rank = new_rank
        new_rank = [(((1.0-d) / n) +
                     d * sum((rank[i] * sim) for sim in row))
                    for row in matrix]
    return rank


def has_converged(x, y, epsilon=EPSILON):
    """Are all the elements in x are within epsilon of their y's?"""
    for a, b in itertools.izip(x, y):
        if abs(a - b) > epsilon:
            return False
    return True


def gen_lexrank_summary(orig_sents, max_words):
    tok_sents = [tokenize.word_tokenize(orig_sent)
                 for orig_sent in orig_sents]
    adj_matrix = normalize_matrix(sim_adj_matrix(tok_sents))
    rank = pagerank(adj_matrix)
    return gen_summary_from_rankings(rank, tok_sents, orig_sents, max_words)



###############################################################################
if __name__ == '__main__':
    # Gen summaries
    collections = get_collections()[:1]
    sums = []
    for i, (docs, models, baseline) in enumerate(collections):
        collection = os.path.dirname(docs[0])
        sum_name = 'summary%02d.txt' % i
        collection_sents = get_sentences(collection)
        summary = ' '.join(gen_lexrank_summary(collection_sents, 100))
        with open('rouge/lexrank/' + sum_name, 'w') as f:
            f.write(summary)
        sums.append((sum_name, map(os.path.basename, models)))
    gen_config('lexrank', 'rouge/lexrank-config.xml', 'lexrank',
               'models', sums)

