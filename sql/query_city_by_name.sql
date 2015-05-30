/* Executes a query to find cities, regions and states whose names start with the supplied pattern. 
*
* @param  req_query	    a string representing the start of the name of the requested location
* @return table			all the cities, regions and countries whose names start with the input pattern.			  
*/

DROP FUNCTION IF EXISTS query_city_by_name(CHARACTER VARYING);
CREATE OR REPLACE FUNCTION query_city_by_name(req_query VARCHAR(80))
	RETURNS SETOF view_city_by_name	AS 
$$
BEGIN

	-- match cities to the requested query
	RETURN QUERY
	SELECT DISTINCT cities.city, states.region, country.country
	FROM cities, states, country
	WHERE cities.city LIKE (req_query || '%') 
	AND states.regionid = cities.regionid
	AND country.countryid = cities.countryid; 

	-- match regions to the requested query 
	RETURN QUERY
	SELECT DISTINCT cities.city, states.region, country.country
	FROM cities, states, country
	WHERE states.region LIKE (req_query || '%') 
	AND cities.regionid = states.regionid 
	AND country.countryid = states.countryid;

	-- match countries to the requested query
	RETURN QUERY
	SELECT DISTINCT cities.city, states.region, country.country
	FROM cities, states, country
	WHERE country.country LIKE (req_query || '%') 
	AND cities.countryid = country.countryid
	AND states.countryid = country.countryid;

	RETURN;
	
END;
$$ 
LANGUAGE 'plpgsql';