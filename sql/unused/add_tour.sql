DROP FUNCTION IF EXISTS add_tour(uuid, VARCHAR(255), VARCHAR (255), VARCHAR(255), NUMERIC, TEXT, bytea[], INTEGER);
CREATE OR REPLACE FUNCTION add_tour(u_id uuid, city_name VARCHAR(255), region_name VARCHAR (255), country_name VARCHAR(255), duration NUMERIC,
start_location VARCHAR(255), description TEXT, photos bytea[], languages INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	_v_city_id		VARCHAR(255);
	_v_tour_num		INTEGER;
	_v_exception_err TEXT;
BEGIN
	_v_city_id = find_cityid_by_name(city_name, region_name, country_name);
	
	SELECT max(tours.t_id) INTO _v_tour_num FROM tours;
	IF _v_tour_num IS NULL THEN
		_v_tour_num = 1;
	ELSE
		_v_tour_num = _v_tour_num + 1;
	END IF;
	
	BEGIN
		INSERT INTO tours(u_id, t_id, t_cityid, t_duration, t_description, t_photos, t_languages, t_available)
		VALUES (u_id, _v_tour_num, _v_city_id, duration, start_location, description, photos, languages, B'0');

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