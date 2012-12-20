"""Generate Rouge configs and run them."""

def gen_configs(sum_type, peer_root, model_root, summaries):
    """Given a summary type string, e.g., 'topicwords', and a list of 
    (summary, [comparisons]) tuples, generate the appropriate config."""
    with open(sum_type + '-config.xml', 'w') as f:
        f.write('<ROUGE-EVAL version="1.0">\n')
        for i, (summary, comparisons) in enumerate(summaries):
            f.write('<EVAL ID="1">\n')
            f.write('<PEER-ROOT>%s</PEER-ROOT>\n' % peer_root)
            f.write('<MODEL-ROOT>%s</MODEL-ROOT>\n' % model_root)
            f.write('<INPUT-FORMAT TYPE="SPL"></INPUT-FORMAT>\n')
            f.write('<PEERS><P ID="%s">summary00.txt</P></PEERS>\n' % sum_type)
            f.write('<MODELS>\n')
            f.write('\n'.join(['<M ID="S%s">%s</M>' % (i, summ)
                               for i, summ in enumerate(summaries)]))
            f.write('\n</MODELS>\n')
            f.write('</EVAL>\n')
        f.write('</ROUGE-EVAL>')


