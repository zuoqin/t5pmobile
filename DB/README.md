# t5pmobile

d:
cd D:\datomic-pro-0.9.5350

https://www.youtube.com/watch?v=kd1yTmx7m2A


## Run a Transactor
http://docs.datomic.com/run-transactor.html

D:\datomic-pro-0.9.5350>bin\transactor config\samples\dev-transactor-template.properties
Starting datomic:dev://localhost:4334/<DB-NAME>, storing data in: data ...
System started datomic:dev://localhost:4334/<DB-NAME>, storing data in: data

## Starting the Console
d:
cd D:\datomic-pro-0.9.5350
bin\console -p 8080 dev datomic:dev://localhost:4334/

then open in the browser: http://localhost:8080/browse

## Starting shell
d:
cd D:\datomic-pro-0.9.5350
bin\shell

import datomic.Peer;
import datomic.Connection;
import datomic.Util;

uri = "datomic:dev://localhost:4334/take5.test01";
Peer.createDatabase(uri);
conn = Peer.connect(uri);

