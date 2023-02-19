[![Logotype](https://github.com/schemaspy/schemaspy/blob/master/docs/source/_static/schemaspy_logo.png)](https://schemaspy.org/)

[![Development](https://github.com/schemaspy/schemaspy/actions/workflows/development.yml/badge.svg)](https://github.com/schemaspy/schemaspy/actions/workflows/development.yml)
[![Documentation Status](https://readthedocs.org/projects/schemaspy/badge/?version=latest)](http://schemaspy.readthedocs.io/en/latest/?badge=latest)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.schemaspy%3Aschemaspy&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.schemaspy%3Aschemaspy&metric=coverage)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)
[![Gitter](https://badges.gitter.im/schemaspy/schemaspy.svg)](https://gitter.im/schemaspy/schemaspy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Docker Pulls](https://img.shields.io/docker/pulls/schemaspy/schemaspy.svg)](https://hub.docker.com/r/schemaspy/schemaspy/)

**SchemaSpy** is a database metadata analyzer. It helps your database administors and developers visualize, navigate and understand your data model. With an easy-to-use HTML-based report, traversing the entity-relationship diagram has never been simpler. Our product showcase is available at http://schemaspy.org/sample/index.html.

[![SchemaSpy sample](http://schemaspy.org/img/example_page_table_relationships.png)](http://schemaspy.org/sample/index.html)

# Installation

SchemaSpy is a stand alone tool. Just download the lastest
[JAR file](https://github.com/schemaspy/schemaspy/releases/download/v6.1.0/schemaspy-6.1.0.jar)
or [Docker image](https://hub.docker.com/r/schemaspy/schemaspy/) and you're
ready to go!

```
curl -L https://github.com/schemaspy/schemaspy/releases/download/v6.1.0/schemaspy-6.1.0.jar \
    --output ~/Downloads/schemaspy.jar
```

# Quick start

Let's assume you're using PostgreSQL (11 or later).
First, download their JBDC driver.

```
curl -L https://jdbc.postgresql.org/download/postgresql-42.5.4.jar \
    --output ~/Downloads/jbdc-driver.jar
```

Then run SchemaSpy against your database and you're ready to browse it in
`DIRECTORY/index.html`.

```
java -jar ~/Downloads/schemaspy.jar \
    --database-type pgsql11 \
    --driverPath ~/Downloads/jbdc-driver.jar \
    -db DATABASE \
    -host SERVER \
    -port 5432 \
    --schema dbo \
    -u USER \
    -p PASSWORD \
    -o DIRECTORY
```

> Please support the project by simply putting a Github star. 
Share this library with friends on Twitter and everywhere else you can.

> If you notice a bug or have something not working, please report an issue, we'll try to fix it as soon as possible.
More documentation and features expected to be soon. Feel free to contribute.

This is a new code repository for SchemaSpy tool initially created and maintained [by John Currier](http://schemaspy.sourceforge.net/).
I personally believe that work on SchemaSpy should be continued, and a lot of still existing issues should be resolved.
Last released version of the SchemaSpy was in 2010, and I have a plan to change this.

I would like to say thank you to John Currier for the invention of this database entity-relationship (ER) diagram generator.

My plan is to release new SchemaSpy version and concentrate on refactoring and improving it.

In new **SchemaSpy 6.0** version you will find:

1. Absolutely new amazing look and feel generated content

2. You can very easily change SchemaSpy theme because all the logic was moved outside the Java code

3. Speed of database generation improved a little

4. Generation of the html pages was changed from plain concatenated text in Java code to Moustache engine

5. In database comments you can use Markdown language what should improve user experience from reading your database documentation

6. You can also easily create in database comments links to the tables and columns

More detail will came in next days plus I have plan to start working on all not resolved issues.

## Documentation

If you need more info about how to use or install SchemaSpy please read the last documentation at Read the Docs.

[Documentation](https://schemaspy.readthedocs.io/en/latest/)

## SchemaSpy v6.1.0
[Release Notes](https://github.com/schemaspy/schemaspy/releases/tag/v6.1.0)

[Download](https://github.com/schemaspy/schemaspy/releases/download/v6.1.0/schemaspy-6.1.0.jar)

## Latest Build

To verify fixes and new features you can download our latest build.

[schemaspy@latest](https://schemaspy.org/schemaspy/download.html)

## Docker  

We publish snapshot and release to [hub.docker.io](https://hub.docker.com/r/schemaspy/schemaspy/)

## FAQ

### General

#### Schema or Catalog name can't be null
This means that Schema or Catalog information could not be extracted from connection.  
I this case you need to add options `-s [schemaName]` or `-cat [catalogName]`   
In most cases for catalog you can use `-cat %`  
In mysql you can use same as `-db`  

### OSX

#### Graphviz
There have been lots of issue with graphviz and OSX  
So install using brew `brew install graphviz --with-librsvg --with-pango`  
If you already have graphviz installed you need to uninstall and then install  
```
brew uninstall graphviz
brew install graphviz --with-pango --with-librsvg
```
Depending on OSX version  
*Older than High Sierra*, add `-renderer :quartz` to the commandline  
*High Sierra or newer*, add `-renderer :cairo` to the commandline  

## Bugs and Issues

Have a bug or an issue with SchemaSpy? [Open a new issue](https://github.com/schemaspy/schemaspy/issues) here on GitHub.

## Contribution
Are very welcome! And remember, contribution is not only PRs and code, but any help with docs or helping other developers to solve issues are very appreciated! Thanks in advance!

### Build Instructions
#### Application
SchemaSpy is built using maven and we utilize the maven wrapper.  
__Windows__ `mvnw.cmd package`    
__Linux__ `./mvnw package`  
The resulting application can be found in `target`  

##### Analyzing
You need your own SonarQube:  
https://hub.docker.com/_/sonarqube/  
__Windows__ `mvnw.cmd -P sonar clean verify -Dsonar.host.url=http://$(boot2docker ip):9000 -Dsonar.jdbc.url="jdbc:h2:tcp://$(boot2docker ip)/sonar"`  
__Linux__ `./mvnw -P sonar clean verify`  

Watch results at:  
__Linux__ `http://localhost:9000`  
__Windows__ `http://$(boot2docker ip):9000`  

#### Documentation
You'll need sphinx installed http://www.sphinx-doc.org    
Navigate into `docs`  
__Windows__ `make.bat html`  
__Linux__ `make html`  
The resulting documentation can be found in `docs/build/html`

## Maintained

SchemaSpy is maintained by:
* [Rafał Kasa](https://github.com/rafalkasa) 
* [Nils Petzäll](https://github.com/npetzall)
* [Jesper Olsson](https://github.com/jesperolsson-se)

with SchemaSpy community support please contact with us if you have some question or proposition.


## Team Tools

[![alt tag](http://pylonsproject.org/img/logo-jetbrains.png)](https://www.jetbrains.com/) 

SchemaSpy Team would like inform that JetBrains is helping by provided IDE to develop the application. Thanks to its support program for an Open Source projects !

[![alt tag](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)

SchemaSpy project is using SonarCloud for code quality. 
Thanks to SonarQube Team for free analysis solution for open source projects.

## License
SchemaSpy is distributed under LGPL version 3 or later, see COPYING.LESSER(LGPL) and COPYING(GPL).   
LGPLv3 is additional permissions on top of GPLv3.

![image](https://user-images.githubusercontent.com/19885116/48661948-6cf97e80-ea7a-11e8-97e7-b45332a13e49.png)

