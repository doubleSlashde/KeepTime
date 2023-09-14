CREATE TABLE IF NOT EXISTS PUBLIC.USERS (
	USERNAME VARCHAR_IGNORECASE(50) NOT NULL,
	PASSWORD VARCHAR_IGNORECASE(500) NOT NULL,
	ENABLED BOOLEAN NOT NULL,
	CONSTRAINT IF NOT EXISTS CONSTRAINT_4 PRIMARY KEY (USERNAME)
);

CREATE TABLE IF NOT EXISTS PUBLIC.AUTHORITIES (
	USERNAME VARCHAR_IGNORECASE(50) NOT NULL,
	AUTHORITY VARCHAR_IGNORECASE(50) NOT NULL,
	CONSTRAINT IF NOT EXISTS FK_AUTHORITIES_USERS PRIMARY KEY (USERNAME,USERNAME)
);
CREATE INDEX IF NOT EXISTS FK_AUTHORITIES_USERS_INDEX_A  ON PUBLIC.AUTHORITIES (USERNAME);
CREATE UNIQUE INDEX IF NOT EXISTS IX_AUTH_USERNAME ON PUBLIC.AUTHORITIES (USERNAME,AUTHORITY);

ALTER TABLE PUBLIC.AUTHORITIES ADD CONSTRAINT IF NOT EXISTS FK_AUTHORITIES_USERS FOREIGN KEY (USERNAME) REFERENCES PUBLIC.USERS(USERNAME) ON DELETE RESTRICT ON UPDATE RESTRICT;