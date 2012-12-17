"""
A Centrality Summarizer
Author: Kyle Hardgrave (kyleh@seas)
"""


def centrality(sents):
    """Calculate the "centrality," or average similarity, of each vector
    to every other vectors."""
    n = len(sents)

    # For each sentence, find the average similarity to all the other 
    # sentences. Use reference equality to avoid comparing with self.
    return [(sum([sim(sent, other)
                  for s1 in sents
                  if s is not s1])
             / n)
            for sent in sents]


def sim(x, y):
    """Calculate the similarity between a vector x and y. Returns a float."""
    assert len(x) == len(y), 'Vectors are not the same.'
    pass


if __name__ == '__main__':
    pass
