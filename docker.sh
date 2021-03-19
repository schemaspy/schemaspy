#!/usr/bin/env bash
# Exit on failure
set -e

REPO=schemaspy/schemaspy

function build() {
    echo "Building docker image ${REPO}:${1:-local}"
    docker build -t ${REPO}:${1:-local} --build-arg GIT_BRANCH=$TRAVIS_BRANCH --build-arg GIT_REVISION=$TRAVIS_COMMIT .
}

function deploy() {
    if [[ "${TRAVIS_PULL_REQUEST}" == "false" ]] &&
       [[ -n ${DOCKER_USER} ]] &&
       [[ -n ${DOCKER_PASS} ]]; then
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
    else
        echo "Skipping deploy"
    fi
}

function pullLogin() {
    if [[ "${TRAVIS_PULL_REQUEST}" == "false" ]] &&
       [[ -n ${DOCKER_PULL_USER} ]] &&
       [[ -n ${DOCKER_PULL_PASS} ]]; then
        docker login -u ${DOCKER_PULL_USER} -p ${DOCKER_PULL_PASS}
    else
        echo "PR or Pull credentials missing"
    fi
}


case "$1" in
    build)
        build $2
        ;;
    deploy)
        deploy
        ;;
    pullLogin)
        pullLogin
        ;;
    *)
        echo "Bad argument: $1"
        exit 1
esac