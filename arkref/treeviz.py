#!/usr/bin/env python

# treeviz.py
# Give this an s-expression, treebank-style parse tree on STDIN
# It will make a graphviz graphic and open it on your computer
# to look at.
# Brendan O'Connor (anyall.org)

# has only been tested on linux and mac
# and requires GraphViz to be installed - the 'dot' command.

from __future__ import with_statement
import sys,os,time,pprint,re

nounish = '#700070'
verbish = '#207020'
prepish = '#C35617'
modifiers = '#902010'
coordish = '#404090'
fade = '#b0b0b0'

dep_colors = {
## tricky. LTH uses SUB for "subordinate clause" but penn2malt uses it for "subject".
  #'SUB': '#202090',
  'SBJ': '#202090',
  #'OBJ': '#903030',
  #'OBJ': '#CC009C',
  'OBJ': ' #9F336C',
  'PMOD': prepish,
  'COORD': coordish,
  'CONJ': coordish,
  #'SBAR': prepish,
  'NMOD': nounish,
  'VMOD': verbish,
  'VC': verbish,
  'AMOD': modifiers,
  #'ADV': modifiers,
  'P': fade,
}

# parts out of the stanford dep hierarchy
d=dep_colors
for c in 'aux auxpass cop'.split(): d[c] = verbish
for c in 'subj nsubj nsubjpass csubj'.split(): d[c] = d['SBJ']
for c in 'obj dobj iobj '.split(): d[c] = d['OBJ']
#for c in 'arg comp agent   attr ccomp xcomp compl mark rel acomp'.split(): d[c]=d['OBJ']
  #if pos.startswith('prep'): return prepish
#for c in 'mod advcl purpcl tmod rcmod amod infmod partmod num number  appos nn abbrev advmod neg poss possessive prt det prep'.split(): d[c]=d['AMOD']
d['nn'] = nounish
#d['amod'] = modifiers
del d


def pos_color(pos):
  if pos.startswith('VB') or pos=='MD': return verbish
  if pos.startswith('NN') or pos.startswith('PRP') or pos.startswith('NNP'): return nounish
  if pos in ('IN','TO'): return prepish
  #if pos.startswith('JJ') or pos.endswith('DT'): return fade
  if pos.startswith('RB') or pos.startswith('JJ'): return modifiers

  if pos.startswith('NP'): return nounish
  if pos.startswith('VP'): return verbish
  if pos.startswith('PP'): return prepish
  if pos in ('ADVP','ADJP'): return modifiers
  if pos=='CC': return coordish

  return 'black'

## some counts
#1398844 NMOD
#575426 VMOD
#422175 PMOD
#402221 P
#262052 SUB
#149750 OBJ
#138129 ROOT
#138129 
#122029 VC
#77576 SBAR
#70289 AMOD
#41906 PRD
#9181 DEP

#dep_bold = set(['SBJ','OBJ'])
dep_bold = set([])



def parse_sexpr(s):
  s = s[s.find('('):]
  tree = []
  stack = []  # top of stack (index -1) points to current node in tree
  stack.append(tree)
  curtok = ""
  depth = 0
  for c in s:
    if c=='(':
      new = []
      stack[-1].append(new)
      stack.append(new)
      curtok = ""
      depth += 1
    elif c==')':
      if curtok:
        stack[-1].append(curtok)
        curtok = ""
      stack.pop()
      curtok = ""
      depth -= 1
    elif c.isspace():
      if curtok:
        stack[-1].append(curtok)
        curtok = ""
    else:
      curtok += c
    if depth<0: raise BadSexpr("Too many closing parens")
  if depth>0: raise BadSexpr("Didn't close all parens, depth %d" % depth)
  root = tree[0]
  # weird
  if isinstance(root[0], list):
    root = ["ROOT"] + root
  return root

class BadSexpr(Exception):pass

def is_balanced(s):
  if '(' not in s: return False
  d = 0
  for c in s:
    if c=='(': d += 1
    if c==')': d -= 1
    if d<0: return False
  return d==0


counter = 0
def graph_tuples(node, parent_pos=None):
  # makes both NODE and EDGE tuples from the tree
  global counter
  my_id = counter
  if isinstance(node,str):
    col = pos_color(parent_pos)
    return [("NODE", my_id, node, {'shape':'box','fontcolor':col, 'color':col})]
  tuples = []
  name = node[0]
  name = name.replace("=H","")
  #color = 'blue' if name=="NP" else 'black'
  color = pos_color(name)
  tuples.append(("NODE", my_id, name, {'shape':'none','fontcolor':color}))
  
  for child in node[1:]:
    counter += 1
    child_id = counter
    opts = {}
    if len(node)>2 and isinstance(child,list) and child[0].endswith("=H"):
      opts['arrowhead']='none'
      opts['style']='bold'
    else:
      opts['arrowhead']='none'
    opts['color'] = pos_color(name) if isinstance(child,str) else \
        pos_color(name) if pos_color(child[0]) == pos_color(name) else \
        'black'
    tuples.append(("EDGE", my_id, child_id, opts))
    tuples += graph_tuples(child, name)
  return tuples

def dot_from_tuples(tuples):
  # takes graph_tuples and makes them into graphviz 'dot' format
  dot = "digraph { "
  for t in tuples:
    if t[0]=="NODE":
      more = " ".join(['%s="%s"' % (k,v) for (k,v) in t[3].items()]) 
      dot += """%s [label="%s" %s]; """ % (t[1], t[2], more)
    elif t[0]=="EDGE":
      more = " ".join(['%s="%s"' % (k,v) for (k,v) in t[3].items()]) 
      dot += """ %s -> %s [%s]; """ % (t[1],t[2], more)
  dot += "}"
  return dot

def call_dot(dotstr, filename="/tmp/tmp.png", format='png'):
  dot = "/tmp/tmp.%s.dot" % os.getpid()
  with open(dot, 'w') as f:
    print>>f, dotstr
  if False and format=='pdf':
    os.system("dot -Teps < " +dot+ " | ps2pdf -dEPSCrop -dEPSFitPage - > " + filename)
  else:
    os.system("dot -T" +format+ " < " +dot+ " > " + filename)


def open_file(filename):
  import webbrowser
  f = "file://" + os.path.abspath(filename)
  webbrowser.open(f)
  # os.system(opener + " " + filename)

def show_tree(sexpr, format):
  tree = parse_sexpr(sexpr)
  tuples = graph_tuples(tree)
  dotstr = dot_from_tuples(tuples)
  filename = "/tmp/tmp.%s.%s" % (time.time(),format)
  call_dot(dotstr, filename, format=format)
  return filename

def conll_to_tuples(conll):
  ret = []
  stuff = [line.split() for line in conll.split("\n") if line.strip()]
  for row in stuff:
    id = row[0]
    word=row[1]
    pos=row[3]
    target = row[6]
    rel = row[7]
    if id != '0':
      col = pos_color(pos)
      ret.append(("NODE", id, "%s /%s" % (word,pos), {'shape':'none', 'fontcolor':col}))
    opts = {'label':rel.lower(),'dir':'forward'}  #forward back both none
    if rel in dep_colors:
      opts.update({'fontcolor':dep_colors[rel], 'color':dep_colors[rel]})
    if rel in dep_bold: opts['fontname'] = 'Times-Bold'
    if target!='0':
      ret.append(("EDGE", target,id, opts))
  return ret

def show_conll(conll, format):
  tuples = conll_to_tuples(conll)
  dotstr = dot_from_tuples(tuples)
  filename = "/tmp/tmp.%s.%s" % (time.time(),format)
  call_dot(dotstr, filename, format=format)
  return filename


def do_multi_tree(parses, to_tuples):  ##= lambda s: dot_from_tuples(graph_tuples(s))):
  base = "/tmp/tmp.%s_NUM.pdf" % (time.time(),)
  for i,parse in enumerate(parses):
    output = base.replace("NUM", "%.03d" % (i+1))
    call_dot(dot_from_tuples(to_tuples(parse)), filename=output, format='pdf')
  output = base.replace("NUM","merged")
  inputs = base.replace("NUM","*")
  os.system("gs -q -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -sOutputFile=%s %s" % (output,inputs))
  return output
  
def smart_process(input, format):
  input = input.strip()
  lines = input.split("\n")
  lines = [l for l in lines if l.strip()]
  # multiple sexprs
  if format=='pdf' and len(lines) > 1 and is_balanced(lines[0]) and is_balanced(lines[1]):
    try:
      return do_multi_tree(lines, lambda s: graph_tuples(parse_sexpr(s)))
    except BadSexpr:
      pass
  # single (potentially multiline) sexpr
  if not all( len(x.split()) in (0,10) for x in lines[:3]) and \
     '(' in input and ')' in input:
    try:
      return show_tree(input, format)
    except BadSexpr:
      pass
  parts = re.split(r'\n[ \t\r]*\n', input)
  # multiple dep parses
  if format=='pdf' and len(parts) > 1:
    return do_multi_tree(parts, conll_to_tuples)
  # single dep parse
  return show_conll(input, format)

if __name__=='__main__':
  import sys
  input = sys.stdin.read().strip()
  format = 'png' if '-png' in sys.argv else \
            'eps' if '-eps' in sys.argv else \
            'pdf' if '-pdf' in sys.argv else \
            'pdf' if sys.platform=='darwin' else \
            'png'
  output_filename = smart_process(input, format)
  open_file(output_filename)

