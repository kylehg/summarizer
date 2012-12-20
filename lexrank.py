"""LexPageRank, a graph-based document summarizer."""

from utils import *


# The minimum similarity for sentences to be considered similar by LexPageRank.
MIN_LEXPAGERANK_SIM = 0.2


def sim_adj_matrix(sents, min_sim=MIN_LEXPAGERANK_SIM):
    """Compute the adjacency matrix of a list of tokenized sentences,
    with an edjge if the sentences are above a given similarity."""
    # TODO: Implement TF-IDF and use that.
    # TODO: This does twice as much work as necessary - use symmetry to avoid.
    return [[1 if cosine_sim(s1, s2, vect_fun=binary_vectorize) > min_sim
             else 0
             for s2 in sents]
            for s1 in sents]


###############################################################################
if __name__ == '__main__':
    pass
