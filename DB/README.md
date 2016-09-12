# t5pmobile

d:
cd D:\datomic-pro-0.9.5350

https://www.youtube.com/watch?v=kd1yTmx7m2A


## Run a Transactor
http://docs.datomic.com/run-transactor.html

bin\transactor config\samples\dev-transactor-template.properties
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

schema_rdr = new FileReader("E:/T5PSVN/branches/research-mobile/WEB/t5pmobile/DB/take5-schema.edn");
schema_tx = Util.readAll(schema_rdr).get(0);

txResult = conn.transact(schema_tx).get();

data_rdr = new FileReader("E:/T5PSVN/branches/research-mobile/WEB/t5pmobile/DB/output.clj");

data_tx = Util.readAll(data_rdr).get(0);

data_rdr.close();

txResult = conn.transact(data_tx).get();

Peer.query("[:find ?c :where [?c :user/code]]",conn.db());

Peer.deleteDatabase(uri);

[:find ?c
 :where
 [?c :employee/english ?c_name]
 [?c :employee/payrollgroup ?p]
 [?p :payrollgroup/english "MACAU "]
]

[:find ?entity ?name ?score
 :where
 [(fulltext $ :employee/english "ad*") [[?entity ?name ?tx ?score]]]
]


[:find ?employee
 :in $ ?reference ?name
 :where
 [?employee ?reference ?name]
]