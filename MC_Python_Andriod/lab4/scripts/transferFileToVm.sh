#!/bin/bash

#myDir="/Users/fangjun/Documents/bitbucket/mobile-computing-lab/lab4/out/production/lab4/team5/mclab/ipvs/uni_stuttgart/de"
myDir="/Users/fangjun/Documents/bitbucket/mobile-computing-lab/lab4/lab4_task1/out/artifacts"
myDir="/Users/fangjun/Documents/bitbucket/mobile-computing-lab/lab4/lab4_task2_my/out/artifacts"

myFilenames=(
  #"lab4_task1.jar"
  "lab4_task2_my.jar"
  )
myHosts=(
  "marvin.informatik.uni-stuttgart.de"
)
myIdFile="/Users/fangjun/.ssh/marvin-38"
myUserName="kuangfn"
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
