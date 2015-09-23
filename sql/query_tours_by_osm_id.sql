DROP FUNCTION IF EXISTS query_tours_by_osm_id(BIGINT);
CREATE OR REPLACE FUNCTION query_tours_by_osm_id(osm_id BIGINT)
	RETURNS SETOF view_tours AS 
$$
BEGIN
	RETURN QUERY 
	SELECT tours.t_id, tours.t_title, tours.t_duration, tours.t_language, tours.t_location, tours.t_rating, tours.t_description
	FROM tours
	WHERE tours.t_osm_id = osm_id
	GROUP BY tours.t_id
	HAVING tours.t_available = B'1'
	ORDER BY tours.t_rating DESC;
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';