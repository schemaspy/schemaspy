## Docker image for SchemaSpy

### Build info

We currently build and publish release image and snapshot images.
This are distinguished by tag.

Alpine base with:
* openjdk8-jre
* OpenSans font
* graphviz

### Usage
Currently the build contains 0 driver.

It exposes 3 volumes that can be mounted

/driver /output /config

The container will load all libraries drivers, it will have output set to /output

Container will exit when finished.

Example:  
`docker run -v "$PWD/drivers:/driver" -v "$PWD/output:/output" schemaspy:[tag] -t mysql -db [database] -u [user] -p [password] -host [host] -port [port]` 

The example above assumes that the mysql jdbc driver is located in $PWD/drivers  
and output will be written to $PWD/output.

### Caveats
If you are new to docker localhost is inside the container and not the dockerhost.  
-net=host probably doesn't work with docker toolbox or docker for (mac|windows).

## Issues
Create an issue in the [github project](https://github.com/schemaspy/schemaspy/issues)

