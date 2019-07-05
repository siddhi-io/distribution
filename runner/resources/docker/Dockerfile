# ------------------------------------------------------------------------
#
# Copyright 2019 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

# set base Docker image to AdoptOpenJDK Alpine Docker image
FROM adoptopenjdk/openjdk8:jdk8u192-b12-alpine
MAINTAINER Siddhi IO Docker Maintainers "siddhi-dev@googlegroups.com"

# set user configurations
ARG USER=siddhi_user
ARG USER_ID=802
ARG USER_GROUP=siddhi_io
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# set siddhi runner configurations
ARG RUNTIME_SERVER_PACK
ARG RUNTIME_SERVER_HOME=${USER_HOME}/${RUNTIME_SERVER_PACK}
# set SIDDHI-IO EULA
ARG MOTD="\n\
Welcome to Siddhi Docker resources.\n\
------------------------------------ \n\
This Docker container comprises of a SIDDHI-IO product, running with its latest GA release \n\
which is under the Apache License, Version 2.0. \n\
Read more about Apache License, Version 2.0 here @ http://www.apache.org/licenses/LICENSE-2.0.\n"

# install required packages
RUN  apk add --update --no-cache netcat-openbsd && \
     rm -rf /var/cache/apk/*

# create a user group and a user
RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

## install the Siddhi Distribution to user's home directory

# use the Siddhi Runner Distribution available locally
COPY ${RUNTIME_SERVER_PACK}.zip/ ${RUNTIME_SERVER_HOME}/
RUN chown -R siddhi_user:siddhi_io ${RUNTIME_SERVER_HOME}/

# set the user and work directory
USER ${USER_ID}
WORKDIR ${USER_HOME}

# set environment variables
ENV RUNTIME_SERVER_HOME=${RUNTIME_SERVER_HOME} \
    WORKING_DIRECTORY=${USER_HOME}

ARG BUNDLE_JAR_DIR=./files/lib
ARG LIB=${RUNTIME_SERVER_HOME}/lib

# copy entrypoint bash script to user home
COPY --chown=siddhi_user:siddhi_io init.sh ${WORKING_DIRECTORY}/

RUN mkdir -p ${WORKING_DIRECTORY}/extend_lib_volume && chown -R siddhi_user:siddhi_io ${WORKING_DIRECTORY}/extend_lib_volume

# expose ports
EXPOSE 9090 9443 9712 9612 7711 7611 7070 7443

STOPSIGNAL SIGINT

RUN sh -c 'unzip -q ${RUNTIME_SERVER_HOME}/${RUNTIME_SERVER_PACK}.zip'

RUN chmod +x /home/siddhi_user/init.sh

ENTRYPOINT ["/home/siddhi_user/init.sh", "--"]