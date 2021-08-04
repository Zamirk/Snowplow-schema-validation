
### Snowplow



This application requires a postgress database. 
Use the following command to create a database with Docker with db *sludg* and password *mysecretpassword*. 

`docker run --name postgres -e POSTGRES_PASSWORD=mysecretpassword  -e POSTGRES_DB=sludg -d -p 5432:5432 postgres`

When ran, flyway should automatically create the DB Schema if the tables do not exist already. Table design can be found in 
`V1__create_json_schema.sql` under resources/db/migration

Application is configured to run on port 8080 @http://localhost:8080/

```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```


`
Post a schema
`

curl http://localhost:8080/schema/config-schema -X POST -d @api/src/main/resources/config-schema.json

`
Get a schema
`

curl http://localhost:8080/validate/config-schema -X POST -d @api/src/main/resources/config.json
