#!/bin/bash
echo "Push new docker image for feature $BRANCH to DockerHub."
echo "$DOCKER_PASSWD" | docker login -u "$DOCKER_USER" --password-stdin
docker tag dniel/forwardauth dniel/forwardauth:$TRAVIS_TAG
docker tag dniel/forwardauth dniel/forwardauth:$BRANCH

# docker push dniel/forwardauth:$TRAVIS_TAG
# docker push dniel/forwardauth:$BRANCH
