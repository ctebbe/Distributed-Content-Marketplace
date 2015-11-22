#!/bin/bash

HOST=$(hostname)

ant clean
ant jar

# discovery node start-up
gnome-terminal -e 'bash -c "ant discovery; bash"'

sleep 1 # allow the discovery node time to spin up

while read PEERNODE
do
    sarray=($PEERNODE)
    gnome-terminal -x bash -c "ssh -t ${sarray[0]} 'cd '~/workspace/cs555/project'; ant -Darg0=${HOST} -Darg1=${sarray[1]} peer; bash'" &
done < peernodes
