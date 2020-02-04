# Gentics Mesh Alexa Skill Server

This project contains an example Alexa skill server which was build using Vert.x, Dagger and Gentics Mesh.
The skill server is hooked to the demo instance of Gentics Mesh which provides the content that will be used by the skill.

## Example Phrases

> `What is the price of a Tesla?`

> `Reserve a Tesla`

> `Ask Gentics what is the price of a Tesla`

## Building 

```
./mvnw clean package -DskipTests
docker build -t gentics/mesh-alexa-skill-demo:latest .
```

## Links

* [Blogpost](https://getmesh.io/blog/voice-enabled-content/)

