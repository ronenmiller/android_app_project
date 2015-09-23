DROP FUNCTION IF EXISTS query_slots_by_city(city_name VARCHAR(255), region_name VARCHAR (255), country_name VARCHAR(255), today DATE);
CREATE OR REPLACE FUNCTION query_slots_by_city(city_name VARCHAR(255), region_name VARCHAR (255), country_name VARCHAR(255), today DATE)
	RETURNS SETOF view_tours AS 
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
	AND languages.lang_id = tours.t_languages
	AND slots.ts_date >= today
	AND slots.ts_vacant > 0
	AND slots.ts_active = B'1';
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';