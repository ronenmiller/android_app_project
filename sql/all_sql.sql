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
			A person's details should not be deleted even if the user chooses to delete himself
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
	u_id 		uuid 	     REFERENCES users(u_id), 
	p_first		VARCHAR(80)  NOT NULL,
	p_last		VARCHAR(80),
	email		VARCHAR(80)  PRIMARY KEY REFERENCES users(email) ON UPDATE CASCADE, -- email address must be unique
	phone_number	VARCHAR(80) REFERENCES users(phone_number) ON UPDATE CASCADE,
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
	u_id 		uuid 	     NOT NULL REFERENCES users(u_id) ON DELETE RESTRICT,
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

CREATE OR REPLACE VIEW view_city_by_name AS
SELECT cities.city, states.region, country.country
FROM cities, states, country;

--create OR REPLACE VIEW view_slots AS
--SElECT 

CREATE OR REPLACE FUNCTION add_user(p_u_name VARCHAR(80), p_u_pass VARCHAR(16), p_email VARCHAR(80), p_phone_number VARCHAR(80), p_u_type BIT)
	RETURNS VOID AS 
$$
DECLARE
	v_exception_err TEXT;
BEGIN
	BEGIN
		INSERT INTO users(u_id, u_name, u_pass, email, phone_number, u_type, u_last_login)
		VALUES (uuid_generate_v1(), p_u_name, p_u_pass, p_email, p_phone_number, p_u_type, now());

	EXCEPTION 
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;
	
	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';


/* Adds a regisitered user's personal details to the database.

* @param  u_id	    	the allocated user id to each person
* @param  p_first		person's first name
* @param  p_last		person's last name
* @param  email			person's email address
* @param  phone_number  person's phone number (for tours approval and security)
* @param  city_str		person's city of residence
* @param  state_str		the state where the above city is located
* @param  country_str   the country where the above state is located
* @param  languages		preffered language to be used in the app UI
  
* @return VOID		  
*/

DROP FUNCTION IF EXISTS add_person(uuid, VARCHAR(16), VARCHAR(80), VARCHAR(80), VARCHAR(255), VARCHAR(255), VARCHAR(255), INTEGER);
CREATE OR REPLACE FUNCTION add_person(u_id uuid, p_first VARCHAR(80), p_last VARCHAR(80), email VARCHAR(80), phone_number VARCHAR(80), city_str VARCHAR(255), state_str VARCHAR(255), country_str VARCHAR(255), languages INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN
	BEGIN
		INSERT INTO people(u_id, p_first, p_last, email, phone_number, city_str, state_str, country_str, languages)
		VALUES (u_id, p_first, p_last, email, phone_number, city_str, state_str, country_str, languages);

	EXCEPTION 
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = _exception_err;
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = _exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = _exception_err;
	
	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';


/* TODO: need to remove all user's appearances in other tables as well */
DROP FUNCTION IF EXISTS rm_user(VARCHAR(80), VARCHAR(16));
CREATE OR REPLACE FUNCTION rm_user(p_u_name VARCHAR(80), p_u_pass VARCHAR(16))
	RETURNS VOID AS 
$$
DECLARE
	v_exception_err TEXT;
BEGIN
	BEGIN
		-- A person's details should not be deleted even if the user chooses to delete himself
		ALTER TABLE people DROP CONSTRAINT IF EXISTS people_u_id_fkey;
		ALTER TABLE people DROP CONSTRAINT IF EXISTS people_email_fkey;
		ALTER TABLE people DROP CONSTRAINT IF EXISTS people_phone_number_fkey;

		-- TODO: add users to slots and attach ON DELETE RESTRICT (if a user is signed up to a tour, delete operation should fail)
		DELETE FROM users 
		WHERE (users.u_name = p_u_name AND users.u_pass = p_u_pass);

		ALTER TABLE people ADD CONSTRAINT people_u_id_fkey FOREIGN KEY (u_id) REFERENCES users(u_id) NOT VALID;
		ALTER TABLE people ADD CONSTRAINT people_email_fkey FOREIGN KEY (email) REFERENCES users(email) NOT VALID;
		ALTER TABLE people ADD CONSTRAINT people_phone_number_fkey FOREIGN KEY (phone_number) REFERENCES users(phone_number) NOT VALID;

	EXCEPTION 
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;
	END;
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';


/* add tour */
CREATE OR REPLACE FUNCTION add_tour(p_u_id uuid, p_city_name VARCHAR(255), p_region_name VARCHAR (255), p_country_name VARCHAR(255), p_t_duration NUMERIC, p_t_description TEXT, p_t_photos bytea[], p_t_languages INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	v_city_id		VARCHAR(255);
	v_tour_num		INTEGER;
	v_exception_err TEXT;
BEGIN
	v_city_id = find_cityid_by_name(p_city_name, p_region_name, p_country_name);
	
	SELECT max(tours.t_id) INTO v_tour_num FROM tours;
	IF v_tour_num IS NULL THEN
		v_tour_num = 1;
	ELSE
		v_tour_num = v_tour_num + 1;
	END IF;
	
	BEGIN
		INSERT INTO tours(u_id, t_id, t_cityid, t_duration, t_description, t_photos, t_languages, t_available)
		VALUES (p_u_id, v_tour_num, v_city_id, p_t_duration, p_t_description, p_t_photos, p_t_languages, B'0');

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION add_slot(p_t_id INTEGER, p_t_date DATE, p_t_time TIME WITH TIME ZONE, p_t_price REAL, p_t_vacant INTEGER)
	RETURNS SETOF VOID AS 
$$
DECLARE
	v_slot_num		INTEGER;
	v_exception_err TEXT;
BEGIN
	SELECT max(slots.ts_id) INTO v_slot_num FROM slots;
	IF v_slot_num IS NULL THEN
		v_slot_num = 1;
	ELSE
		v_slot_num = v_slot_num + 1;
	END IF;
	
	BEGIN
		INSERT INTO slots(t_id, ts_id, ts_date, ts_time, ts_price, ts_vacant, ts_active)
		VALUES (p_t_id, v_slot_num, p_t_date, p_t_time, p_t_price, p_t_vacant, B'1');

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION add_rating_2_tour(p_u_id uuid, p_t_id INTEGER, p_t_rating SMALLINT)
	RETURNS VOID AS 
$$
DECLARE
	v_u_id			uuid;
	v_exception_err TEXT;
BEGIN
	SELECT u_id
	INTO v_u_id
	FROM tours
	WHERE t_id = p_t_id;
	
	IF v_u_id = p_u_id THEN
		RAISE EXCEPTION 'ERROR: a guide cannot rate his own tour';	
	END IF;
	
	BEGIN
		INSERT INTO rate2tour(u_id, t_id, t_rating)
		VALUES (p_u_id, p_t_id, p_t_rating);

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;
	
	UPDATE tours
	SET t_rating = (SELECT DISTINCT avg(t_rating) OVER (PARTITION BY t_id) FROM rate2tour WHERE t_id = p_t_id)
	WHERE t_id = p_t_id;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION add_rating_2_guide(p_u_id uuid, p_g_id uuid, p_g_rating SMALLINT)
	RETURNS VOID AS 
$$
DECLARE
	v_g_name		VARCHAR(80);
	v_g_type		BIT;
	v_exception_err TEXT;
BEGIN
	SELECT u_type
	INTO v_g_type
	FROM users
	WHERE u_id = p_g_id;
	
	IF v_g_type != B'1' THEN
		RAISE EXCEPTION 'ERROR: user % is not a guide', v_g_name;
	END IF;
	IF p_u_id = p_g_id THEN
		RAISE EXCEPTION 'ERROR: a guide cannot rate himeself/herself';
	END IF;
	
	BEGIN
		INSERT INTO rate2guide(u_id, g_id, g_rating)
		VALUES (p_u_id, p_g_id, p_g_rating);

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;
	
	UPDATE users
	SET u_rating = (SELECT DISTINCT avg(g_rating) OVER (PARTITION BY g_id) FROM rate2guide WHERE g_id = p_g_id)
	WHERE u_id = p_g_id;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION search_tour_by_guide(v_guide_name VARCHAR(255))
	RETURNS SETOF view_tours AS 
$$
DECLARE
	v_guide_id	      uuid;
	v_guide_db_name   VARCHAR(80);
BEGIN
	SELECT users.u_id, users.u_name
	INTO v_guide_id, v_guide_db_name
	FROM users
	WHERE users.u_name = v_guide_name;

	IF NOT FOUND THEN
        RAISE EXCEPTION 'No guide named % was found', $1;
    END IF;
	
	RETURN QUERY 
	SELECT v_guide_db_name, tours.t_id, cities.city, states.region, country.country, tours.t_rating, languages.lang_name, tours.t_duration, tours.t_description, tours.t_photos, tours.t_comments, tours.t_available
	FROM users, tours, languages, cities, states, country
	WHERE tours.u_id = v_guide_id
	AND cities.cityid = tours.t_cityid
	AND states.regionid = cities.regionid
	AND country.countryid = cities.countryid
	AND languages.lang_id = tours.t_languages;
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION search_tour_by_city(p_city_name VARCHAR(255), p_region_name VARCHAR (255), p_country_name VARCHAR(255))
	RETURNS SETOF view_tours AS 
$$
DECLARE
	v_city_id      VARCHAR(255);
	v_city_name    VARCHAR(255);
	v_region_name  VARCHAR(255);
	v_country_name VARCHAR(255);
BEGIN
	v_city_id = find_cityid_by_name(p_city_name, p_region_name, p_country_name);

	SELECT cities.city, states.region, country.country
	INTO v_city_name, v_region_name, v_country_name
	FROM cities, states, country 
	WHERE cities.cityid = v_city_id
	AND states.regionid = cities.regionid 
	AND country.countryid = cities.countryid;
	
	RETURN QUERY 
	SELECT users.u_name, tours.t_id, v_city_name, v_region_name, v_country_name, tours.t_rating, languages.lang_name, tours.t_duration, tours.t_description, tours.t_photos, tours.t_comments, tours.t_available
	FROM users, tours, languages
	WHERE tours.t_cityid = v_city_id
	AND users.u_id = tours.u_id
	AND languages.lang_id = tours.t_languages;
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION request_tour(p_u_id uuid, p_ts_id INTEGER, p_r_num_participants INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	v_ts_active		BIT;
	v_ts_vacant		INTEGER;
	v_exception_err TEXT;
BEGIN
	SELECT ts_vacant, ts_active
	INTO v_ts_vacant, v_ts_active
	FROM slots
	WHERE ts_id = p_ts_id;
	
	IF v_ts_active != B'1' THEN
		RAISE EXCEPTION 'ERROR: the requested slot is not active';
	END IF;
	IF (v_ts_vacant - p_r_num_participants) < 0 THEN
		RAISE EXCEPTION 'ERROR: too many participants for this slot. only % places remaining', v_ts_vacant;
	END IF; 
		
	BEGIN
		INSERT INTO requests(u_id, ts_id, r_num_participants, r_active)
		VALUES (p_u_id, p_ts_id, p_r_num_participants, B'0');

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION find_cityid_by_name(p_city_name VARCHAR(255), p_region_name VARCHAR (255), p_country_name VARCHAR(255))
	RETURNS SETOF VARCHAR(255) AS 
$$
DECLARE
	v_country_id      VARCHAR(255);
	v_region_id       VARCHAR(255);
BEGIN
	SELECT country.countryid
	INTO v_country_id
	FROM country
	WHERE country.country = p_country_name;

	IF NOT FOUND THEN
        RAISE EXCEPTION 'No country named % was found', $3;
    END IF;

	SELECT states.regionid
	INTO v_region_id
	FROM states 
	WHERE states.countryid = v_country_id 
	AND states.region = p_region_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'No region named % in country % was found', $2, $3;
    END IF;
    
	RETURN QUERY 
	SELECT cities.cityid
	--INTO v_city_id
	FROM cities 
	WHERE cities.countryid = v_country_id 
	AND cities.regionid = v_region_id 
	AND cities.city = p_city_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'No city named % was found', $1;
    END IF;
    
    RETURN;
	
	--RETURN v_city_id;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION open_slots_by_tour(p_t_id INTEGER)
	RETURNS SETOF slots AS 
$$
BEGIN
	RETURN QUERY 
	SELECT *
	FROM slots
	WHERE slots.t_id = p_t_id
	AND slots.ts_active = B'1'
	HAVING slots.ts_vacant > 0;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION approve_request(p_u_id uuid, p_ts_id INTEGER, p_t_num_participants INTEGER)
	RETURNS VOID AS 
$$
BEGIN
	UPDATE slots
	SET ts_vacant = ts_vacant - p_t_num_participants
	WHERE ts_id = p_ts_id;

	UPDATE requests
	SET r_active = B'1'
	WHERE u_id = p_u_id
	AND ts_id = p_ts_id;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION open_slots_by_tour(p_t_id INTEGER)
	RETURNS SETOF slots AS 
$$
BEGIN
	RETURN QUERY 
	SELECT *
	FROM slots
	WHERE slots.t_id = p_t_id
	AND slots.ts_active = B'1'
	HAVING slots.ts_vacant > 0;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';

/* Finds a unique city ID in the database based on the city's name, region, and country.  
*
* @param  city_name	    the name of the requested city
* @param  region_name	the name of the region/state where the city is located
* @param  country_name	the name of the country where the city is located
* @return VARCHAR(255)	the requested city's ID.		  
*/

DROP FUNCTION IF EXISTS query_cityid_by_name(CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING);
CREATE OR REPLACE FUNCTION query_cityid_by_name(city_name VARCHAR(255), region_name VARCHAR (255), country_name VARCHAR(255))
	RETURNS VARCHAR(255) AS 
$$
DECLARE
	_country_id      VARCHAR(255);
	_region_id       VARCHAR(255);
	_city_id 		 VARCHAR(255);
BEGIN
	SELECT country.countryid
	INTO _country_id
	FROM country
	WHERE country.country = country_name;

	IF NOT FOUND THEN
        RAISE EXCEPTION 'No country named % was found', $3;
    END IF;

	SELECT states.regionid
	INTO _region_id
	FROM states 
	WHERE states.countryid = _country_id 
	AND states.region = region_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'No region named % in country % was found', $2, $3;
    END IF;
    
	SELECT cities.cityid
	INTO _city_id
	FROM cities 
	WHERE cities.countryid = _country_id 
	AND cities.regionid = _region_id 
	AND cities.city = city_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'No city named % in region % of country % was found', $1, $2, $3;
    END IF;
    
    RETURN _city_id;

END;
$$ 
LANGUAGE 'plpgsql';