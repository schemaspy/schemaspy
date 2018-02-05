# [Schema Spy](http://schemaspy.org/) -  [schemaspy.org](http://schemaspy.org/)

[![Build Status](https://travis-ci.org/schemaspy/schemaspy.svg?branch=master)](https://travis-ci.org/schemaspy/schemaspy)
[![Documentation Status](https://readthedocs.org/projects/schemaspy/badge/?version=latest)](http://schemaspy.readthedocs.io/en/latest/?badge=latest)
[![Quality Gate](https://sonarqube.com/api/badges/gate?key=org.schemaspy%3Aschemaspy)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)
[![Quality Gate](https://sonarqube.com/api/badges/measure?key=org.schemaspy%3Aschemaspy&metric=coverage)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)
[![Gitter](https://badges.gitter.im/schemaspy/schemaspy.svg)](https://gitter.im/schemaspy/schemaspy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Docker Pulls](https://img.shields.io/docker/pulls/schemaspy/schemaspy.svg)](https://hub.docker.com/r/schemaspy/schemaspy/)

> Please support a project by simply putting a Github star. 
Share this library with friends on Twitter and everywhere else you can.

> If you notice bug or have something not working please report an issue, we'll try to fix it as soon as possible.
More documentation and features expected to be soon. Feel free to contribute.

This is a new code repository for SchemaSpy tool initially created and maintained by John Currier.
I'm personally believe that work on SchemaSpy should be continued and a lot of still existing issue should be resolved.
Last released version of the SchemaSpy was two years ago, and I have plan to change this.

I would like to say thank you to John Currier for invent this database entity-relationship (ER) diagram generator.

In next days I have plan to release new SchemaSpy version and concentrate to refactor and improve the solution.
In new **SchemaSpy 6.0** version you will find:

1. Absolutely new amazing look and feel generated content

2. You can very easily change SchemaSpy theme because all of the logic was moved outside the Java code

3. Speed of database generation was little improve

4. Generation of the html pages was changed from plain concatenaited text in Java code to Moustache engine

5. In database comments you can use Markdown language what should improve user experience from reading your database documentation

6. You can also easily create in database comments links to the tables and columns

More detail will came in next days plus I have in plan to start working on all not resolved issues.

## Documentation

If you need more info about how to use or install SchemaSpy please read the last documentation at Read the Docs.

[Documentation](http://schemaspy.readthedocs.io/en/latest/)

[Sample Output](http://schemaspy.org/sample/index.html)

[![alt tag](http://schemaspy.org/img/example_page.png)](http://schemaspy.org/sample/index.html)

## SchemaSpy v6.0.0 RC2
[Release Notes](https://github.com/schemaspy/schemaspy/releases/tag/v6.0.0-rc2)

[Download](https://github.com/schemaspy/schemaspy/releases/download/v6.0.0-rc2/schemaspy-6.0.0-rc2.jar)

## Latest Build

To verify fixes and new features you can download our latest build.

[schemaspy@latest](https://github.com/schemaspy/schemaspy/raw/gh-pages/schemaspy-6.0.0-rc2.jar)

## Docker  

We publish snapshot and release to [hub.docker.io](https://hub.docker.com/r/schemaspy/schemaspy/)

## Bugs and Issues

Have a bug or an issue with SchemaSpy? [Open a new issue](https://github.com/schemaspy/schemaspy/issues) here on GitHub.

## Contribution
Are very welcome! And remember, contribution is not only PRs and code, but any help with docs or helping other developers to solve issues are very appreciated! Thanks in advance!

### Build Instructions
#### Application
SchemaSpy is built using maven and we utilize the maven wrapper.  
__Windows__ `mvnw.cmd build`    
__Linux__ `./mvnw build`  
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

SchemaSpy is maintained by [Rafal Kasa](malito:rafalkasa@gmail.com) with SchemaSpy community support please contact with me if you have some question or proposition.
* https://github.com/rafalkasa

## Team Tools

[![alt tag](http://pylonsproject.org/img/logo-jetbrains.png)](https://www.jetbrains.com/) 

SchemaSpy Team would like inform that JetBrains is helping by provided IDE to develop the application. Thanks to its support program for an Open Source projects !

[![alt tag](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)

SchemaSpy project is using SonarCloud for code quality. 
Thanks to SonarQube Team for free analysis solution for open source projects.



