
### Snowplow



This application requires a postgress database. 
Use the following command to create a database with Docker with db *sludg* and password *mysecretpassword*. 

`docker run --name postgres -e POSTGRES_PASSWORD=mysecretpassword  -e POSTGRES_DB=sludg -d -p 5432:5432 postgres`

When ran, flyway should automatically create the DB Schema if the tables do not exist already. Table design can be found in 
`V1__create_json_schema.sql` under resources/db/migration