<!--
  ~  Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  -->

Siddhi Distribution
======================

  [![Jenkins Build Status](https://wso2.org/jenkins/view/extensions/job/siddhi/job/distribution/badge/icon)](https://wso2.org/jenkins/view/extensions/job/siddhi/job/distribution)
  [![GitHub (pre-)release](https://img.shields.io/github/release-pre/siddhi-io/distribution.svg)](https://github.com/siddhi-io/distribution/releases)
  [![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/siddhi-io/distribution.svg)](https://github.com/siddhi-io/distribution/releases)
  [![GitHub last commit](https://img.shields.io/github/last-commit/siddhi-io/distribution.svg)](https://github.com/siddhi-io/distribution/commits/master)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Introduction ##

[Siddhi](https://siddhi-io.github.io/siddhi/) is a Streaming and Complex Event Processing engine that listens to events from data streams, detects complex conditions described via a Streaming SQL language, and triggers actions.
This repo contains necessary source code which creates the Siddhi runner and Siddhi tooling distributions by embedding Siddhi library in it.

Siddhi maintains two distributions as Siddhi-Runner and Siddhi-Tooling distribution.
* Siddhi-Runner distribution bundles the Siddhi runtime which runs the Siddhi logic in a production environment.

* Siddhi-Tooling distribution bundles the tooling editor profile which can be used for developing, testing and debugging Siddhi applications before moving to production.

## Build from Source ##

### Prerequisites
* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK 8](http://openjdk.java.net/install/) (Java 8 should be used for building in order to support both Java 8 and Java 11 at runtime)
* [Maven 3.5.x version](https://maven.apache.org/install.html)
* [Docker 17.09+](https://docs.docker.com/install/) (You enable docker dependent build profiles with `-Dwith-docker` maven property)
* [Node.js](https://nodejs.org/en/)

### Steps to Build ###
1. Get a clone or download source from [Github](https://github.com/siddhi-io/distribution.git)

    ```bash
    git clone https://github.com/siddhi-io/distribution.git
    ```

1. Run the Maven command ``mvn clean install`` from the root directory

2. Find the Siddhi distributions in the following directories,<br />
    Runner:     runner/target<br />
    Tooling:    tooling/target<br />

## Try Siddhi Runner & Tooling ##

### Siddhi Runner Distribution ###
Download the latest released distributions from [here](https://github.com/siddhi-io/distribution/releases)

1. Extract the Siddhi runner distribution.
2. Unzip the siddhi-runner-x.x.x.zip.
3. Navigate to the <RUNNER_HOME>/bin directory. Start SiddhiApps with the runner config by executing the following commands from the distribution directory
     
     Linux/Mac : 
     ```bash 
     ./bin/runner.sh -Dapps=<siddhi-file> -Dconfig=<config-yaml-file>
     ```
     
     Windows : 
     ```bash
     bin\runner.bat -Dapps=<siddhi-file> -Dconfig=<config-yaml-file>
     ```
   
#### Running Multiple SiddhiApps in one runner ####
To run multiple SiddhiApps in one runtime, have all SiddhiApps in a directory and pass its location through `-Dapps` parameter as follows,<br/>

```bash
-Dapps=<siddhi-apps-directory>
```
   
>"Always use **absolute path** for SiddhiApps and runner configs."
       Providing absolute path of SiddhiApp file, or directory in `-Dapps` parameter, and when providing the Siddhi runner config yaml on `-Dconfig` parameter while starting Siddhi runner.


### Siddhi Tooling Distribution ###
Download the latest released distributions from [here](https://github.com/siddhi-io/distribution/releases)

1. Extract the Siddhi tooling distribution.
2. Navigate to the <TOOLING_HOME>/bin directory and issue the following command:<br/>
   For Windows: ```tooling.bat``` <br/>
   For Linux: ```./tooling.sh``` <br/>
3. Access the Editor UI using the following URL.<br/>
   http://localhost:<EDITOR_PORT>/editor   (e.g: https://localhost:9390/editor)
   

Please refer the [link](https://siddhi.io/en/v5.1/docs/siddhi-as-a-local-microservice/) for more details.

## Reporting Issues ##

We encourage you to report issues. However, please consider searching the existing issues in GitHub and communicating in Siddhi-Dev Google Group if you are unsure if it is a bug before filing a new issue.

To file a non-security issues:

1. Click the **Issues** tab in the GitHub repository,

2. Click the **New Issue** button,

3. Fill out all sections in the issue template and submit.

## Communicating with the team

[Siddhi-Dev Google Group](https://groups.google.com/forum/#!forum/siddhi-dev) Group is the main Siddhi project discussion forum for developers.

Users can use [Siddhi-User Google Group](https://groups.google.com/forum/#!forum/siddhi-user) to raise any queries and get some help to achieve their use cases.

[StackOverflow](https://stackoverflow.com/questions/tagged/siddhi) also can be used to get support, and GitHub for issues and code repositories.

