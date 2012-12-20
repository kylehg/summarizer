"""A Centrality Summarizer"""

from nltk import tokenize

from utils import *


def centrality(vects):
    """Calculate the centralities of each vector."""

    n = len(vects)

    # For each vector, find the average similarity to all the other
    # vectors. Use reference equality to avoid comparing with self.
    return [(sum([cosine_sim(vect, vect1)
                  for vect1 in vects
                  if vect is not vect1])
             / n)
            for vect in vects]


def gen_centrality_summary(orig_sents, max_words):
    """Given a list of *untokenized* sentences and a threshold summary
    length (in words), return an ordered list of sentences comprising
    the summary."""
    tok_sents = [tokenize.word_tokenize(orig_sent)
                 for orig_sent in orig_sents]
    # TODO: Remove funcwords, etc?
    feat_space = sorted(set().union(*tok_sents))
    vects = [binary_vectorize(feat_space, tok_sent)
             for tok_sent in tok_sents]
    centralities = sorted(zip(centrality(vects), tok_sents, orig_sents),
                          reverse=True)
    summary, tok_summary = [], []
    word_count = 0

    for score, tok_sent, orig_sent in centralities:
        if word_count >= max_words:
            break
        if (is_valid_sent_len(tok_sent) and
            not is_repeat(tok_sent, tok_summary)):
            summary.append(orig_sent)
            tok_summary.append(tok_sent)
            word_count += len(tok_sent)

    assert sum(map(len, summary)) <= max_words, 'Summary not within threshold'
    return summary


if __name__ == '__main__':
    sents = get_sentences(ls(INPUT_ROOT)[0])
    print gen_centrality_summary(sents, 100)
