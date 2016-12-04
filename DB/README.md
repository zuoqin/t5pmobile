# t5pmobile

d:
cd D:\datomic-pro-0.9.5350

https://www.youtube.com/watch?v=kd1yTmx7m2A


## Run a Transactor
http://docs.datomic.com/run-transactor.html

bin\transactor config\samples\dev-transactor-template.properties
Starting datomic:dev://localhost:4334/<DB-NAME>, storing data in: data ...
System started datomic:dev://localhost:4334/<DB-NAME>, storing data in: data


## Starting Clojure REPL
bin\repl

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

[:find ?year
 :where
 [?t :message/senddate ?date]
 [((fn [dt] (+ dt 1900)) ?date) ?year]]


[:find ?m
 :where
 [?m :message/recipients ?e]
 [?e :employee/english "Nacho"]

]


[:find ?date ?en ?ch ?m ?type
 :in $ ?login
 :where
 [?m :message/senddate ?date]
 [?m :message/Recipients ?r]
 [?r :recipient/type ?t]
 [?r :recipient/employee ?e]
 [?u :user/employee ?e]
 [?u :user/code ?login]
 [?m :message/chinese ?ch]
 [?m :message/english ?en]
 [((fn [dt] (if (= dt :recipient.type/to) "To" "CC")) ?t) type]
]

[:find (count ?e)
 :where
 [?m :message/recipients ?e]
 [?e :employee/english "Nacho"]
 [(count ?e) ?cnt]
 [(> ?cnt 0)]
]


[:find ?m
 :where
 [?m :message/senddate "Thu Aug 18 18:22:14 CST 2016"]
 [?m :message/recipients ?e]
 [?e :employee/english "Nacho"]]


 [:find ?m
 :where
 [?m :message/senddate #inst "2013-10-25T15:12:06.500-00:00"]
 [?m :message/status 4]
 [?m :message/recipients ?e]
 [?m :message/sender ?e]
 [?e :employee/english "Nacho"]
]



 [:find ?m ?year
 :where
 [?m :message/senddate ?date]
 [?m :message/status 4]
 [?m :message/recipients ?e]
 [?m :message/sender ?e]
 [?e :employee/english "Nacho"]
 [((fn [dt] (.getTime dt) ) #inst "2018-08-19T00:00:00.000-00:00") ?year]

]

 [:find ?m ?year
 :where
 [?m :message/senddate ?date]
 [?m :message/status 4]
 [?m :message/recipients ?e]
 [?m :message/sender ?e]
 [?e :employee/english "Nacho"]
 [((fn [dt] (.getTime dt) ) #inst "2016-07-18T00:00:00.000-00:00") ?year]
]

[:find ?m ?year
 :where
 [?m :message/senddate ?date]
 [?m :message/status 4]
 [?m :message/recipients ?r]
 [?r :recipient/employee ?e]
 [?m :message/sender ?e]
 [?e :employee/english "Nacho"]
 [((fn [dt] (.getTime dt)) ?date) ?year]
 [(> ?year 534550400000)]
 [(< ?year 9534636800000)]
]

[:find ?m ?year
 :where
 [?m :message/senddate ?date]
 [((fn [dt] (.getTime dt)) ?date) ?year]
 [(> ?year 1534464000000)]
 [(< ?year 1534550400000)]
]


[:find ?m ?year
 :where
 [?m :message/senddate ?date]
 [?m :message/recipients ?e]
 [?e :employee/english "Nacho"]
 [((fn [dt] (.getTime dt)) ?date) ?year]
 [(> ?year 1468800000000)]
 [(< ?year 1468886400000)]
]


[:find ?entity ?to ?isCc ?isBcc ?e ?c ?s ?b
 :in $ ?entity
 :where
 [?entity :message/To ?to]
 [(missing? $ ?entity :message/CC) ?isCc]
 [(missing? $ ?entity :message/Bcc) ?isBcc]
 [?entity :message/english ?e]
 [?entity :message/chinese ?c]
 [?entity :message/senddate ?s]
 [?entity :message/body ?b]
 [?entity]
]

[:find ?eid
 :in $ ?eid
 :where
 [?eid]
]



[:find ?eid
 :in $ [?t ...]
 :where
 [?eid :db/ident ?t]
]
[?t ...] [:recipient.type/to :recipient.type/cc :recipient.type/bcc]


17592186080614 two recipients
[:find ?e
 :in $ ?empcode
 :where
 [?e :emp_new/empcode ?empcode]
]

