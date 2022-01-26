# Assignment-3
third assignment of system programming course
running code: 10000 = port 5= number of threads
Reactor:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.Main.MainReactor" -Dexec.args="10000 5"
TPC:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.Main.MainTPC" -Dexec.args="10000"

commands:
REGISTER aa a 11-11-2012
LOGIN aa a 1
FOLLOW 1 aa
LOGOUT
POST my first post
PM aa hey, how are you?
LOGSTAT
STAT aa bb cc dd ee
BLOCK aa

filtered words:
in an array in bgu.spl.net.api.Commands
