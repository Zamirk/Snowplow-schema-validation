
### Snowplow: Schema validator



This application requires a postgress database. 
Use the following command to create a database with Docker with db *sludg* and password *mysecretpassword*. 

`docker run --name postgres -e POSTGRES_PASSWORD=mysecretpassword  -e POSTGRES_DB=sludg -d -p 5432:5432 postgres`

When exacuted, flyway should automatically create the DB Schema if the tables do not exist already. Table design can be found in 
`V1__create_json_schema.sql` under resources/db/migration

Application is configured to run on port 8080 @http://localhost:8080/, this can be changed in application.conf

```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```

All test data can be found under resources. Json data is cleaned and nulls are removed.


Console output when running service

```
[io-compute-7] INFO  snowplow.Main - Application start. 
[io-compute-13] INFO  snowplow.Main - Creating db connection. 
[io-compute-13] INFO  o.f.c.i.l.VersionPrinter - Flyway Community Edition 7.5.2 by Redgate 
[io-compute-13] INFO  c.z.h.HikariDataSource - HikariPool-1 - Starting... 
[io-compute-13] INFO  c.z.h.HikariDataSource - HikariPool-1 - Start completed. 
[io-compute-13] INFO  o.f.c.i.d.b.DatabaseType - Database: jdbc:postgresql://localhost:5432/sludg (PostgreSQL 12.2) 
[io-compute-13] INFO  o.f.c.i.c.DbValidate - Successfully validated 1 migration (execution time 00:00.013s) 
[io-compute-13] INFO  o.f.c.i.c.DbMigrate - Current version of schema "public": 1 
[io-compute-13] INFO  o.f.c.i.c.DbMigrate - Schema "public" is up to date. No migration necessary. 
[io-compute-13] INFO  o.h.b.c.n.NIO1SocketServerGroup - Service bound to address /0:0:0:0:0:0:0:0:8080 
[io-compute-13] INFO  o.h.b.s.BlazeServerBuilder - 
  _   _   _        _ _
 | |_| |_| |_ _ __| | | ___
 | ' \  _|  _| '_ \_  _(_-<
 |_||_\__|\__| .__/ |_|/__/
             |_| 
[io-compute-13] INFO  o.h.b.s.BlazeServerBuilder - http4s v0.15.1 on blaze v0.15.1 started at http://[::]:8080/ 
```

To run Tests, do ```sbt test```

```
[info] Non-compiled module 'compiler-bridge_2.13' for Scala 2.13.5. Compiling...
[info]   Compilation completed in 7.37s.
[info] compiling 3 Scala sources to /home/samir/Desktop/Snowplow-schema-validation/api/target/scala-2.13/test-classes ...
[info] SchemaProcessingEndpointSpec:
[info] Calling on /Schema/Id 
[info]   with a POST request 
[info]   - with a valid shema should return a 201 status
[info]   - with a valid schema should return json response
[info]   - with an invalid schema should return not found status code
[info]   with a GET request 
[info]   - with a valid schema id should return an Ok status
[info]   - with a valid schema id should return json response of the data
[info]   - with an invalid schema id should return not found status code
[info] SchemaValidateEndpointSpec:
[info] Calling on /Schema/Id 
[info]   with a POST request 
[info]   - with an invalid document schema should return a bad request status code
[info]   - with a valid document schema should return a 200 status code
[info]   - with a valid Json document but a missing schema should should return a Not found status code
[info]   - with a valid Json document that is invalid against the stored schema, should return a Json error
[info] Run completed in 1 second, 707 milliseconds.
[info] Total number of tests run: 10
[info] Suites: completed 2, aborted 0
[info] Tests: succeeded 10, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.

```


 - Insert a schema


curl http://localhost:8080/schema/config-schema -X POST -d @api/src/main/resources/config-schema.json

Response

`{"action":"uploadSchema","id":"/config-schema","status":"success"}`


 - Retrieve a schema


curl http://localhost:8080/schema/config-schema -X GET 

Response

`{"id":"/config-schema","jsonschema":"{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"type\":\"object\",\"properties\":{\"source\":{\"type\":\"string\"},\"destination\":{\"type\":\"string\"},\"timeout\":{\"type\":\"integer\",\"minimum\":0,\"maximum\":32767},\"chunks\":{\"type\":\"object\",\"properties\":{\"size\":{\"type\":\"integer\"},\"number\":{\"type\":\"integer\"}},\"required\":[\"size\"]}},\"required\":[\"source\",\"destination\"]}"}`

- Validating a document against a schema

curl http://localhost:8080/validate/config-schema -X POST -d @api/src/main/resources/config.json

`{"action":"validateDocument","id":"/config-schema","status":"success"}`

- Validating a document against a missing schema

curl http://localhost:8080/validate/config-schema2 -X POST -d @api/src/main/resources/config.json

Response

`"The schema was not found"`, 404 status

- Validating with a document that does not match the schema

curl http://localhost:8080/validate/config-schema -X POST -d @api/src/main/resources/config-no-match.json

Response

`{"action":"validateDocument","id":"/config-schema","status":"error","message":"Message: instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])"}s`

More test examples can be found in SchemaProcessingEndpointSpec and SchemaValidateEndpointSpec