#!/bin/bash

INPUT_DIR="/home/local/mth/wiki-data"
OUTPUT_DIR="/home/local/mth/vq-exports"

rm -rf ~/tmp/svnwiki-data
./vqwiki2svnwiki.py "$INPUT_DIR" "$OUTPUT_DIR"
