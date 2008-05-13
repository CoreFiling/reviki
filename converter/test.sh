#!/bin/bash

INPUT_DIR="/home/mth/tmp/wiki-data"
OUTPUT_DIR="/home/mth/tmp/vq-exports"

rm -rf "$OUTPUT_DIR"
./vqwiki2reviki.py "$INPUT_DIR" "$OUTPUT_DIR"
