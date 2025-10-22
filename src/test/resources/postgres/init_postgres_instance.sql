-- base de authorisation
CREATE USER authdb WITH PASSWORD 'authdb' CREATEDB;
CREATE DATABASE authdb
WITH
    OWNER = authdb
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- base de collection manager
CREATE USER itv WITH PASSWORD 'itv' CREATEDB;
CREATE DATABASE itv
WITH
    OWNER = itv
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- base de sync
CREATE USER syncdb WITH PASSWORD 'syncdb' CREATEDB;
CREATE DATABASE syncdb
WITH
    OWNER = syncdb
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- base de keycloak
CREATE USER adminkc WITH PASSWORD 'adminkc' CREATEDB;
CREATE DATABASE keycloak
WITH
    OWNER = adminkc
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;