import sys,string
from sane_re import *
text = sys.stdin.read()
text = _R(r'<\S+>').gsub(text,'')
dots = sum(x=='.' for x in text)
caps = sum(x[0] in string.uppercase for x in text.split())

print (dots*1.0+1)/(caps+1)

