# Gentics Mesh - Musetech Demo Importer

This import will import the content for the [Gentics Mesh Musetech Demo](https://musetech.getmesh.io/en/welcome).


## Docker

The docker image 

Environment variables: `gentics/mesh-musetech-demo-importer`  can be used to import the content.

| Name                                        | Description                                                 | Default Value |
|------------------------------------------|---------------------------------------------------------|---------------------|
| MUSETECH_MESH_HOST       | Gentics Mesh Server hostname              | localhost        |
| MUSETECH_MESH_PORT       | Gentics Mesh Server Port                        |  8080               |
| MUSETECH_MESH_SSL           | SSL Flag for Gentics Mesh Server           | false                |
| MUSETECH_PROJECT_NAME | Project name to be used for importing | musetech       |