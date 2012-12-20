"""LexPageRank, a PageRank-inspired algorithm for generating multidocument.
sentence summaries."""

import itertools

from utils import *


# The minimum similarity for sentences to be considered similar by LexPageRank.
MIN_LEXPAGERANK_SIM = 0.2
EPSILON = 0.001

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


def pagerank(matrix):
    """Given a matrix of values, run the PageRank algorithm on them
    until the values converge."""
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


def has_converged(x, y, epsilon=EPSILON):
    """Are all the elements in x are within epsilon of y."""
    for a, b in itertoos.izip(x, y):
        if math.abs(a - b) > epsilon:
            return False
    return True


###############################################################################
if __name__ == '__main__':
    pass
