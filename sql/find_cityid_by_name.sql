CREATE OR REPLACE FUNCTION find_cityid_by_name(p_city_name VARCHAR(255), p_region_name VARCHAR (255), p_country_name VARCHAR(255))
	RETURNS VARCHAR(255) AS 
$$
DECLARE
	v_country_id      VARCHAR(255);
	v_region_id       VARCHAR(255);
	v_city_id         VARCHAR(255);
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
    
	SELECT cities.cityid
	INTO v_city_id
	FROM cities 
	WHERE cities.countryid = v_country_id 
	AND cities.regionid = v_region_id 
	AND cities.city = p_city_name;
	
	IF NOT FOUND THEN
        RAISE EXCEPTION 'No city named % was found', $1;
    END IF;
	
	RETURN v_city_id;
END;
$$ 
LANGUAGE 'plpgsql';