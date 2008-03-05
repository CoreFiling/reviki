#!/usr/bin/python

import os.path

BOILERPLATE = open('BOILERPLATE', 'r').read()

def apply_boilerplate(arg, dirname, fnames):
  for i, name in enumerate(fnames):
    if name == '.svn':
            del fnames[i]
    elif name.endswith('.java'):
      path = os.path.join(dirname, name)
      ensure_has_boilerplate(path)

def ensure_has_boilerplate(path):
  print path

for dir in ('src', 'webtests'):
    os.path.walk('src', apply_boilerplate, None)
