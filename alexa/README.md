# Gentics Mesh Alexa Skill Server

This project contains an example Alexa skill server which was build using Vert.x, Dagger and Gentics Mesh.
The skill server is hooked to the demo musetech instance of Gentics Mesh which provides the content that will be used by the skill.

## Example Phrases

> `What is the price of a Tour?`

> `Reserve the next tour for me`

> `Ask Muse Tech what is the price of a tour`

## Building 

```
./mvnw clean package -DskipTests
docker build -t gentics/mesh-musetech-alexa-skill:latest .
```

## Links

* [Blogpost](https://getmesh.io/blog/voice-enabled-content/)

