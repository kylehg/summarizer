"""Generate Rouge configs and run them."""

import os
import subprocess


ROUGE_EXEC = './ROUGE-1.5.5.pl' #'/home1/c/cis530/final_project/rouge/ROUGE-1.5.5.pl'


def gen_config(sum_type, config_filename, peer_root, model_root, summaries):
    """Given a summary type string, e.g., 'topicwords', and a list of
    (summary, [comparisons]) tuples, generate the appropriate config."""
    with open(config_filename, 'w') as f:
        f.write('<ROUGE-EVAL version="1.0">\n')
        for i, (summary, comparisons) in enumerate(summaries):
            f.write('<EVAL ID="1">\n')
            f.write('<PEER-ROOT>%s</PEER-ROOT>\n' % peer_root)
            f.write('<MODEL-ROOT>%s</MODEL-ROOT>\n' % model_root)
            f.write('<INPUT-FORMAT TYPE="SPL"></INPUT-FORMAT>\n')
            f.write('<PEERS><P ID="%s">summary00.txt</P></PEERS>\n' % sum_type)
            f.write('<MODELS>\n')
            f.write('\n'.join(['<M ID="S%s">%s</M>' % (i, summ)
                               for i, summ in enumerate(comparisons)]))
            f.write('\n</MODELS>\n')
            f.write('</EVAL>\n')
        f.write('</ROUGE-EVAL>')


if __name__ == '__main__':
    from utils import get_collections
    sums = [(os.path.basename(baseline), map(os.path.basename, models))
            for docs, models, baseline in get_collections()]
    config_filename = 'rouge/baseline-config.xml'
    gen_configs('baseline', config_filename, 'baseline', 'models', sums)

