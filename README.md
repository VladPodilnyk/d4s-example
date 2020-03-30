# D4S example

Example `d4s` project presented at ScalaUA 2020.
Features:
- [_d4s_](https://github.com/PlayQ/d4s) - the brand new hyper-pragmatic library for DynamoDB.
- [izumi](https://izumi.7mind.io/latest/release/doc/index.html) - Scala libraries that makes your life easier :^)
- [Bifunctor Tagless Final](https://github.com/7mind/izumi/blob/v0.10.0-M5/fundamentals/fundamentals-bio/src/main/scala/izumi/functional/bio/package.scala)

__Note__: To launch tests that require postgres ensure you have a docker daemon running in the background.
 
---
### Usage
In case you want to run service localy you could use `docker-compose` to run docker container with DynamoDB.
You don't need to do this if you need to run test, because container will be created automatically using `DIStage Testkit`.

Project demonstrates an implementation of Leaderboard service with the following API:
```
GET  /LeaderboardService/ladder - get users with score
POST /LeaderboardService/ladder/{id}/{score} - submit score for the particular user
GET  /LeaderboadService/profiles/{id} - get user's profile with rank
POST /LeaderboardService/profiles/{id} - create profile for a user
```

To run service with sbt cli use the following commands 
```
sbt "runMain leaderboard.MainProdD4S"
```


Example session using `curl`
```
curl -X POST http://localhost:8080/LeaderboardService/ladder/50753a00-5e2e-4a2f-94b0-e6721b0a3cc4/100
curl -X POST http://localhost:8080/LeaderboardService/profile/50753a00-5e2e-4a2f-94b0-e6721b0a3cc4 -d '{"userName": "RoboCop", "description": "Cop"}'
curl -X GET http://localhost:8080/LeaderboardService/profile/50753a00-5e2e-4a2f-94b0-e6721b0a3cc4
curl -X GET http://localhost:8080/LeaderboardService/ladder
```

