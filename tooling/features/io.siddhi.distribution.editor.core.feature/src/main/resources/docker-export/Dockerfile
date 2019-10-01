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

# use siddhi-runner-base
FROM {{SIDDHI_RUNNER_BASE_IMAGE}}
MAINTAINER Siddhi IO Docker Maintainers "siddhi-dev@googlegroups.com"

ARG HOST_BUNDLES_DIR=./bundles
ARG HOST_JARS_DIR=./jars
ARG HOST_APPS_DIR=./siddhi-files
ARG JARS=${RUNTIME_SERVER_HOME}/jars
ARG BUNDLES=${RUNTIME_SERVER_HOME}/bundles
ARG APPS=${RUNTIME_SERVER_HOME}/deployment/siddhi-files
ARG CONFIG_FILE=./configurations.yaml
ARG CONFIG_FILE_PATH=${HOME}/configurations.yaml

# copy bundles & jars to the siddhi-runner distribution
{{APPS_BLOCK}}
{{JARS_BLOCK}}
{{BUNDLES_BLOCK}}
{{CONFIGURATION_BLOCK}}

{{ENV_BLOCK}}

# expose ports
{{EXPORT_PORTS_BLOCK}}

STOPSIGNAL SIGINT

ENTRYPOINT ["/home/siddhi_user/siddhi-runner/bin/runner.sh" {{CONFIGURATION_PARAMETER_BLOCK}}]
