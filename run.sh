docker run -it --rm --name schemaspy \
    -e DB_TYPE=pgsql \
    -e DB_NAME=$DB_NAME \
    -e SQL_HOST=$SQL_HOST \
    -e SQL_PORT=$SQL_PORT \
    -e SQL_USER=$SQL_USER \
    -e SQL_PASSWORD=$SQL_PASSWORD \
    -e SCHEMASPY_OUTPUT=/tmp/output \
    -v ${PWD}/output:/tmp/output \
    carljmosca/schemaspy:sha-5dc946e