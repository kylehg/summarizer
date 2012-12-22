"""Generate Rouge configs and run them."""

import os
import subprocess
from utils import get_collections


ROUGE_EXEC = './ROUGE-1.5.5.pl' #'/home1/c/cis530/final_project/rouge/ROUGE-1.5.5.pl'


def gen_config(sum_type, config_filename, peer_folder, sums=None):
    """Given a summary type string, e.g., 'topicwords', and a list of
    (summary, [comparisons]) tuples, generate the appropriate config."""
    sums = sums or [(i, models) for i, _, models, _ in get_collections(False)]
    with open(config_filename, 'w') as f:
        f.write('<ROUGE-EVAL version="1.0">\n')
        for i, comparisons in sums:
            f.write('<EVAL ID="%d">\n' % (i+1))
            f.write('<PEER-ROOT>%s</PEER-ROOT>\n' % peer_folder)
            f.write('<MODEL-ROOT>models</MODEL-ROOT>\n')
            f.write('<INPUT-FORMAT TYPE="SPL"></INPUT-FORMAT>\n')
            f.write('<PEERS><P ID="%s">summary%02d.txt</P></PEERS>\n' % (sum_type, i))
            f.write('<MODELS>\n')
            f.write('\n'.join(['<M ID="S%s">%s</M>' % (j, summ)
                               for j, summ in enumerate(comparisons)]))
            f.write('\n</MODELS>\n')
            f.write('</EVAL>\n')
        f.write('</ROUGE-EVAL>')


if __name__ == '__main__':
    gen_config('baseline', 'rouge/baseline-config.xml', 'baseline')

