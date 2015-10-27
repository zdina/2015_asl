#!/bin/bash

experimentId="testExperiment1"
database="52.29.58.74"
declare -a servers=("ec2-52-29-105-187" "ec2-52-28-189-57")
declare -a clients=("ec2-52-28-241-225" "ec2-52-29-83-127")

key="/Users/dinazverinski/Desktop/aslkey.pem"
code="Middleware/asl.jar"
conf="scripts/amazon/config.properties"
amazon=".eu-central-1.compute.amazonaws.com"


psql -h $database -U ec2-user << EOF
\c asldb
select emptydb();
EOF

count=1
for i in "${servers[@]}"
do
   echo "Starting server $i with number $count"
   scp -i $key $code ec2-user@$i$amazon:~
   scp -i $key $conf ec2-user@$i$amazon:~
   ssh -i $key ec2-user@$i$amazon "mkdir /home/ec2-user/tmp"
   ssh -i $key ec2-user@$i$amazon "java -cp asl.jar asl.middleware.Server $count 2>&1 > /home/ec2-user/tmp/server.out " &
   sleep 1
   while [ `ssh -i $key ec2-user@$i$amazon "cat /home/ec2-user/tmp/server.out | grep 'Server started' | wc -l"` != 1 ]
   do
   	sleep 1
   done
   ((count++))
done


count=1
servernum=0
pids=""
for i in "${clients[@]}"
do
  numClients=${#clients[@]}
  if [ "$count" -le "$((numClients/2))" ]
    then
    servernum=1
  else
    servernum=2
  fi
  echo "starting client $i with server $servernum"
  scp -i $key $code ec2-user@$i$amazon:~
  scp -i $key $conf ec2-user@$i$amazon:~
  ssh -i $key ec2-user@$i$amazon "mkdir /home/ec2-user/tmp"
  ssh -i $key ec2-user@$i$amazon "java -cp asl.jar asl.client.Client $servernum 2>&1 > /home/ec2-user/tmp/client.out " &
  pids="$pids $!"
  ((count++))
done

echo -ne "  Waiting for the clients to finish ... "
for f in $pids
do
	wait $f
done
echo "OK"

mkdir "Experiments/$experimentId"

touch Experiments/$experimentId/allservers.log
count=1
for i in "${servers[@]}"
do
   echo "Shutting down server $i with number $count"
   ssh -i $key ec2-user@$i$amazon "killall java"
   mkdir "Experiments/$experimentId/server$count/"
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/tmp/* Experiments/$experimentId/server$count/
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/server.log Experiments/$experimentId/server$count/
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/server-error.log Experiments/$experimentId/server$count/
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/tmp"
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/logs"
      cat Experiments/$experimentId/server$count/server.log >> Experiments/$experimentId/allservers.log
   ((count++))
done

touch Experiments/$experimentId/allclients.log
count=1
for i in "${clients[@]}"
do
   echo "Shutting down server $i with number $count"
   ssh -i $key ec2-user@$i$amazon "killall java"
   mkdir "Experiments/$experimentId/client$count/"
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/tmp/* Experiments/$experimentId/client$count/
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/client.log Experiments/$experimentId/client$count/
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/client-error.log Experiments/$experimentId/client$count/
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/tmp"
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/logs"
   cat Experiments/$experimentId/client$count/client.log >> Experiments/$experimentId/allclients.log
   ((count++))
done
