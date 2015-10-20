#!bin/bash

#run from home/zdina

PRTNUMBER = 12341
username = "zdina"
folder = "/mnt/local/${username}"

mkdir $folder

cp . $folder

tar xjf postgresql-9.4.4.tar.bz2
cd postgresql-9.4.4/
./configure --prefix="${folder}/postgres"
make
make install

export LD_LIBRARY_PATH=$folder/postgres/lib
export LANG="en_US.utf8"
export LC_CTYPE="en_US.utf8"

$folder/postgres/bin/initdb -D $folder/postgres/db/

$folder/postgres/bin/postgres -D $folder/postgres/db/ -p $PORTNUMBER -i -k $folder >$folder/db.out 2>&1 &

$folder/postgres/bin/createdb -p $PORTNUMBER -h $folder

echo "DB created"
$folder/postgres/bin/psql -p $PORTNUMBER -h $folder << EOF
create database asldb;
EOF

$folder/postgres/bin/psql -p $PORTNUMBER -h $folder -U dinazverinski asldb < $folder/pg.sql

export PGDATA="/mnt/local/zdina/postgres/db"
export PGPRT=$PORTNUMBER

#got add "host  all  all 0.0.0.0/0 trust" to top of pg_hba.conf
