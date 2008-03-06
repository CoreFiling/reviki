#!/usr/bin/python

"""
A python script to partially convert a VQWiki data directory
to a directory suitable for checking in as the data for
svnwiki.

Author: Matt Hillsdon <matt@hillsdon.net>
"""

import glob
import os
from os.path import join, split
import re
import sys

INPUT_DIR="/home/mth/tmp/wiki-data"
OUTPUT_DIR="/home/mth/tmp/svnwiki-data"

class VQWikiDescriptor:
  def __init__(self, name, dataDir):
    self.name = name
    self.dataDir = dataDir
    self.versionsDir = join(dataDir, 'versions')
    self.uploadDir = join(dataDir, 'upload')

  def pages(self):
    return [split(x)[-1][0:-4]
             for x in glob.glob(join(self.dataDir, '*.txt'))]

  def get(self, page):
    return open(join(self.dataDir, page + '.txt'), 'r').read()

def get_wikis(dir):
  """
  Yields VQWikiDescriptors for each sub-wiki in the dir.
  """
  for wiki in [line.strip() for line in open(join(dir, 'virtualwikis.lst'), 'r')]:
    # Not sure if this is specific to our setup.
    if (wiki == 'jsp'):
      yield VQWikiDescriptor('default', dir)
    else:
      yield VQWikiDescriptor(wiki, join(dir, wiki))

def fix_attachment_references(wiki, markup):
  """
  Copies any attachments referenced by the page from
  the VQWiki upload directory to be attachments of the
  page.  Note this will result in duplicated attachments
  when they are referenced from more than one page
  but that's pretty rare.

  Returns the text with the attachment references fixed
  to be Creole style.
  """
  return markup

def translate_markup(markup):
  """
  Convert the mark-up from VQWiki to Creole.
  This is a partial implementation with no great
  concern for correctness!

  Attachment references are not handled here,
  see fix_attachment_references.
  """
  return markup
  

if os.path.exists(OUTPUT_DIR):
  print >> sys.stderr, OUTPUT_DIR, 'already exists'
  exit(1)
os.mkdir(OUTPUT_DIR)
for wiki in get_wikis(INPUT_DIR):
  outputDataDir = join(OUTPUT_DIR, wiki.name)
  os.mkdir(outputDataDir)
  for page in wiki.pages():
    markup = fix_attachment_references(wiki, translate_markup(wiki.get(page)))
    fout = open(join(outputDataDir, page), 'w')
    fout.write(markup)



