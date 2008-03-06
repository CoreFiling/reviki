#!/bin/bash

INPUT_DIR="/home/local/mth/wiki-data"
OUTPUT_DIR="/home/local/mth/vq-exports"

rm -rf "$OUTPUT_DIR"
./vqwiki2reviki.py "$INPUT_DIR" "$OUTPUT_DIR"
