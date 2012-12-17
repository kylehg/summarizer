"""A Centrality Summarizer"""

from utils import *


def centrality(vects):
    """Calculate the centralities of each vector."""

    n = len(vects)

    # For each vector, find the average similarity to all the other
    # vectors. Use reference equality to avoid comparing with self.
    return [(sum([sim(vect, vect1)
                  for vect1 in vects
                  if vect is not vect1])
             / n)
            for vect in vects]


def gen_summary(sents, max_words):
    """Given a list of tokenized sentences and a threshold summary
    length (in words), return an ordered list of sentences comprising
    the summary."""
    vects = map(binary_vectorize, sents)
    centralities = sorted(zip(centrality(vects), sents), reverse=True)
    summary = []
    word_count = 0

    for sent, score in centralities:
        if word_count >= max_words:
            break
        if is_valid_sent_len(sent) and not is_repeat(sent, summary):
            summary.append(sent)
            word_count += len(sent)

    assert sum(map(len, summary)) <= max_words, 'Summary not within threshold'
    return summary


if __name__ == '__main__':
    collection_sents = get_toks(ls(INPUT_ROOT)[0])
    print collection_sents
