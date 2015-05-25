-- build extension for uuid generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- when database is completed, remove "DROP TABLE IF ..." lines

/*
SECTION: 	Build users's types table 
DESC:		Build the types table containing what type of users are possible - a regular user or guide (who's also a user)
TABLE:		type_id - a unique type ID (primary key),
		type_name - what type is the user
*/
DROP TABLE IF EXISTS types_tbl CASCADE;
CREATE TABLE types_tbl (
	type_id		BIT 	   PRIMARY KEY,
	type_name	VARCHAR(7) UNIQUE NOT NULL
)
WITH (
  OIDS = FALSE
);
ALTER TABLE types_tbl OWNER TO postgres;

-- create the two possible user types
INSERT INTO types_tbl VALUES (B'0','Regular');
INSERT INTO types_tbl VALUES (B'1','Guide');

/*
SECTION: 	Build languages table 
DESC:		Build the languages table containing which languages are currently supported
TABLE:		lang_id - a unique language ID (primary key),
		lang_name - language name
*/ 
DROP TABLE IF EXISTS languages CASCADE;
CREATE TABLE languages (
	lang_id		SERIAL 	    PRIMARY KEY,
	lang_name	VARCHAR(16) UNIQUE NOT NULL
)
WITH (
  OIDS = FALSE
);
ALTER TABLE languages OWNER TO postgres;

--- a few widely-used languages
INSERT INTO languages (lang_name) VALUES ('English');
INSERT INTO languages (lang_name) VALUES ('Espanol');
INSERT INTO languages (lang_name) VALUES ('Francais');
INSERT INTO languages (lang_name) VALUES ('Deutsch');
INSERT INTO languages (lang_name) VALUES ('Hebrew');
INSERT INTO languages (lang_name) VALUES ('Chinese');

/*
SECTION: 	Build users table 
DESC:		Build the users table containing basic user information for login and linking to other tables.
TABLE:		u_id - a unique user ID (primary key),
			u_name - a user must have a non-null unique username. username must begin with a character and be at least 5 characters long,
			u_pass - a user must have a non-null unique password. password must be at least 8 character and contain at least one digit and one character,
			email - must be entered and unique for each use,
			phone_number - must be entered and unique for SMS confirmation,
			u_type - a user can be a regular user or a guide: 0 - user, 1 - guide,
			u_last_login - timestamp of the last time the user logged in (use now() function),
			u_rating - user rating
*/
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
	u_id 		 uuid 	     PRIMARY KEY,
	u_name	 	 VARCHAR(80) UNIQUE NOT NULL,
	u_pass	 	 VARCHAR(16) NOT NULL,
	email		 VARCHAR(80) UNIQUE NOT NULL,
	phone_number VARCHAR(80) UNIQUE NOT NULL, -- must be entered and unique for SMS confirmation
	u_type		 BIT	     REFERENCES types_tbl(type_id) ON DELETE RESTRICT,
	u_last_login TIMESTAMP WITHOUT TIME ZONE NOT NULL, 
	u_rating	 REAL,
	CONSTRAINT chk_u_name_len   CHECK (char_length(u_name) >= 2), 
	CONSTRAINT chk_u_name_valid CHECK (u_name ~ '^([A-Za-z])+'), -- username must begin with a character
	CONSTRAINT chk_pass_len     CHECK (char_length(u_pass) >= 8),
	CONSTRAINT chk_pass_valid   CHECK (u_pass ~ '[0-9]+' AND u_pass ~ '([A-Za-z])+'), -- password must have at least one digit and one character
	CONSTRAINT chk_email_valid  CHECK (email ~ '^\w+(\w|\.)*@{1}(\w+\.+\w+)+$'),
	CONSTRAINT chk_rating_valid CHECK (u_rating >= 0 AND u_rating <= 10)
)
WITH (
  OIDS = FALSE
);
ALTER TABLE users OWNER TO postgres;

/*
SECTION: 	Build people table
DESC:		Build the people table and add additional information about each person
TABLE:		u_id - a unique user ID (references u_id in users table),
		p_first - person's first name,
		p_last - person's last name,
		email - unique for each user (primary key),
		phone_number - must be entered and unique for SMS confirmation,
		city_id - person current city of residence (ID),
		state_id - person current state of residence (ID),
		country_id - person current country of residence (ID),
		languages - languages spoken by the person
*/
DROP TABLE IF EXISTS people CASCADE;
CREATE TABLE people (
	u_id 		uuid 	     REFERENCES users (u_id) ON DELETE RESTRICT, -- holds unique user ID
	p_first		VARCHAR(80)  NOT NULL,
	p_last		VARCHAR(80),
	email		VARCHAR(80)  PRIMARY KEY, -- email address must be unique
	phone_number	VARCHAR(80)  UNIQUE NOT NULL, -- must be entered and unique for SMS confirmation
	city_str	VARCHAR(255),
	state_str	VARCHAR(255),
	country_str	VARCHAR(255),
	languages	INTEGER    REFERENCES languages(lang_id) ON DELETE RESTRICT, -- should be integer[], how to apply more than one language?
	CONSTRAINT chk_email_valid CHECK (email ~ '^\w+(\w|\.)*@{1}(\w+\.+\w+)+$')
)
WITH (
  OIDS = FALSE
);
ALTER TABLE people OWNER TO postgres;

/*
SECTION: 	Build tours table
DESC:		Build the tours table which contains general information on offered tours
TABLE:		u_id - a unique user ID of the tour's guide,
			t_id - a unique tour ID (primary key),
			t_city_id - tour is in which city (ID),
			t_state_id - tour is in which state (ID),
			t_country_id - tour is in which country (ID) tour is in which city (ID),
			t_duration - tour duration in minutes,
			t_description - general tour's description,
			t_photos - some photos of the tour (submitted by the guide),
			t_languages - all the languages the tour is given in (some might not be available in the slots table),
			t_rating - the given rating,
			t_comments - comments of users on the tour,
			t_available - currently there are open slots for this tour: 0 - no slots, 1 - open slots available
*/
DROP TABLE IF EXISTS tours CASCADE;
CREATE TABLE tours (
	u_id 		uuid 	     NOT NULL REFERENCES users(u_id),
	t_id		INTEGER      PRIMARY KEY,
	t_cityid	VARCHAR(255) NOT NULL REFERENCES cities(cityid) ON DELETE RESTRICT,
	t_duration	NUMERIC	     NOT NULL,
	t_description	TEXT,
	t_photos	bytea[], -- what data type?
	t_languages	INTEGER      NOT NULL REFERENCES languages(lang_id) ON DELETE RESTRICT, -- should be integer[], how to apply more than one language?
	t_rating	REAL, -- how to make sure the user participated in the tour? from the slots table once the date of the tour had passed?
	t_comments	VARCHAR(255)[], -- how to relate a comment to a certian user?
	t_available BIT,
	CONSTRAINT chk_duration_positive CHECK (t_duration > 0),
	CONSTRAINT chk_rating_valid CHECK (t_rating >= 0 AND t_rating <= 10)
)
WITH (
  OIDS = FALSE
);
ALTER TABLE tours OWNER TO postgres;

/*
SECTION: 	Build slots table
DESC:		Build the slots table which contains open slots for currently available tours
TABLE:		t_id - a unique tour ID,
			ts_id - a unique tour slot ID (primary key),
			ts_date - the tour departs on this date,
			ts_time - the tour departs on this time,
			ts_price - tour price per person,
			ts_vacant - the remaining number of people that can sign up for this tour,
			ts_active - once the is finished, slot is deactivated: 0 - not active, 1 - active 
*/
DROP TABLE IF EXISTS slots CASCADE;
CREATE TABLE slots (
	t_id		INTEGER  REFERENCES tours (t_id) ON DELETE RESTRICT,
	ts_id		INTEGER  PRIMARY KEY,
	ts_date		DATE     NOT NULL,
	ts_time		TIME WITH TIME ZONE NOT NULL,
	ts_price	REAL 	 NOT NULL,
	ts_vacant 	INTEGER  NOT NULL,
	ts_active	BIT 	 NOT NULL,
	CONSTRAINT chk_price_non_negative CHECK (ts_price >= 0),
	CONSTRAINT chk_vacant_non_negative CHECK (ts_vacant >= 0)
)
WITH (
  OIDS = FALSE
);

ALTER TABLE slots OWNER TO postgres;

/*
SECTION: 	Build requests table
DESC:		Build the requests table which contains requests to sign up for a tour
TABLE:		u_id - a unique user ID of the requesting user
			t_id - a unique tour ID of the requested tour,
			ts_id - a unique tour slot ID
			r_num_participants - number of participants that are signed to the tour under the current user 
			(a user can sign up his friends and family to the tour, not just himself),
			r_active - the user participation has been approved: 0 - not approved, 1- approved
			r_active is used to show the user to which slots he/she is currently signed on
			-- TODO: remove user from table once the tour is complete
*/
DROP TABLE IF EXISTS requests CASCADE;
CREATE TABLE requests (
	u_id		   uuid    NOT NULL REFERENCES users(u_id) ON DELETE CASCADE,
	ts_id		   INTEGER NOT NULL REFERENCES slots(ts_id) ON DELETE RESTRICT,
	r_num_participants INTEGER NOT NULL,
	r_active	   BIT
	CONSTRAINT chk_participants_non_negative CHECK (r_num_participants >= 0),
	PRIMARY KEY (u_id, ts_id)
)
WITH (
  OIDS = FALSE
);

ALTER TABLE requests OWNER TO postgres;

/*
SECTION: 	Build tours rating table
DESC:		Build the tours rating table. The table is used to store rating of tours by users whom participated in the rated tour.
			It is also used to make sure that each user has rated a specific tour only once.
TABLE:		u_id - a unique user ID whom rates the tour,
			t_id - a unique tour ID which is rated,
			t_rating - the given rating
*/
DROP TABLE IF EXISTS rate2tour CASCADE;
CREATE TABLE rate2tour (
	u_id		uuid     NOT NULL REFERENCES users(u_id) ON DELETE CASCADE,
	t_id		INTEGER  NOT NULL REFERENCES tours (t_id) ON DELETE CASCADE,
	t_rating	SMALLINT NOT NULL, -- how to make sure the user participated in the tour? from the slots table once the date of the tour had passed?
	CONSTRAINT chk_rating_valid CHECK (t_rating >= 0 AND t_rating <= 10),
	PRIMARY KEY (u_id, t_id)
)
WITH (
  OIDS = FALSE
);
ALTER TABLE rate2tour OWNER TO postgres;

/*
SECTION: 	Build guides rating table
DESC:		Build the guides rating table. The table is used to store rating of guides by users whom participated in their tour.
			It is also used to make sure that each user has rated a guide only once for each tour.
TABLE:		u_id - a unique user ID whom rates the tour,
			t_id - a unique tour ID in which the user participated,
			g_id - a unique user ID of the guide being rated
			g_rating - the given rating
*/
DROP TABLE IF EXISTS rate2guide CASCADE;
CREATE TABLE rate2guide (
	u_id		uuid     NOT NULL REFERENCES users(u_id) ON DELETE CASCADE,
	g_id		uuid	 NOT NULL REFERENCES users(u_id) ON DELETE CASCADE, -- on insertion, check that this user is really a guide
	g_rating	SMALLINT NOT NULL, -- how to make sure the user participated in the tour? from the slots table once the date of the tour had passed?
	CONSTRAINT chk_rating_valid CHECK (g_rating >= 0 AND g_rating <= 10),
	PRIMARY KEY (u_id, g_id)
)
WITH (
  OIDS = FALSE
);
ALTER TABLE rate2guide OWNER TO postgres;

CREATE OR REPLACE VIEW view_tours AS
SELECT users.u_name, tours.t_id, cities.city, states.region, country.country, tours.t_rating, languages.lang_name, tours.t_duration, tours.t_description, tours.t_photos, tours.t_comments, tours.t_available
FROM users, tours, languages, cities, states, country;

--create OR REPLACE VIEW view_slots AS
--SElECT 