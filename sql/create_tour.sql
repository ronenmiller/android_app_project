DROP FUNCTION IF EXISTS create_tour(BIGINT, TEXT, VARCHAR(255), INTEGER,  NUMERIC, VARCHAR(255), TEXT);
CREATE OR REPLACE FUNCTION create_tour(_osm_id BIGINT, _u_id TEXT, _title VARCHAR(255), _language INTEGER,
 _duration NUMERIC, _location VARCHAR(255), _description TEXT)
	RETURNS VOID AS 
$$
DECLARE
	_v_exception_err TEXT;
BEGIN

	BEGIN
		INSERT INTO tours(t_osm_id, m_id, t_title, t_language, t_duration, t_location, t_description, t_available)
		VALUES (_osm_id, _u_id::uuid, _title, _language, _duration, _location, _description, B'1');

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS _v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = _v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS _v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = _v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS _v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = _v_exception_err;

	END;

	RETURN;

END;
$$ 
LANGUAGE 'plpgsql';