#!/bin/bash
experimentId="60clientsCascadeWarmDbNoLocks5minRefactoredOpenSystem"
database="52.29.105.154"
declare -a servers=("ec2-52-29-105-209" "ec2-52-29-105-202")
declare -a clients=("ec2-52-29-105-193" "ec2-52-29-103-142" "ec2-52-29-105-7" "ec2-52-29-104-225" "ec2-52-29-48-160" "ec2-52-29-105-207")

key="/Users/dinazverinski/Desktop/aslkey.pem"
code="Middleware/asl.jar"
conf="scripts/amazon/config.properties"
amazon=".eu-central-1.compute.amazonaws.com"
experimentFolder="Experiments/$experimentId"


#psql -h $database -U ec2-user << EOF
#\c asldb
#select emptydb();
#EOF

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

mkdir $experimentFolder

touch $experimentFolder/allservers.log
count=1
for i in "${servers[@]}"
do
   echo "Shutting down server $i with number $count"
   ssh -i $key ec2-user@$i$amazon "killall java"
   serverExperimentFolder=$experimentFolder/server$count
   mkdir $serverExperimentFolder/
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/tmp/* $serverExperimentFolder
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/server.log $serverExperimentFolder
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/server-error.log $serverExperimentFolder
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/queues.log $serverExperimentFolder
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/tmp"
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/logs"
      cat $serverExperimentFolder/server.log >> Experiments/$experimentId/allservers.log
   ((count++))
done

touch $experimentFolder/allclients.log
count=1
for i in "${clients[@]}"
do
   echo "Shutting down client $i with number $count"
   ssh -i $key ec2-user@$i$amazon "killall java"
   clientExperimentFolder=$experimentFolder/client$count
   mkdir $clientExperimentFolder/
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/tmp/* $clientExperimentFolder
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/client.log $clientExperimentFolder
   scp -i $key ec2-user@$i$amazon:/home/ec2-user/logs/client-error.log $clientExperimentFolder
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/tmp"
   ssh -i $key ec2-user@$i$amazon "rm -r /home/ec2-user/logs"
   cat $clientExperimentFolder/client.log >> Experiments/$experimentId/allclients.log
   ((count++))
done

cut -d, -f1-5 $experimentFolder/allclients.log | sort -k1,1n > $experimentFolder/allclientsplot.log

echo "  Generating response time trace with gnuplot"
gnuplot << EOF
set terminal png
set output '$experimentFolder/client.png'
set datafile separator ','
set xlabel 'Time (s)'
set ylabel 'Response Time (ms)'
set title 'Response Time Trace log'
set xrange [0:]
set yrange [0:]
plot "$experimentFolder/allclientsplot.log" every 10 u (\$1/1000):(\$5/1000000) w l title "$experimentId"
EOF

python scripts/prepareThroughputTrace.py $experimentFolder/allclientsplot.log $experimentFolder/throughput.csv

echo "  Generating throughput trace with gnuplot"
gnuplot << EOF
set terminal png
set output '$experimentFolder/throughput.png'
set datafile separator ','
set xlabel 'Time (s)'
set ylabel 'Throughput'
set title 'Throughput Trace log'
set xrange [0:]
set yrange [0:]
plot "$experimentFolder/throughput.csv" u 1:2 w l title "$experimentId"
EOF

echo "Writing means and stds"
python scripts/meanAndStd.py $experimentFolder/allclientsplot.log $experimentFolder/allservers.log $experimentFolder/throughput.csv > $experimentFolder/meanAndStd.csv
