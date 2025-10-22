#!/bin/bash
# run with Git Batch from explorer/src/test/resources/logstash/scriptForLocal
parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"
echo $(pwd)
echo 'run treatment'
curl -XPOST http://localhost:9200/rcn_specimen_short/_bulk -H "Content-Type: application/x-ndjson" --data-binary @specimen.ndjson
echo 'end treatment'