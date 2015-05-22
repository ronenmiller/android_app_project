CREATE OR REPLACE VIEW view_tours_by_city AS
SELECT users.u_name, tours.t_id, cities.city, states.region, country.country, tours.t_rating, languages.lang_name, tours.t_duration, tours.t_description, tours.t_photos, tours.t_comments
FROM users, tours, languages, cities, states, country;

CREATE OR REPLACE FUNCTION search_tour_by_city(v_city_name VARCHAR(255), v_region_name VARCHAR (255), v_country_name VARCHAR(255))
	RETURNS SETOF view_tours_by_city AS 
$$
DECLARE
	v_country_id      VARCHAR(255);
	v_country_db_name VARCHAR(255);
	v_region_id       VARCHAR(255);
	v_region_db_name  VARCHAR(255);
	v_city_id         VARCHAR(255);
	v_city_db_name    VARCHAR(255);
BEGIN
	SELECT country.countryid, country.country
	INTO v_country_id, v_country_db_name
	FROM country
	WHERE country.country = v_country_name;

	IF NOT FOUND THEN
        RAISE EXCEPTION 'No country named % found', $3;
    END IF;

	SELECT states.regionid, states.region
	INTO v_region_id, v_region_db_name
	FROM states 
	WHERE states.countryid = v_country_id 
	AND states.region = v_region_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'No region named % in country % found', $2, $3;
    END IF;
    
	SELECT cities.cityid, cities.city
	INTO v_city_id, v_city_db_name
	FROM cities 
	WHERE cities.countryid = v_country_id 
	AND cities.regionid = v_region_id 
	AND cities.city = v_city_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'A city named % was not found in the database', $1;
    END IF;
	
	RETURN QUERY 
	SELECT users.u_name, tours.t_id, v_city_db_name, v_region_db_name, v_country_db_name, tours.t_rating, languages.lang_name, tours.t_duration, tours.t_description, tours.t_photos, tours.t_comments
	FROM users, tours, languages
	WHERE tours.t_cityid = v_city_id
	AND users.u_id = tours.u_id
	AND languages.lang_id = tours.t_languages;
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';