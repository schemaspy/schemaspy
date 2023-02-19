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

If you aren't using PostgreSQL, don't panic! Out of the box, SchemaSpy supports
over a dozen different databases. List them by using `-dbhelp`. Still not enough?
As long as your database has a JBDC driver you can
[plug it in](https://schemaspy.readthedocs.io/en/latest/configuration/databaseType.html)
to SchemaSpy.

# Main use cases

SchemaSpy covers a lot of use cases for database analysis and documentation.
Be sure to check out the guides provided by the community later in this README.

## On-demand database documentation

The prefered way to document databases is through entity-relationship (ER) diagrams.
However, drawing these diagrams manually is such a time-consuming and error-prone
process that we hardly ever draw them in practice. When the diagrams *are* drawn,
they rarely stay up-to-date. With SchemaSpy, this is no longer a problem.
The diagrams can be generated quickly and even as a part of your CI/CD workflow
to ensure it's always up to date.

## Your database in numbers

SchemaSpy can collect various kinds of interesting statistics to describe the shape
and form of your database's structure. Drill down deeper into these statistics
directly in the report or export them to excel or CSV for further QA analysis.

## Keep the data confidential

Nowadays, a company's data can be their most valuable asset. Since SchemaSpy only
reads structural information, it works just as well on an empty database replica.
This means that the report can be shared for third party analysis without fear.

## Detect sub-optimal constructs

SchemaSpy incorporates knowledge about best practices in database design. It can
locate and report anomalies such as missing indexes, implied relationships, and
orphan tables.

# Community

Welcome to the SchemaSpy community! Just reading this file or using the tool means
that you're a part of our community and contributing to the future of the project.
We're grateful to have you with us!

Some of our community members have put extra effort into sharing SchemaSpy with
more people, asked their companies to provide financial aid, or decided to improve
the software. We wish we had the space to thank each of you individually because
every Github star, tweet or other activity reminds us that our work is appreciated.

## Special thanks

For creating the first five versions of SchemaSpy:
* [John Currier](http://schemaspy.sourceforge.net/)

For perpetuating SchemaSpy ever since:
* [Rafał Kasa](https://github.com/rafalkasa),
* [Nils Petzäll](https://github.com/npetzall), and
* [Jesper Olsson](https://github.com/jesperolsson-se)

For creating tutorials and guides for the community:
* :fr: :arrow_forward: [Une DOC AUTOMATIQUE avec SchemaSpy (et SYMFONY et GITLAB)](https://youtu.be/Ehw1t2S4APQ?t=602) by YoanDev
* :de: :arrow_forward: [Datenbank-Analyse mit SchemaSpy](https://www.jentsch.io/datenbank-analyse-mit-schemaspy/) by Michael Jentsch
* :es: :arrow_forward: [Ejemplo de Uso de schemaspy](https://www.youtube.com/watch?v=13MMSeDaWao) by MGS Educación, Tecnología y Juventud
* :es: :arrow_forward: [Generar modelo desde una base de datos con schemaSpy](https://youtu.be/RoTITyGJ07Y) by Inforgledys
* :open_book: [Java Power Tools](https://www.goodreads.com/en/book/show/2631468) by John Ferguson Smart
* [Documenting your database with SchemaSpy](https://robintegg.com/2019/01/29/documenting-your-database-with-schemaspy.html) by Robin Tegg
* [How to Document a Database With SchemaSpy](https://levelup.gitconnected.com/database-documentation-with-schemaspy-e9071eecd45a) by Data 4 Everyone!
* [How to use SchemaSpy to document your database](https://medium.com/@gustavo.ponce.ch/how-to-use-schemaspy-to-document-your-database-4046fdecfe83) by Gustavo Ponce
* [Installing SchemaSpy to document you database](http://www.goring.org/resources/schemaspy_tutorial.html) by @SimonGoring
* [SchemaSpy-HOWTO](https://gist.github.com/dpapathanasiou/c9c6236a410e9d018ae0) by @dpapathanasiou
* [Simple database documentation with SchemaSpy](https://rieckpil.de/howto-simple-database-documentation-with-schemaspy/) by @rieckpil
* [Use cases of data and Schemaspy: Database Management](https://juileetalele.medium.com/use-cases-of-data-and-schemaspy-database-management-6e4c43c383e2) by Juilee Talele

## Scientific usage

We are proud to note that SchemaSpy assists researchers in their work.

* [EpiSurf: metadata-driven search server for analyzing amino acid changes within epitopes of SARS-CoV-2 and other viral species](https://doi.org/10.1093/database/baab059) by Anna Bernasconi et al.
* [Experiences from performing software quality evaluations via combining benchmark-based metrics analysis, software visualization, and expert assessment](https://doi.org/10.1109/ICSM.2015.7332493) by Aiko Yamashita
* [FOCUSPEARL version 5.5.5 - technical description of database and interface](https://library.wur.nl/WebQuery/wurpubs/608609) by Maarten C Braakhekke et al.
* [From monolithic systems to microservices: A decomposition framework based on process mining](https://doi.org/10.5220/0007755901530164) by Davide Taibi and Kari Systä
* [Methodology of integration of a clinical data warehouse with a clinical information system: the HEGP case](https://doi.org/10.3233/978-1-60750-588-4-193) by Eric Zapletal et al.
* [Predicting Hospital Readmission by Analyzing Patient EHR Records](https://doi.org/10.1007/978-1-4842-7086-8_3) by Anshik
* [Processes, Motivations, and Issues for Migrating to Microservices Architectures: An Empirical Investigation](https://doi.org/10.1109/MCC.2017.4250931) by Davide Taibi et al.

# Documentation

If you need more info about how to use or install SchemaSpy please read the last documentation at Read the Docs.

[Documentation](https://schemaspy.readthedocs.io/en/latest/)

# Latest Build

To verify fixes and new features you can download our latest build.

[schemaspy@latest](https://schemaspy.org/schemaspy/download.html)

# Build Instructions
## Application
SchemaSpy is built using maven and we utilize the maven wrapper.  
__Windows__ `mvnw.cmd package`    
__Linux__ `./mvnw package`  
The resulting application can be found in `target`  

### Analyzing
You need your own SonarQube:  
https://hub.docker.com/_/sonarqube/  
__Windows__ `mvnw.cmd -P sonar clean verify -Dsonar.host.url=http://$(boot2docker ip):9000 -Dsonar.jdbc.url="jdbc:h2:tcp://$(boot2docker ip)/sonar"`  
__Linux__ `./mvnw -P sonar clean verify`  

Watch results at:  
__Linux__ `http://localhost:9000`  
__Windows__ `http://$(boot2docker ip):9000`  

## Documentation
You'll need sphinx installed http://www.sphinx-doc.org    
Navigate into `docs`  
__Windows__ `make.bat html`  
__Linux__ `make html`  
The resulting documentation can be found in `docs/build/html`
