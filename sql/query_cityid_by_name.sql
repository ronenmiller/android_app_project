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