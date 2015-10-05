DROP FUNCTION IF EXISTS add_location(BIGINT, VARCHAR(255), VARCHAR(16), REAL, REAL);
CREATE OR REPLACE FUNCTION add_location(_osm_id BIGINT, _display_name VARCHAR(255), _type VARCHAR(16),
 _lat REAL, _lon REAL)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN

	BEGIN
		INSERT INTO locations(osm_id, display_name, type, lat, lon, count)
		VALUES (_osm_id, _display_name, _type, _lat, _lon, 1);

	EXCEPTION
	
		WHEN unique_violation THEN
            UPDATE locations
            SET count = count + 1
            WHERE locations.osm_id = _osm_id;
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
