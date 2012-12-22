""" our summarizer. woo~ """

import sys, os
from re import sub
from subprocess import call
from collections import defaultdict
from nltk.tokenize import word_tokenize
from BeautifulSoup import BeautifulStoneSoup 

sys.path.append(os.path.abspath('../'))
from utils import *

def call_ark(inputfile):
    ark_cmd = "./arkref.sh -input " + inputfile
    call(ark_cmd, shell=True)

def cat_files(inputdir):
    filecontents = []
    for file in ls(inputdir):
        filecontents.append(open(file).read())
    return "\n".join(filecontents)

def build_corefs(inputdir):
    tagged_files = []
    for file in ls(inputdir):
        call_ark(file)
        tagged_files.append(file[:-4] + ".tagged")
    return tagged_files

def build_all_corefs(inputdir):
    for subdir in ls(inputdir):
        build_corefs(subdir)

def resolve_coref(taggeddir):
    # taggeddir contains file.tagged for all files in original file
    ent_count = defaultdict(int) # numeric string to integer count
    ent_names = defaultdict(list) # numeric string to list of strings
    for file in ls(taggeddir):
        if file[-6:] == "tagged":
            tagged_sents = []
            rank_entities(open(file).readlines(), ent_count, ent_names)
    return ent_count, ent_names

def rank_entities(tagged_sents, ent_count, ent_names):
    try:
        test = BeautifulStoneSoup.NESTABLE_TAGS['mention']
    except KeyError:
        BeautifulStoneSoup.NESTABLE_TAGS['mention'] = []
    for sent in tagged_sents:
        soup = BeautifulStoneSoup(sent)
        all_ents = [sub("<.*?>","",str(m)) for m in soup.findAll('mention')]    
        ent_attrs = [m.attrs for m in soup.findAll('mention')]
        for i, ent in enumerate(ent_attrs):
            ent_count[ent[1][1]] += 1
            ent_names[ent[1][1]].append(all_ents[i])

def check_overlap(listone, listtwo):
    overlap = 0
    for itemone in set(listone):
        for itemtwo in set(listtwo):
            if itemone == itemtwo:
                overlap += 1
    if overlap > (len(set(listone)) + len(set(listtwo)) / 2) / 2:
        return True
    return False

def get_top_ents(taggeddir):
    ent_count, ent_names = resolve_coref(taggeddir)
    top_ents = sorted(ent_count.items(), key=lambda t: t[1], reverse=True)[0:20]
    for i, ent in enumerate(top_ents):
        for j, otherent in enumerate(top_ents):
            if check_overlap(ent_names[ent[0]], ent_names[otherent[0]]):
                ent_names[ent[0]].extend(ent_names[otherent[0]])
                ent_count[ent[1]] += ent_count[otherent[1]]
                del ent_count[otherent[0]]
                del ent_names[otherent[0]]
                del top_ents[j]
    return [ent for entlist in sorted(ent_count.items(), key=lambda t: t[1], reverse=True)[0:10] for ent in ent_names[entlist[0]]]

def count_ents(sentence, entlist):
    # don't take out entities in other entities because could be separate 
    # entities like "Amalia" versus "Amalia's friend" ... may cause a few
    # issues but less than otherwise.
    score = 0
    for ent in set(entlist):
        if ent in sentence:
            score += 1
    return score

def get_sent_dict(all_sents, entlist):
    sent_dict = dict()
    for sent in all_sents:
        # scoring number one CHANGE
        sent_dict[sent] = count_ents(sent, entlist)/len(word_tokenize(sent))
    return sent_dict

def generate_summary(taggeddir, untaggeddir):
    all_sents = get_sentences(untaggeddir)
    entlist = get_top_ents(taggeddir)
    sent_dict = get_sent_dict(all_sents, entlist)
    best_sents = sorted(sent_dict.items(), key=lambda t: t[1], reverse=True)
    pretty = []
    for sent in best_sents:
        if not is_repeat(sent[0], pretty):
            pretty.append(sent[0])
    # return 100 words
    return " ".join(word_tokenize(" ".join(pretty))[:100])

if __name__ == '__main__':
    # build_all_corefs("../input/")
    for rawdir in ls("../input/"):
        name = rawdir.split("/")[-1][0:11]
        if name[0] != ".":
            taggeddir = "../coref-tagged/" + name + "_tagged"
            summary = generate_summary(taggeddir, rawdir)
            sumfile = open("../hg-summaries/" + name, "w")
            sumfile.write(summary)

