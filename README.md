[![Logotype](https://github.com/schemaspy/schemaspy/blob/master/docs/source/_static/schemaspy_logo.png)](https://schemaspy.org/)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.schemaspy/schemaspy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.schemaspy/schemaspy/)
[![Docker Pulls](https://img.shields.io/docker/pulls/schemaspy/schemaspy.svg)](https://hub.docker.com/r/schemaspy/schemaspy/)
[![Development](https://github.com/schemaspy/schemaspy/actions/workflows/development.yml/badge.svg)](https://github.com/schemaspy/schemaspy/actions/workflows/development.yml)
[![Documentation Status](https://readthedocs.org/projects/schemaspy/badge/?version=latest)](http://schemaspy.readthedocs.io/en/latest/?badge=latest)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.schemaspy%3Aschemaspy&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.schemaspy%3Aschemaspy&metric=coverage)](https://sonarcloud.io/dashboard?id=org.schemaspy%3Aschemaspy)
[![Gitter](https://badges.gitter.im/schemaspy/schemaspy.svg)](https://gitter.im/schemaspy/schemaspy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

**SchemaSpy** is a database metadata analyzer. It helps your database administrators and developers visualize, navigate and understand your data model. With an easy-to-use HTML-based report, traversing the entity-relationship diagram has never been simpler. Our product showcase is available at http://schemaspy.org/sample/index.html.

[![SchemaSpy sample](http://schemaspy.org/img/example_page_epivirusurf.png)](http://schemaspy.org/samples/epivirusurf)

# Installation

SchemaSpy is a standalone application without GUI.
Just download the latest [JAR file](https://github.com/schemaspy/schemaspy/releases/latest) or [Docker image](https://hub.docker.com/r/schemaspy/schemaspy/) and you're ready to go!
To use SchemaSpy from Maven, please see the [Maven chapter](#maven) below.

```
# replace '6.2.4' with latest version
curl -L https://github.com/schemaspy/schemaspy/releases/download/v6.2.4/schemaspy-6.2.4.jar \
    --output ~/Downloads/schemaspy.jar
```

> For unreleased bug fixes and features-in-progress, download our [snapshot JAR](https://schemaspy.org/schemaspy/download.html) or use Docker tag `snapshot`

## Maven

SchemaSpy releases two types of JAR files: a bare-bone JAR and a fat JAR including all dependencies.
Both JARs are published to Maven Central.
The fat JAR is also attached to releases on GitHub.
The "maven central" badge at the top of this page will take you straight to the latest version on Maven Central.

The Maven GAV of the two artifacts is as follows:
- bare-bone JAR: `org.schemaspy:schemaspy:<version>`
- fat JAR: `org.schemaspy:schemaspy:<version>:app` **← note the `app` classifier**

# Quick start

Let's assume you're using PostgreSQL (11 or later).
First, download their JDBC driver.

```
curl -L https://jdbc.postgresql.org/download/postgresql-42.5.4.jar \
    --output ~/Downloads/jdbc-driver.jar
```

Then run SchemaSpy against your database and you're ready to browse it in
`DIRECTORY/index.html`.

```
java -jar ~/Downloads/schemaspy.jar \
    -t pgsql11 \
    -dp ~/Downloads/jdbc-driver.jar \
    -db DATABASE \
    -host SERVER \
    -port 5432 \
    -u USER \
    -p PASSWORD \
    -o DIRECTORY
```

If you aren't using PostgreSQL, don't panic! Out of the box, SchemaSpy supports
over a dozen different databases. List them by using `-dbhelp`. Still not enough?
As long as your database has a JDBC driver you can
[plug it in](https://schemaspy.readthedocs.io/en/latest/configuration/databaseType.html)
to SchemaSpy.

# Documentation and FAQs

We host our [documentation](https://schemaspy.readthedocs.io/en/latest/) on Read the Docs.
Be sure to check out the section on
[troubleshooting common problems](https://schemaspy.readthedocs.io/en/latest/faq.html).

# Main use cases

SchemaSpy covers a lot of use cases for database analysis and documentation.
Be sure to check out the guides provided by the community later in this README.

## On-demand database documentation

The preferred way to document databases is through entity-relationship (ER) diagrams.
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
* :czech_republic: [Automatické vytvoření dokumentace k databázi s využitím nástroje SchemaSpy ](https://www.root.cz/clanky/automaticke-vytvoreni-dokumentace-k-databazi-s-vyuzitim-nastroje-schemaspy/) by Pavel Tišnovský
* :de: [Datenbank-Analyse mit SchemaSpy](https://www.jentsch.io/datenbank-analyse-mit-schemaspy/) by Michael Jentsch
* :de: [Quick Tipp: Eine Datenbank Struktur verstehen mit Hilfe von schemaspy](https://www.exensio.de/news-medien/newsreader-blog/quick-tipp-eine-datenbank-struktur-verstehen-mit-hilfe-von-schemaspy) by von Irving Tschepke
* :es: :arrow_forward: [Ejemplo de Uso de schemaspy](https://www.youtube.com/watch?v=13MMSeDaWao) by MGS Educación, Tecnología y Juventud
* :es: :arrow_forward: [Generar modelo desde una base de datos con schemaSpy](https://youtu.be/RoTITyGJ07Y) by Inforgledys
* :es: [Cómo documentar tus bases de datos con SchemaSpy](https://profile.es/blog/como-documentar-tus-bases-de-datos-con-schemaspy/) by Jesus Jimenez Herrera
* :es: [¿Y si documentamos la base de datos? ... SchemaSpy al rescate](https://www.enmilocalfunciona.io/y-si-documentamos-la-base-de-datos-schemaspy-al-rescate/) by Víctor Madrid
* :fr: :arrow_forward: [Une DOC AUTOMATIQUE avec SchemaSpy (et SYMFONY et GITLAB)](https://youtu.be/Ehw1t2S4APQ?t=602) by YoanDev
* :fr: [Documentation automatique d’une App Symfony avec SchemaSpy et GitLab !](https://yoandev.co/documentation-automatique-dune-app-symfony-avec-schemaspy-et-gitlab/) by YoanDev
* :fr: [Documenter une base de données avec SchemaSpy](https://dataforeveryone.medium.com/documenter-une-base-de-donn%C3%A9es-avec-schemaspy-e0f56a6fcfb3) by Data 4 Everyone!
* :jp: [SchemaSpyでデータベースのドキュメントを生成してみた](https://dev.classmethod.jp/articles/schemaspy-doc/) By 坂井裕介
* :jp: [SchemaSpyでER図を生成する](https://zenn.dev/onozaty/articles/schema-spy-er) By @onozaty
* :portugal: [Documentando bancos com Schemaspy](https://www.linkedin.com/pulse/documentando-bancos-com-schemaspy-krisnamourt-silva/) By Krisnamourt Silva
* :thailand: [แนะนำ SchemaSpy เครื่องมือทำเอกสาร Database](https://knowlats.dev/how-to-use-schemaspy/) by @icegotcha
* :open_book: [Java Power Tools](https://www.goodreads.com/en/book/show/2631468) by John Ferguson Smart
* :open_book: [Monolith to Microservices: Sustaining Productivity While Detangling the System](https://www.goodreads.com/en/book/show/44144499) by Sam Newman
* [Documenting your database with SchemaSpy](https://robintegg.com/2019/01/29/documenting-your-database-with-schemaspy.html) by Robin Tegg
* [Documenting your relational database using SchemaSpy](https://tech.asimio.net/2020/11/27/Documenting-your-relational-database-using-SchemaSpy.html) by Orlando L Otero
* [How to Create ERD(Entity Relationship Diagram)](https://www.cybrosys.com/blog/how-to-create-erd-entity-relationship-diagram) by Cybrosys technologies
* [How to Document a Database With SchemaSpy](https://levelup.gitconnected.com/database-documentation-with-schemaspy-e9071eecd45a) by Data 4 Everyone!
* [How to use SchemaSpy to document your database](https://medium.com/@gustavo.ponce.ch/how-to-use-schemaspy-to-document-your-database-4046fdecfe83) by Gustavo Ponce
* [How to visualize a PostgreSQL schema as SVG with SchemaSpy](https://dev.to/mostalive/how-to-visualize-a-postgresql-schema-as-svg-with-schemaspy-516g) by Willem van den Ende
* [Installing SchemaSpy to document you database](http://www.goring.org/resources/schemaspy_tutorial.html) by @SimonGoring
* [Netbox database schema diagram using schemaspy](https://www.oasys.net/posts/netbox-database-schema-diagram-using-schemaspy/) by Jason Lavoie
* [Production grade PostgreSQL documentation in minutes](https://postgresconf.org/blog/posts/production-grade-postgresql-documentation-in-minutes) by Magnus Brun Falch
* [Schemaspy – create documentation for your database](https://petrhnilica.cz/en/blog/2018/04/12/schemaspy-create-documentation-for-your-database/) by Petr Hnilica
* [SchemaSpy-HOWTO](https://gist.github.com/dpapathanasiou/c9c6236a410e9d018ae0) by @dpapathanasiou
* [Simple database documentation with SchemaSpy](https://rieckpil.de/howto-simple-database-documentation-with-schemaspy/) by @rieckpil
* [Use cases of data and Schemaspy: Database Management](https://juileetalele.medium.com/use-cases-of-data-and-schemaspy-database-management-6e4c43c383e2) by Juilee Talele

## Scientific usage

We are proud to note that SchemaSpy assists researchers in their work.

* [A data-driven dynamic ontology](https://doi.org/10.1177/0165551515576478) by Dhomas Hatta Fudholi et al.
* [A large scale empirical comparison of state-of-the-art search-based test case generators](https://doi.org/10.1016/j.infsof.2018.08.009) by Annibale Panichella et al.
* [A scientist's guide for submitting data to ZFIN](https://doi.org/10.1016/bs.mcb.2016.04.010) by Douglas G Howe
* [Automated unit test generation for classes with environment dependencies](http://dx.doi.org/10.1145/2642937.2642986) by Andrea Arcuri et al.
* [BiG-SLiCE: A highly scalable tool maps the diversity of 1.2 million biosynthetic gene clusters](https://doi.org/10.1093/gigascience/giaa154) by Satria A Kautsar et al.
* [emrKBQA: A Clinical Knowledge-Base Question Answering Dataset](https://hdl.handle.net/1721.1/143907) by Preethi Raghavan et al.
* [EpiSurf: metadata-driven search server for analyzing amino acid changes within epitopes of SARS-CoV-2 and other viral species](https://doi.org/10.1093/database/baab059) by Anna Bernasconi et al.
* [Experiences from performing software quality evaluations via combining benchmark-based metrics analysis, software visualization, and expert assessment](https://doi.org/10.1109/ICSM.2015.7332493) by Aiko Yamashita
* [FOCUSPEARL version 5.5.5 - technical description of database and interface](https://library.wur.nl/WebQuery/wurpubs/608609) by Maarten C Braakhekke et al.
* [From monolithic systems to microservices: A decomposition framework based on process mining](https://doi.org/10.5220/0007755901530164) by Davide Taibi and Kari Systä
* [GEM: The GAAIN Entity Mapper](https://doi.org/10.1007%2F978-3-319-21843-4_2) by Naveen Ashish et al.
* [Healthsheet: Development of a Transparency Artifact for Health Datasets](https://doi.org/10.1145/3531146.3533239) by Negar Rostamzadeh et al.
* [How Aphia—The Platform behind Several Online and Taxonomically Oriented Databases—Can Serve Both the Taxonomic Community and the Field of Biodiversity Informatics](https://doi.org/10.3390/jmse3041448) by Leen Vandepitte et al.
* [Incremental Control Dependency Frontier Exploration for Many-Criteria Test Case Generation](https://doi.org/10.1007/978-3-319-99241-9_17) by Annibale Panichella et al.
* [Integrating Multimodal Radiation Therapy Data into i2b2](https://doi.org/10.1055/s-0038-1651497) by Eric Zapletal et al.
* [Methodology of integration of a clinical data warehouse with a clinical information system: the HEGP case](https://doi.org/10.3233/978-1-60750-588-4-193) by Eric Zapletal et al.
* [NakeDB: Database Schema Visualization](https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=8374b287c790e70eeee834fd437de9ad9b2fe461) by Luis Miguel Cort ́es-Peña
* [OCTOPUS database (v.2)](https://doi.org/10.5194/essd-14-3695-2022) by Alexandru T. Codilean et al.
* [On approximate matching of programs for protecting libre software](https://doi.org/10.1145/1188966.1188994) by Arnoldo José Müller Molina and Takeshi Shinohara
* [On the Quality of Relational Database Schemas in Open-source Software](https://hal.science/hal-00742605v1) by Fabien Coelho et al.
* [Predicting Hospital Readmission by Analyzing Patient EHR Records](https://doi.org/10.1007/978-1-4842-7086-8_3) by Anshik
* [Prediction of actor collaborations using IMDB data](https://users.soe.ucsc.edu/~vassilis/projects/AdvancedMLCS290.pdf) by Vassilis Polychronopoulos and Abhinav Venkateswar Venkataraman
* [Probabilistic relational model benchmark generation: Principle and application](https://doi.org/10.3233/IDA-160823) by Mouna Ben Ishak et al.
* [Processes, Motivations, and Issues for Migrating to Microservices Architectures: An Empirical Investigation](https://doi.org/10.1109/MCC.2017.4250931) by Davide Taibi et al.
* [Realising the Potential for ML from Electronic Health Records](https://cpb-eu-w2.wpmucdn.com/blogs.bristol.ac.uk/dist/8/471/files/2020/01/DSRS-Turing_19.pdf) by Haoyuan Zhang et al.
* [Seeding strategies in search-based unit test generation](https://doi.org/10.1002/stvr.1601) by José Miguel Rojas et al.
* [Sound empirical evidence in software testing](https://doi.org/10.1109/ICSE.2012.6227195) by Gordon Fraser and Andrea Arcuri
* [The Zebrafish Information Network: major gene page and home page updates](https://doi.org/10.1093/nar/gkaa1010) by Douglas G Howe et al
* [Transformation and Evaluation of the MIMIC Database in the OMOP Common Data Model: Development and Usability Study](https://doi.org/10.2196/30970) by Nicolas Paris et al.
* [Una base de datos espacial integrada en un Sistema de Información Geográfica para la gestión del terroir: un nuevo sistema consistente e interactivo](https://doi.org/10.1051/e3sconf/20185002008) by Alberto Lázaro-López et al.
* [Using a combination of measurement tools to extract metrics from open source projects](http://hdl.handle.net/1885/51159) by Normi S Awang Abu Bakar and Clive Boughton
* [Using Tableau Dashboards as Visualization Tool for MIMIC-III Data](https://dx.doi.org/10.3205/21gmds044) by Karl Gottfried et al.
* [Zebrafish information network, the knowledgebase for Danio rerio research](https://doi.org/10.1093/genetics/iyac016) by Yvonne M Bradford et al.

To cite SchemaSpy, please use:

```
SchemaSpy Team (2024) SchemaSpy: Database documentation built easy. SchemaSpy. URL https://schemaspy.org/
```

The BibTeX entry for LaTeX users is:

```
@Manual{schemaspy,
  title = {SchemaSpy: Database documentation built easy},
  author = {{SchemaSpy Team}},
  organization = {SchemaSpy},
  year = {2024},
  url = {https://schemaspy.org/}
}
```


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
Built using Python  
Create venv  
Install dependencies `pip install -r docs/requirements.txt`  
Navigate into `docs`  
__Windows__ `make.bat clean && make.bat html`  
__Linux__ `make clean html`  
The resulting documentation can be found in `docs/build/html`
