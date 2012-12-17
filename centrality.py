"""A Centrality Summarizer"""

MIN_SENT_LEN = 10
MAX_SENT_LEN = 35


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


def sim(x, y):
    """Calculate the similarity between a vector x and y. Returns a float."""
    assert len(x) == len(y), 'Vectors are not the same.'
    raise NotImplementedError


def is_valid_sent_len(sent):
    """Takes a list of tokens, returns if valid token length."""
    return MIN_SENT_LEN <= len(sent) <= MAX_SENT_LEN


def is_repeat(sent, sents):
    """Given a tokenized sentence and a list of tokenized sentences,
    return whether the sentences overlaps too highly in content with any
    of the others."""
    raise NotImplementedError


def gen_summary(sents, max_words):
    """Given a list of tokenized sentences and a threshold summary
    length (in words), return an ordered list of sentences comprising
    the summary."""
    vects = [vectorize(sent) for sent in sents]
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
    pass
