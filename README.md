# java-sql

query a ms sql server 

Usage
$>java -jar sqlapp-1.0.0.jar 

Help:

Missing required options: u, pw, i, q
usage: app
 -d,--domain <arg>      (optional) domain of the user, like
                        WINDOMAIN\username
 -db,--database <arg>   (optional) remote port, defaults to master
 -f,--filename <arg>    (optional) save result to file
 -h,--hostname <arg>    (optional) databse host, defaults to localhost
 -i,--instance <arg>    (required) sql server instance
 -p,--port <arg>        (optional) remote port, defaults to 1433
 -pw,--password <arg>   (required) user password, special characters like
                        ! don't need to be escaped with '!', without tics
                        esapce
 -q,--sql <arg>         (required) SQL Query
 -s,--silent            (optional) results not printed to stdout
 -u,--username <arg>    (required) username to connect to db
