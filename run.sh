#!/bin/bash

HOST=$(hostname)

#ant clean
#ant jar

# discovery node start-up
gnome-terminal -e 'bash -c "ant discovery; bash"'

sleep 5 # allow the discovery node time to spin up

while read PEERNODE
do
    #echo 'sshing into '${PEERNODE}
    sarray=($PEERNODE)
    #echo ${sarray[0]} echo ':' echo ${sarray[1]}
    #gnome-terminal -x bash -c "ssh -t ${sarray[0]} 'cd '~/workspace/cs555/A02'; bash'" #&
    gnome-terminal -x bash -c "ssh -t ${sarray[0]} 'cd '~/workspace/cs555/project'; ant -Darg0=${HOST} peer; bash'" #&
    #  ;ant -Darg0=jefferson-city -Darg1= peer_custom
done < peernodes

#gnome-terminal -x bash -c "ssh -t santa-fe 'cd '~/workspace/cs555/A02'; echo $PEERNODE; ant -Darg0=${HOST} -Darg1=true peer_custom; bash'" #&

# data node startup gnome-terminal -x bash -c "ssh -t salt-lake-city 'cd '~/workspace/cs555/A02'; echo $(hostname); ant -Darg0=${HOST} data; bash'" #&
