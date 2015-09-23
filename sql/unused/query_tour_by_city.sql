DROP FUNCTION IF EXISTS query_tour_by_city(city_name VARCHAR(255), region_name VARCHAR (255), country_name VARCHAR(255));
CREATE OR REPLACE FUNCTION query_tour_by_city(city_name VARCHAR(255), region_name VARCHAR (255), country_name VARCHAR(255))
	RETURNS SETOF tours AS 
$$
DECLARE
	_city_id      VARCHAR(255);
	_city_name    VARCHAR(255);
	_region_name  VARCHAR(255);
	_country_name VARCHAR(255);
BEGIN
	_city_id = find_cityid_by_name(city_name, region_name, country_name);

	SELECT cities.city, states.region, country.country
	INTO _city_name, _region_name, _country_name
	FROM cities, states, country 
	WHERE cities.cityid = _city_id
	AND states.regionid = cities.regionid 
	AND country.countryid = cities.countryid;
	
	RETURN QUERY 
	SELECT users.u_name, tours.t_id, _city_name, _region_name, _country_name, tours.t_rating, languages.lang_name, tours.t_duration, tours.t_location, tours.t_description, tours.t_photos, tours.t_comments, tours.t_available
	FROM users, tours, languages
	WHERE tours.t_cityid = _city_id
	AND users.u_id = tours.u_id
	AND languages.lang_id = tours.t_languages;
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';