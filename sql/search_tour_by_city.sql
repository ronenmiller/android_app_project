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