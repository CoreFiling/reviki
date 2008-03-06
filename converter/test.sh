#!/bin/bash

INPUT_DIR="/home/mth/tmp/wiki-data"
OUTPUT_DIR="/home/mth/tmp/vq-exports"

rm -rf ~/tmp/svnwiki-data
./vqwiki2svnwiki.py "$INPUT_DIR" "$OUTPUT_DIR"
