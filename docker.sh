#!/usr/bin/env bash
# Exit on failure
set -e

REPO=schemaspy/schemaspy

function build() {
    echo "Building docker image"
    docker build -t ${REPO}:snapshot --build-arg GIT_BRANCH=$TRAVIS_BRANCH --build-arg GIT_REVISION=$TRAVIS_COMMIT .
}

function deploy() {
    docker login -u ${DOCKER_USER} -p ${DOCKER_PASS}
    echo "Deploying snapshot"
    docker push ${REPO}:snapshot
    if [[ ${TRAVIS_TAG} =~ ^v[0-9]+\.[0-9]+\.[0-9]+.*$ ]]; then
        docker tag ${REPO}:snapshot ${REPO}:${TRAVIS_TAG:1}
        echo "Deploying release ${TRAVIS_TAG:1}"
        docker push ${REPO}:${TRAVIS_TAG:1}
        echo "Deploying latest"
        docker tag ${REPO}:${TRAVIS_TAG:1} ${REPO}:latest
        docker push ${REPO}:latest
    fi
}

if [[ "${TRAVIS_BRANCH}" == "master" ]] &&
   [[ "${TRAVIS_PULL_REQUEST}" == "false" ]] &&
   [[ -n ${DOCKER_USER} ]] &&
   [[ -n ${DOCKER_PASS} ]]; then
    case "$1" in
        build)
            build
            ;;
        deploy)
            deploy
            ;;
        *)
            echo "Bad argument: $1"
            exit 1
    esac
else
    echo "Skipping $1"
fi