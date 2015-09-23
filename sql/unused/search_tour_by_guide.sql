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