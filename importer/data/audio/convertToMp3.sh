#!/bin/bash

function convert() {
  for audioFile in $(ls *.wav); do
    local name="${audioFile%.*}"
    if [ ! -e $name.mp3 ] ; then
      echo "Converting $audioFile"
      ffmpeg -i $audioFile -vn -ar 44100 -ac 2 -ab 96k -f mp3 $name.mp3
    else  
      echo "$audioFile already converted"
    fi
  done
}
convert
