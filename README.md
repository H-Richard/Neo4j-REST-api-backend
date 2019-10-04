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
