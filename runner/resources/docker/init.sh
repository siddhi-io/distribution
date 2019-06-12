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

# check if the siddhi_io non-root user home exists
test ! -d ${WORKING_DIRECTORY} && echo "Siddhi Runner Docker non-root user home does not exist" && exit 1

# check if the Siddhi Runner home exists
test ! -d ${RUNTIME_SERVER_HOME} && echo "Siddhi Runner Home does not exist" && exit 1

exit_func() {
        exit 1
}
trap exit_func SIGTERM SIGINT

# start the Siddhi Runner profile
exec ${RUNTIME_SERVER_HOME}/bin/runner.sh "$@"
