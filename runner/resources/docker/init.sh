#!/bin/sh
# ------------------------------------------------------------------------
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
# ------------------------------------------------------------------------

set -e

# capture Docker container IP from the container's /etc/hosts file
docker_container_ip=$(awk 'END{print $1}' /etc/hosts)

# capture Docker container IP from the container's /etc/hosts file
docker_container_ip=$(awk 'END{print $1}' /etc/hosts)

# volume mounts
extend_lib_volume=${WORKING_DIRECTORY}/extend_lib_volume

# check if the siddhi_io non-root user home exists
test ! -d ${WORKING_DIRECTORY} && echo "Siddhi Runner Docker non-root user home does not exist" && exit 1

# check if the Siddhi Runner home exists
test ! -d ${RUNTIME_SERVER_HOME} && echo "Siddhi Runner Home does not exist" && exit 1

# a grace period for mounts to be setup
echo "Waiting for all volumes to be mounted..."
sleep 5

# copy any configuration changes mounted to config_volume
if [ "$(ls -A ${extend_lib_volume})" ]
    then cp -RL ${extend_lib_volume}/* ${RUNTIME_SERVER_HOME}/lib
else
    echo "No extended jars to be mounted to ${RUNTIME_SERVER_HOME}/lib"
fi
exit_func() {
        exit 1
}
trap exit_func SIGTERM SIGINT

# start the Siddhi Runner profile
exec ${RUNTIME_SERVER_HOME}/bin/runner.sh "$@"
