# UNMAINTAINED: CIS-530 Final Project

**NOTE: This was a school project. It is very likely riddled with bugs, and is entirely unmaintained. It should not be considered for any real-world use and we are not accepting issues or PRs.**

A multidocument summarizer. Final project for CIS-530.
By Kyle Hardgrave and Amalia Hawkins


Centrality Summarizer
---------------------

File: `centrality.py`
Main function: `gen_centrality_summary(orig_sents, max_words)`
Summaries: `rouge/centrality/`
ROUGE config: `rouge/centrality-config.xml`

### Parameters
**Valid sentance length**: We only chose sentences of between 10 and 55 words. Initially starting with just 15-35, as the write-up recommended, we expanded it slightly to include for the sometimes wordy but information-packed sentences of news articles.

**Remove redundant sentences**: Using TF-IDF cosine similarity (which we found to be a slightly better indicator than binary cosine similarity), we removed sentences that had a similarity score of greater than 0.4.

### Rougue

    ---------------------------------------------
    centrality ROUGE-1 Average_R: 0.36114 (95%-conf.int. 0.34785 - 0.37332)
    centrality ROUGE-1 Average_P: 0.36661 (95%-conf.int. 0.35374 - 0.37881)
    centrality ROUGE-1 Average_F: 0.36369 (95%-conf.int. 0.35107 - 0.37603)
    ---------------------------------------------
    centrality ROUGE-2 Average_R: 0.07496 (95%-conf.int. 0.06714 - 0.08299)
    centrality ROUGE-2 Average_P: 0.07595 (95%-conf.int. 0.06828 - 0.08400)
    centrality ROUGE-2 Average_F: 0.07542 (95%-conf.int. 0.06767 - 0.08341)


Centrality Summarizer
---------------------

File: `lexrank.py`
Main function: `gen_lexrank_summary(orig_sents, max_words)`
Summaries: `rouge/lexrank/`
ROUGE config: `rouge/lexrank-config.xml`

### Parameters
**Valid sentance length**: Again, we only chose sentences of between 10 and 55 words.

**Remove redundant sentences**: Similar again to centrality, we removed sentences that had a similarity score of greater than 0.4.

**Dampening factor**: We used a dampening factor of 0.85 in the PageRank algorithm, based on general recommendations for the PageRank algorithm.

**Min similarity for edges**: Here, we included an edge between any sentence with a TF-IDF cosine similarity of greater than 0.2.

### Rougue

    ---------------------------------------------
    lexrank ROUGE-1 Average_R: 0.34099 (95%-conf.int. 0.32894 - 0.35323)
    lexrank ROUGE-1 Average_P: 0.34600 (95%-conf.int. 0.33375 - 0.35765)
    lexrank ROUGE-1 Average_F: 0.34338 (95%-conf.int. 0.33103 - 0.35531)
    ---------------------------------------------
    lexrank ROUGE-2 Average_R: 0.06593 (95%-conf.int. 0.05933 - 0.07278)
    lexrank ROUGE-2 Average_P: 0.06695 (95%-conf.int. 0.06024 - 0.07405)
    lexrank ROUGE-2 Average_F: 0.06642 (95%-conf.int. 0.05971 - 0.07336)


## Discussion & Analysis

**AMALIA ADD TO ME PLZ**

We found the baseline surprisingly hard to beat with centrality and LexPageRank. Likely some more fine-tuning of parameters could have yielded some better results -- with 3-5 parameters per summarization method (once you factor in choice of vectorization method), finding the sweetspot could be difficult. It's also likely, though, that the baseline will continue to be strong given that these are news sources and thus the first sentences are written to essentially be summaries of what follows.
