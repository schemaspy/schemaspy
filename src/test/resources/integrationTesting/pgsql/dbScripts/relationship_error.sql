
-- SELECT uuid_generate_v4();


DROP SCHEMA IF EXISTS cendraom CASCADE;


CREATE SCHEMA cendraom AUTHORIZATION postgres;


CREATE EXTENSION "uuid-ossp" SCHEMA cendraom;


-- ==========================================================================================================================
-- ==========================================================================================================================
-- ==========================================================================================================================
-- =======================																				=====================
-- =======================				FUNCIONES UTILES												=====================
-- =======================																				=====================
-- ==========================================================================================================================
-- ==========================================================================================================================
-- ==========================================================================================================================



DROP FUNCTION IF EXISTS cendraom.white_is_null (att_val VARCHAR) CASCADE;


CREATE OR REPLACE FUNCTION cendraom.white_is_null(att_val VARCHAR) RETURNS VARCHAR AS $$
BEGIN
	IF CHAR_LENGTH(TRIM(att_val)) = 0 THEN
		RETURN null::VARCHAR;
	END IF;
	RETURN att_val::VARCHAR;
END;
$$  LANGUAGE plpgsql;


-- ==========================================================================================================================
-- ==========================================================================================================================
-- ==========================================================================================================================
-- =======================																				=====================
-- =======================				TABLAS y TRIGGERS												=====================
-- =======================																				=====================
-- ==========================================================================================================================
-- ==========================================================================================================================
-- ==========================================================================================================================


-- Table: cendraom.Clazz


DROP TABLE IF EXISTS cendraom.Clazz CASCADE;


CREATE TABLE cendraom.Clazz
(
    id VARCHAR PRIMARY KEY DEFAULT cendraom.uuid_generate_v4(),
    virtual BOOLEAN NOT NULL DEFAULT false,
    name VARCHAR  NOT NULL UNIQUE,
    visibility VARCHAR  NOT NULL,
    finalClass BOOLEAN NOT NULL DEFAULT false,
    abstractClass BOOLEAN NOT NULL DEFAULT false
);


-- SELECT * FROM cendraom.Clazz;
-- DELETE FROM cendraom.Clazz;

-- CREATE UNIQUE INDEX u_Clazz_name ON cendraom.Clazz (LOWER(TRIM(name)));


DROP INDEX IF EXISTS u_Clazz_name CASCADE;


CREATE UNIQUE INDEX u_Clazz_name ON cendraom.Clazz (TRIM(name));


-- ---------------------------------------------------------------------------------------------------------------------------


DROP FUNCTION IF EXISTS cendraom.ftgFormatClazz() CASCADE;


CREATE OR REPLACE FUNCTION cendraom.ftgFormatClazz() RETURNS TRIGGER AS $formatClazz$
DECLARE
BEGIN
   NEW.id := cendraom.white_is_null(TRIM(NEW.id));
   NEW.name := cendraom.white_is_null(TRIM(NEW.name));
	RETURN NEW;
END;
$formatClazz$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------------------------------------------------------


DROP TRIGGER IF EXISTS tgFormatClazz ON cendraom.Clazz CASCADE;


CREATE TRIGGER tgFormatClazz BEFORE INSERT OR UPDATE
    ON cendraom.Clazz FOR EACH ROW
    EXECUTE PROCEDURE cendraom.ftgFormatClazz();


-- ==========================================================================================================================

-- Table: cendraom.ClazzExtends


DROP TABLE IF EXISTS cendraom.ClazzExtends CASCADE;


CREATE TABLE cendraom.ClazzExtends
(
    id VARCHAR PRIMARY KEY DEFAULT cendraom.uuid_generate_v4(),
    clazz VARCHAR NOT NULL REFERENCES cendraom.Clazz (id),
    clazzExtends VARCHAR NOT NULL REFERENCES cendraom.Clazz (id)
);


-- SELECT * FROM cendraom.ClazzExtends;


DROP INDEX IF EXISTS u_ClazzExtends_clazz_clazzExtends CASCADE;


CREATE UNIQUE INDEX u_ClazzExtends_clazz_clazzExtends ON cendraom.ClazzExtends (TRIM(clazz), TRIM(clazzExtends));


-- ---------------------------------------------------------------------------------------------------------------------------


DROP FUNCTION IF EXISTS cendraom.ftgFormatClazzExtends() CASCADE;


CREATE OR REPLACE FUNCTION cendraom.ftgFormatClazzExtends() RETURNS TRIGGER AS $formatClazzExtends$
DECLARE
BEGIN
   	NEW.id := cendraom.white_is_null(TRIM(NEW.id));
	RETURN NEW;
END;
$formatClazzExtends$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------------------------------------------------------


DROP TRIGGER IF EXISTS tgFormatClazzExtends ON cendraom.ClazzExtends CASCADE;


CREATE TRIGGER tgFormatClazzExtends BEFORE INSERT OR UPDATE
    ON cendraom.ClazzExtends FOR EACH ROW
    EXECUTE PROCEDURE cendraom.ftgFormatClazzExtends();


-- ==========================================================================================================================

-- Table: cendraom.ClazzAtt


DROP TABLE IF EXISTS cendraom.ClazzAtt CASCADE;


CREATE TABLE cendraom.ClazzAtt
(
    id VARCHAR PRIMARY KEY DEFAULT cendraom.uuid_generate_v4(),
    clazz VARCHAR NOT NULL REFERENCES cendraom.Clazz (id),
    name VARCHAR  NOT NULL,
    dataType VARCHAR  NOT NULL,
    typeCardinality VARCHAR  NOT NULL,
    orderAtt INTEGER NOT NULL DEFAULT 0
);


-- SELECT * FROM cendraom.ClazzAtt;
-- DELETE FROM cendraom.ClazzAtt;

-- CREATE UNIQUE INDEX u_Clazz_name ON cendraom.Clazz (LOWER(TRIM(name)));


DROP INDEX IF EXISTS u_ClazzAtt_calzz_name CASCADE;


CREATE UNIQUE INDEX u_ClazzAtt_calzz_name ON cendraom.ClazzAtt (TRIM(ClazzAtt.clazz), TRIM(ClazzAtt.name));


-- INSERT INTO cendraom.ClazzAtt (id, clazz, name, dataType, typeCardinality, orderAtt)
-- VALUES ('b465fcf8-71bc-4e9b-8597-98c42bfdbabc', '46171755-b975-4431-be81-dd8de9919022', 'edad', 'org.cendra.om.model.datatype.String', '1-1', '3')

-- ---------------------------------------------------------------------------------------------------------------------------


DROP FUNCTION IF EXISTS cendraom.ftgFormatClazzAtt() CASCADE;


CREATE OR REPLACE FUNCTION cendraom.ftgFormatClazzAtt() RETURNS TRIGGER AS $formatClazzAtt$
DECLARE
BEGIN

	NEW.id := cendraom.white_is_null(TRIM(NEW.id));
    NEW.name := cendraom.white_is_null(TRIM(NEW.name));
    NEW.dataType := cendraom.white_is_null(TRIM(NEW.dataType));

	RETURN NEW;
END;
$formatClazzAtt$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------------------------------------------------------


DROP TRIGGER IF EXISTS tgFormatClazzAtt ON cendraom.ClazzAtt CASCADE;


CREATE TRIGGER tgFormatClazzAtt BEFORE INSERT OR UPDATE
    ON cendraom.ClazzAtt FOR EACH ROW
    EXECUTE PROCEDURE cendraom.ftgFormatClazzAtt();


-- ==========================================================================================================================



-- ==========================================================================================================================


DROP SCHEMA IF EXISTS org_cendra_person CASCADE;


DROP SCHEMA IF EXISTS org_cendra_person_human CASCADE;


CREATE SCHEMA org_cendra_person AUTHORIZATION postgres;


CREATE SCHEMA org_cendra_person_human AUTHORIZATION postgres;


-- Table: org_cendra_person.person

-- DROP TABLE org_cendra_person.person;


CREATE TABLE org_cendra_person.person
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    fullname character varying COLLATE pg_catalog."default",
    birthdate date,
    CONSTRAINT person_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;



ALTER TABLE org_cendra_person.person
    OWNER to postgres;


-- Table: org_cendra_person.personaddress

-- DROP TABLE org_cendra_person.personaddress;


CREATE TABLE org_cendra_person.personaddress
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    countrycode character varying COLLATE pg_catalog."default",
    adminarealevel1code character varying COLLATE pg_catalog."default",
    adminarealevel2 character varying COLLATE pg_catalog."default",
    locality character varying COLLATE pg_catalog."default",
    neighbourhood character varying COLLATE pg_catalog."default",
    street character varying COLLATE pg_catalog."default",
    streetnumber character varying COLLATE pg_catalog."default",
    buildingfloor character varying COLLATE pg_catalog."default",
    buildingroom character varying COLLATE pg_catalog."default",
    building character varying COLLATE pg_catalog."default",
    postalcode character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    addressesperson_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT personaddress_pkey PRIMARY KEY (id),
    CONSTRAINT personaddress_addressesperson_id_fkey FOREIGN KEY (addressesperson_id)
        REFERENCES org_cendra_person.person (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person.personaddress
    OWNER to postgres;


-- Table: org_cendra_person.personemail

-- DROP TABLE org_cendra_person.personemail;


CREATE TABLE org_cendra_person.personemail
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    email character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    emailsperson_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT personemail_pkey PRIMARY KEY (id),
    CONSTRAINT personemail_emailsperson_id_fkey FOREIGN KEY (emailsperson_id)
        REFERENCES org_cendra_person.person (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person.personemail
    OWNER to postgres;


-- Table: org_cendra_person.personphone

-- DROP TABLE org_cendra_person.personphone;


CREATE TABLE org_cendra_person.personphone
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    countrycode character varying COLLATE pg_catalog."default",
    phonenumber character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    phonesperson_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT personphone_pkey PRIMARY KEY (id),
    CONSTRAINT personphone_phonesperson_id_fkey FOREIGN KEY (phonesperson_id)
        REFERENCES org_cendra_person.person (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person.personphone
    OWNER to postgres;


-- Table: org_cendra_person_human.humanidtype

-- DROP TABLE org_cendra_person_human.humanidtype;


CREATE TABLE org_cendra_person_human.humanidtype
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    code character varying COLLATE pg_catalog."default",
    name character varying COLLATE pg_catalog."default",
    countrycode character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    CONSTRAINT humanidtype_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person_human.humanidtype
    OWNER to postgres;


-- Table: org_cendra_person_human.human

-- DROP TABLE org_cendra_person_human.human;


CREATE TABLE org_cendra_person_human.human
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    extendsperson_id character varying COLLATE pg_catalog."default",
    givenname character varying COLLATE pg_catalog."default",
    middlename character varying COLLATE pg_catalog."default",
    familyname character varying COLLATE pg_catalog."default",
    male boolean,
    urlphoto character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    CONSTRAINT human_pkey PRIMARY KEY (id),
    CONSTRAINT human_extendsperson_id_fkey FOREIGN KEY (extendsperson_id)
        REFERENCES org_cendra_person.person (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person_human.human
    OWNER to postgres;


-- Table: org_cendra_person_human.humanbirth

-- DROP TABLE org_cendra_person_human.humanbirth;


CREATE TABLE org_cendra_person_human.humanbirth
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    countrycode character varying COLLATE pg_catalog."default",
    adminarealevel1code character varying COLLATE pg_catalog."default",
    adminarealevel2 character varying COLLATE pg_catalog."default",
    locality character varying COLLATE pg_catalog."default",
    lat double precision,
    lng double precision,
    birthhuman_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT humanbirth_pkey PRIMARY KEY (id),
    CONSTRAINT humanbirth_birthhuman_id_key UNIQUE (birthhuman_id),
    CONSTRAINT humanbirth_birthhuman_id_fkey FOREIGN KEY (birthhuman_id)
        REFERENCES org_cendra_person_human.human (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person_human.humanbirth
    OWNER to postgres;


-- Table: org_cendra_person_human.humanid

-- DROP TABLE org_cendra_person_human.humanid;


CREATE TABLE org_cendra_person_human.humanid
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    idnumber character varying COLLATE pg_catalog."default",
    code character varying COLLATE pg_catalog."default",
    name character varying COLLATE pg_catalog."default",
    countrycode character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    type character varying COLLATE pg_catalog."default",
    idshuman_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT humanid_pkey PRIMARY KEY (id),
    CONSTRAINT humanid_idshuman_id_fkey FOREIGN KEY (idshuman_id)
        REFERENCES org_cendra_person_human.human (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT humanid_type_fkey FOREIGN KEY (type)
        REFERENCES org_cendra_person_human.humanidtype (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person_human.humanid
    OWNER to postgres;


-- Table: org_cendra_person_human.humannationality

-- DROP TABLE org_cendra_person_human.humannationality;


CREATE TABLE org_cendra_person_human.humannationality
(
    id character varying COLLATE pg_catalog."default" NOT NULL DEFAULT cendraom.uuid_generate_v4(),
    countrycode character varying COLLATE pg_catalog."default",
    comment character varying COLLATE pg_catalog."default",
    nationalitieshuman_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT humannationality_pkey PRIMARY KEY (id),
    CONSTRAINT humannationality_nationalitieshuman_id_fkey FOREIGN KEY (nationalitieshuman_id)
        REFERENCES org_cendra_person_human.human (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


ALTER TABLE org_cendra_person_human.humannationality
    OWNER to postgres;

