# Neo4j-REST-api-backend

A Neo4j REST-api built with Java.

## Usage

Clone the repo

```bash
git clone https://github.com/H-Richard/Neo4j-REST-api-backend
```

Open up `src/main/java/ca/utoronto/utm/mcs/App.java`

Setup `App.java` as follows:

```java
Driver driver = GraphDatabase.driver("bolt://localhost:7687", 
                                            AuthTokens.basic("neo4j", "your password here"));
```


```
PUT /api/v1/addActor
```

Request Body:

```Json
{
  "name": "Emma Stone",
  "actorId": "9348"
}
```

Response:

- 200 OK for a successful add
- 400 BAD REQUEST if the request body is improperly formatted or missing required information
- 500 INTERNAL SERVER ERROR if save or add was unsuccessful (Java Exception is thrown)


```
PUT /api/v1/addMovie
```
