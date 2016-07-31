#!/bin/bash

myDir="/home/kuangfn/lab4-task1"
myFilenames=(
  "lab4_task1.jar"
)
myHosts=(
  "vspi32"
  "vspi31"
  "vspi33"
  "129.69.210.197"
  "192.168.213.30"
)
myIdFile="/home/kuangfn/.ssh/mclab"
myUserName="team5"
myDirInRemoteHost="~/lab4-task1/"

for myFile in ${myFilenames[@]}; do
  for myHost in ${myHosts[@]}; do
    echo "Start to copy ${myFile} to ${myHost}:${myDirInRemoteHost}"
    scp -i "${myIdFile}" "${myDir}/${myFile}" "${myUserName}@${myHost}:${myDirInRemoteHost}"
    if [ $? -eq 0 ];
    then
        echo "Finish copying ${myFile} to ${myHost}:${myDirInRemoteHost}"
    else
        echo "Failed: copy ${myFile} to ${myHost}:${myDirInRemoteHost}"
    fi
    echo ""
  done
done
