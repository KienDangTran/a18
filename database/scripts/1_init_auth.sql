CREATE SCHEMA IF NOT EXISTS auth;

SET search_path = "auth";

SET TIMEZONE TO +7;

------------------------------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS auth.token_blacklist (
  jti         VARCHAR(255) NOT NULL,
  username    VARCHAR(255) NOT NULL,
  expires_in  BIGINT,
  blacklisted BOOLEAN,

  CONSTRAINT pk_token_blacklist PRIMARY KEY (jti)
);

------------------------------------------------------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS auth.user_seq;
CREATE TABLE IF NOT EXISTS auth."user" (
  id                      BIGINT       NOT NULL DEFAULT nextval('auth.user_seq' :: REGCLASS),
  username                VARCHAR(255) NOT NULL UNIQUE,
  password                VARCHAR(100) NOT NULL,
  fullname                VARCHAR(100) NOT NULL,
  email                   VARCHAR(255) NOT NULL UNIQUE,
  phone                   VARCHAR(50) UNIQUE,
  date_of_birth           DATE,
  enabled                 BOOLEAN               DEFAULT TRUE,
  account_non_locked      BOOLEAN               DEFAULT TRUE,
  account_non_expired     BOOLEAN               DEFAULT TRUE,
  credentials_non_expired BOOLEAN               DEFAULT TRUE,

  version                 BIGINT       NOT NULL DEFAULT 0,
  created_on              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_on              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by              BIGINT,
  updated_by              BIGINT,

  CONSTRAINT pk_user PRIMARY KEY (id)
);

INSERT INTO auth."user"(username, password, fullname, phone, email)
VALUES ('root',
        '{bcrypt}$2a$10$EOs8VROb14e7ZnydvXECA.4LoIhPOoFHKvVF/iBZ/ker17Eocz4Vi', -- password
        'Root Agent',
        '09056843152',
        'root@evil.com');

INSERT INTO auth."user"(username, password, fullname, phone, email, enabled)
VALUES ('test',
        '{bcrypt}$2a$10$EOs8VROb14e7ZnydvXECA.4LoIhPOoFHKvVF/iBZ/ker17Eocz4Vi', -- password
        'TEST ACCOUNT',
        NULL,
        'test@live.com',
        FALSE);

------------------------------------------------------------------------------------------------------------------------

------------------------- authority ------------------------------------------------------------------------------------
CREATE TYPE auth.AUTHORITY_STATUS_ENUM AS ENUM ('ACTIVE', 'SUSPENDED');
CREATE SEQUENCE IF NOT EXISTS auth.authority_seq;
CREATE TABLE IF NOT EXISTS auth.authority (
  id         SMALLINT                   NOT NULL DEFAULT nextval('auth.authority_seq' :: REGCLASS),
  code       VARCHAR(255)               NOT NULL UNIQUE,
  status     auth.AUTHORITY_STATUS_ENUM NOT NULL DEFAULT 'ACTIVE' :: auth.AUTHORITY_STATUS_ENUM,

  version    BIGINT                     NOT NULL DEFAULT 0,
  created_on TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_on TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,

  CONSTRAINT pk_authority PRIMARY KEY (id)
);

-- auth
INSERT INTO auth.authority(code)
VALUES ('STAFF');
INSERT INTO auth.authority(code)
VALUES ('USER');
INSERT INTO auth.authority(code)
VALUES ('ROLE');
INSERT INTO auth.authority(code)
VALUES ('AUTHORITY');

-- account
INSERT INTO auth.authority(code)
VALUES ('ACCOUNT');
INSERT INTO auth.authority(code)
VALUES ('AGENT_LEVEL');

-- lottery
INSERT INTO auth.authority(code)
VALUES ('SCHEDULER');
INSERT INTO auth.authority(code)
VALUES ('LOTTERY_ISSUE');
INSERT INTO auth.authority(code)
VALUES ('LOTTERY_PRIZE');
INSERT INTO auth.authority(code)
VALUES ('LOTTERY_SCHEMA');
INSERT INTO auth.authority(code)
VALUES ('RULE');

------------------------- roles ----------------------------------------------------------------------------------------
CREATE TYPE auth.ROLE_STATUS_ENUM AS ENUM ('ACTIVE', 'SUSPENDED');
CREATE SEQUENCE IF NOT EXISTS auth.role_seq;
CREATE TABLE IF NOT EXISTS auth.role (
  id             SMALLINT              NOT NULL DEFAULT nextval('auth.role_seq' :: REGCLASS),
  code           VARCHAR(255)          NOT NULL UNIQUE,
  parent_role_id SMALLINT,
  status         auth.ROLE_STATUS_ENUM NOT NULL DEFAULT 'ACTIVE' :: auth.ROLE_STATUS_ENUM,

  version        BIGINT                NOT NULL DEFAULT 0,
  created_on     TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_on     TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by     BIGINT,
  updated_by     BIGINT,

  CONSTRAINT pk_role PRIMARY KEY (id),
  CONSTRAINT fk_parent_role FOREIGN KEY (parent_role_id) REFERENCES auth.role (id)
);

INSERT INTO auth.role(code)
VALUES ('ROLE_USER');
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_STAFF', 1);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_PAYMENT_STAFF', 2);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_PAYMENT_HEAD', 3);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_MARKETING_STAFF', 2);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_MARKETING_HEAD', 4);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_CS_STAFF', 2);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_CS_HEAD', 6);
INSERT INTO auth.role(code, parent_role_id)
VALUES ('ROLE_ADMIN', 2);

------------------------- role_authority -------------------------------------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS auth.role_authority_seq;
CREATE TABLE IF NOT EXISTS auth.role_authority (
  id           SMALLINT  NOT NULL DEFAULT nextval('auth.role_seq' :: REGCLASS),
  authority_id SMALLINT  NOT NULL,
  role_id      SMALLINT  NOT NULL,
  read         BOOLEAN            DEFAULT TRUE,
  write        BOOLEAN            DEFAULT FALSE,
  exec         BOOLEAN            DEFAULT FALSE,

  version      BIGINT    NOT NULL DEFAULT 0,
  created_on   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_on   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by   BIGINT,
  updated_by   BIGINT,

  CONSTRAINT pk_role_authority PRIMARY KEY (id),
  CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES auth.role (id),
  CONSTRAINT fk_authority FOREIGN KEY (authority_id) REFERENCES auth.authority (id)
);

INSERT INTO auth.role_authority(authority_id, role_id, write)
SELECT id, 9, TRUE
FROM auth.authority;

------------------------------------------------------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS auth.staff_seq;
CREATE TABLE IF NOT EXISTS auth.staff (
  id                      BIGINT       NOT NULL DEFAULT nextval('auth.staff_seq' :: REGCLASS),
  username                VARCHAR(255) NOT NULL UNIQUE,
  password                VARCHAR(100) NOT NULL,
  fullname                VARCHAR(100) NOT NULL,
  email                   VARCHAR(255) NOT NULL UNIQUE,
  phone                   VARCHAR(50) UNIQUE,
  date_of_birth           DATE,
  enabled                 BOOLEAN               DEFAULT TRUE,
  account_non_locked      BOOLEAN               DEFAULT TRUE,
  account_non_expired     BOOLEAN               DEFAULT TRUE,
  credentials_non_expired BOOLEAN               DEFAULT TRUE,

  version                 BIGINT       NOT NULL DEFAULT 0,
  created_on              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_on              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by              BIGINT,
  updated_by              BIGINT,

  CONSTRAINT pk_staff PRIMARY KEY (id)
);

INSERT INTO auth.staff(username, password, fullname, email)
VALUES ('admin',
        '{bcrypt}$2a$10$EOs8VROb14e7ZnydvXECA.4LoIhPOoFHKvVF/iBZ/ker17Eocz4Vi', -- password
        'Administrator',
        'admin@evil.com');

------------------------------------------------------------------------------------------------------------------------

CREATE SEQUENCE IF NOT EXISTS auth.staff_role_seq;
CREATE TABLE IF NOT EXISTS auth.staff_role (
  id         SMALLINT  NOT NULL DEFAULT nextval('auth.staff_role_seq' :: REGCLASS),
  role_id    SMALLINT  NOT NULL DEFAULT 1,
  staff_id   BIGINT    NOT NULL,

  version    BIGINT    NOT NULL DEFAULT 0,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,

  CONSTRAINT pk_user_role PRIMARY KEY (id),
  CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES auth.role (id),
  CONSTRAINT fk_user FOREIGN KEY (staff_id) REFERENCES auth.staff (id)
);

INSERT INTO auth.staff_role(staff_id, role_id)
VALUES (1, 9);