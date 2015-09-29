-- build extension for uuid generation
-- when database is completed, remove "DROP TABLE IF ..." lines
INSERT INTO languages (lang_name) VALUES ('English');
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE people (
ALTER TABLE people OWNER TO postgres;
DROP TABLE IF EXISTS tours CASCADE;
	t_id				INTEGER      		PRIMARY KEY,
ALTER TABLE slots OWNER TO postgres;
DESC:		Build the tours rating table. The table is used to store rating of tours by users whom participated in the rated tour.
DROP TABLE IF EXISTS rate2tour CASCADE;
WITH (
);
ALTER TABLE rate2tour OWNER TO postgres;
/*
				g_rating - the given rating
DROP TABLE IF EXISTS rate2guide CASCADE;
	g_id			uuid	 			NOT NULL REFERENCES users(u_id) ON DELETE CASCADE, -- on insertion, check that this user is really a guide
	PRIMARY KEY (u_id, g_id)
ALTER TABLE rate2guide OWNER TO postgres;
CREATE OR REPLACE VIEW view_tours AS