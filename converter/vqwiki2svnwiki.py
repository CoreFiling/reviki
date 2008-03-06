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
import shutil
import re
import sys

INPUT_DIR="/home/mth/tmp/wiki-data"
OUTPUT_DIR="/home/mth/tmp/svnwiki-data"

class VQWikiDescriptor:
  def __init__(self, name, rootDir, dataDir):
    self.name = name
    self.dataDir = dataDir
    self.versionsDir = join(rootDir, 'versions', name)
    self.uploadDir = join(rootDir, 'upload', name)

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
    if wiki == 'jsp':
      dataDir = dir
    else:
      dataDir = join(dir, wiki)
    yield VQWikiDescriptor(wiki, dir, dataDir)

IMAGE_EXTENSIONS = ['png', 'gif', 'jpg', 'jpeg', 'bmp']

ATTACH_QUOTED_RE = re.compile(r'attach:"([0-9-]*)(.*?)"')
ATTACH_RE = re.compile(r'attach:([0-9-]*)(.*?)\s')

def fix_attachment_references(wiki, inDir, outDir, page, markup):
  """
  Copies any attachments referenced by the page from
  the VQWiki upload directory to be attachments of the
  page.  Note this will result in duplicated attachments
  when they are referenced from more than one page
  but that's pretty rare.

  Returns the text with the attachment references fixed
  to be Creole style.
  """
  def handle_match(match):
    name = match.group(1) + match.group(2)
    cleanName = match.group(2)
    attachmentDir = join(outDir, page + '-attachments');
    if not os.path.isdir(attachmentDir):
      os.mkdir(attachmentDir)
    shutil.copyfile(join(inDir, name), join(attachmentDir, cleanName))
    if cleanName.rpartition('.')[2].lower() in IMAGE_EXTENSIONS:
      return "{{" + cleanName + "}}"
    else:
      return "[[" + cleanName + "]]"
  
  # Important to do quoted first as the REs overlap.
  markup = ATTACH_QUOTED_RE.sub(handle_match, markup)
  markup = ATTACH_RE.sub(handle_match, markup)
  return markup

TABLE_RE = re.compile('(^|\n)####(.*?)####', re.DOTALL)
BOLD_ITALIC_RE = re.compile("'''''(.*?)'''''", re.DOTALL)
BOLD_RE = re.compile("'''(.*?)'''", re.DOTALL)
ITALIC_RE = re.compile("''(.*?)''", re.DOTALL)
INLINE_ESCAPE_RE = re.compile("__(.*?)__", re.DOTALL)

def translate_markup(markup):
  """
  Convert the mark-up from VQWiki to Creole.
  This is a partial implementation with no great
  concern for correctness!

  Attachment references are not handled here,
  see fix_attachment_references.
  """
  def translate_lists(symbol, markup):
    def expand_symbol(match):
      return match.group(1) + match.group(2) + symbol * len(match.group(2))
    regex = re.compile('(^|\n)(\t+)' + re.escape(symbol))
    return regex.sub(expand_symbol, markup)

  def translate_tables(markup):
    def handle_table(match):
      content = match.group(2).replace('##', ' | ').strip()
      return '\n' + ('\n'.join('| ' + line for line in content.splitlines()))
    return TABLE_RE.sub(handle_table, markup)

  def wrap_group1_with(before, after):
    def helper(match):
      return before + match.group(1) + after
    return helper

  markup = BOLD_ITALIC_RE.sub(wrap_group1_with('//**', '**//'), markup)
  markup = BOLD_RE.sub(wrap_group1_with('**', '**'), markup)
  markup = ITALIC_RE.sub(wrap_group1_with('//', '//'), markup)
  markup = INLINE_ESCAPE_RE.sub(wrap_group1_with('{{', '}}'), markup)
  markup = markup.replace('\r\n', '\n')
  markup = translate_tables(markup)
  markup = translate_lists('#', markup)
  markup = translate_lists('*', markup)
  markup = markup.replace('\t', '  ')
  return markup

if os.path.exists(OUTPUT_DIR):
  print >> sys.stderr, OUTPUT_DIR, 'already exists'
  exit(1)
os.mkdir(OUTPUT_DIR)
for wiki in get_wikis(INPUT_DIR):
  outputDataDir = join(OUTPUT_DIR, wiki.name)
  os.mkdir(outputDataDir)
  for page in wiki.pages():
    markup = fix_attachment_references(wiki, wiki.uploadDir, outputDataDir, page, translate_markup(wiki.get(page)))
    open(join(outputDataDir, page), 'w').write(markup)

