# Siddhi Docker Artifacts

Exported Docker artifacts from Siddhi Tooling can be used to manually 
build **Siddhi Runner** docker image bundling the Siddhi applications, configurations and dependency jars/bundles configured during the Export Docker phase.


## Directory Structure

Directory structure of the exported docker artifacts zip file is as follows.

```
.
├── bundles
│   ├── <BUNDLE_FILE_1>.jar
│   └── <BUNDLE_FILE_2>.jar
├── configurations.yaml
├── Dockerfile
├── jars
│   └── <JAR_FILE_1>.jar
├── README.md
└── siddhi-files
    ├── <SIDDHI_FILE_1>.siddhi
    └── <SIDDHI_FILE_2>.siddhi
```


Purpose of each file in the above archive is as follows.

- **README.md**: This readme file.
- **Dockerfile**: Docker image build script which contains all commands to assemble Siddhi Runner image. 
- **siddhi-files**: Directory which contains Siddhi files.
- **bundles**: Directory maintained for OSGI bundles which needs to be copied to Siddhi Runner image during build phase.
- **jars**: Directory maintained for Jar files which may not have their corresponding OSGi bundle implementation. These Jars will be converted as OSGI bundles and copied to Siddhi Runner image during build phase.

## How to Run?

To run this archive, you need **Docker** installed in your environment.

If the docker prerequisite is met, follow the steps mentioned below to create and run the docker image.

Let the extracted directory be referred as `<DOCKER_BUILD_HOME>` within this document.

1. Go to `<DOCKER_BUILD_HOME>` directory.

2. Run the following command to build the Docker image.

```
docker build -t {{DOCKER_IMAGE_NAME}} .
```

3. Run the following command to run the Docker image.(

Note: Container ports are forwarded to its specific ports in the host machine assuming those ports are not in use.
   
```
docker run -it {{BIND_PORTS}} {{DOCKER_IMAGE_NAME}}
```


