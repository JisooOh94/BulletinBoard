DROP TABLE IF EXISTS article;
DROP SEQUENCE IF EXISTS seq;

CREATE SEQUENCE seq START WITH 1 INCREMENT BY 1;

CREATE TABLE article (
  `no`  BIGINT DEFAULT seq.nextval PRIMARY KEY,
  title VARCHAR(20) NOT NULL,
  content VARCHAR(100),
  registYmdt  TIMESTAMP,
  modifyYmdt  TIMESTAMP
);