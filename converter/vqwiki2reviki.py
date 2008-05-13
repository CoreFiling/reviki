#!/usr/bin/python

"""
A python script to partially convert a VQWiki data directory
to a directory suitable for checking in as the data for
reviki.

Author: Matt Hillsdon <matt@hillsdon.net>
"""

import glob
import os
from os.path import join, split
import shutil
import re
import sys

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
ATTACH_RE = re.compile(r'attach:([0-9-]*)(.*?)(?=[\s}])')

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
    if os.path.exists(join(attachmentDir, cleanName)):
      # Oh well, we tried.
      cleanName = name
    shutil.copyfile(join(inDir, name), join(attachmentDir, cleanName))
    if cleanName[cleanName.rfind('.') + 1:].lower() in IMAGE_EXTENSIONS:
      return "{{" + cleanName + "}}"
    else:
      return "[[" + cleanName + "]]"
  
  # Important to do quoted first as the REs overlap.
  markup = ATTACH_QUOTED_RE.sub(handle_match, markup)
  markup = ATTACH_RE.sub(handle_match, markup)
  return markup

H1_RE = re.compile('^!!!(.*?)!!!$', re.MULTILINE)
H2_RE = re.compile('^!!(.*?)!!$', re.MULTILINE)
H3_RE = re.compile('^!(.*?)!$', re.MULTILINE)
TABLE_RE = re.compile('(^|\n)####(.*?)####', re.DOTALL)
BOLD_ITALIC_RE = re.compile("'''''(.*?)'''''", re.DOTALL)
BOLD_RE = re.compile("'''(.*?)'''", re.DOTALL)
ITALIC_RE = re.compile("''(.*?)''", re.DOTALL)
INLINE_ESCAPE_RE = re.compile("__(.*?)__", re.DOTALL)
MULTILINE_ESCAPE_RE = re.compile('(^|\n)@@@@(.*?)($|\n\n)', re.DOTALL)
EXTERNAL_LINK = re.compile('ext:(.*?(\n|$))')
NAMED_EXTERNAL_LINK = re.compile(r'\[ext:(.*?)\|(.*?)\]')

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
      return '\n' + ('\n'.join(['| ' + line for line in content.splitlines()]))
    return TABLE_RE.sub(handle_table, markup)

  def wrap_group_with(group, before, after):
    def helper(match):
      return before + match.group(group) + after
    return helper

  def handle_named_external_link(match):
    return '[[' + match.group(1) + '|' + match.group(2) + ']]'

  # We normalize line endings first but do tabs to spaces last to simplify REs.
  markup = markup.replace('\r\n', '\n')
  markup = MULTILINE_ESCAPE_RE.sub(wrap_group_with(2, '\n{{{', '\n}}}\n\n'), markup)
  markup = INLINE_ESCAPE_RE.sub(wrap_group_with(1, '{{', '}}'), markup)

  # Order is important for these three.
  markup = BOLD_ITALIC_RE.sub(wrap_group_with(1, '//**', '**//'), markup)
  markup = BOLD_RE.sub(wrap_group_with(1, '**', '**'), markup)
  markup = ITALIC_RE.sub(wrap_group_with(1, '//', '//'), markup)

  markup = H1_RE.sub(wrap_group_with(1, '== ', ' =='), markup)
  markup = H2_RE.sub(wrap_group_with(1, '=== ', ' ==='), markup)
  markup = H3_RE.sub(wrap_group_with(1, '==== ', ' ===='), markup)

  # Order important for these two.
  markup = NAMED_EXTERNAL_LINK.sub(handle_named_external_link, markup)
  markup = EXTERNAL_LINK.sub(wrap_group_with(1, '', ''), markup)

  markup = translate_tables(markup)
  markup = translate_lists('#', markup)
  markup = translate_lists('*', markup)
  markup = markup.replace('\t', '  ')
  return markup

try:
  inputDir = sys.argv[1]
  outputDir = sys.argv[2]
except IndexError:
  print >> sys.stderr, 'Usage:', sys.argv[0], '<inputDir> <outputDir>'
  sys.exit(1)

if os.path.exists(outputDir):
  print >> sys.stderr, outputDir, 'already exists'
  sys.exit(1)

os.mkdir(outputDir)
for wiki in get_wikis(inputDir):
  outputDataDir = join(outputDir, wiki.name)
  os.mkdir(outputDataDir)
  for page in wiki.pages():
    markup = wiki.get(page)
    try:
      markup = translate_markup(wiki.get(page))
      markup = fix_attachment_references(wiki, wiki.uploadDir, outputDataDir, page, markup)
      open(join(outputDataDir, page), 'w').write(markup)
    except:
      print >> sys.stderr, "Error converting", page, "dumping markup:"
      print >> sys.stderr, markup
      raise

