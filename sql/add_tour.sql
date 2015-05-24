CREATE OR REPLACE FUNCTION add_tour(p_u_id uuid, p_city_name VARCHAR(255), p_region_name VARCHAR (255), p_country_name VARCHAR(255), p_t_duration NUMERIC, p_t_description TEXT, p_t_photos bytea[], p_t_languages INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	v_city_id		VARCHAR(255);
	v_tour_num		INTEGER;
	v_exception_err TEXT;
BEGIN
	v_city_id = find_cityid_by_name(p_city_name, p_region_name, p_country_name);
	
	SELECT max(tours.t_id) INTO v_tour_num FROM tours;
	IF v_tour_num IS NULL THEN
		v_tour_num = 1;
	ELSE
		v_tour_num = v_tour_num + 1;
	END IF;
	
	BEGIN
		INSERT INTO tours(u_id, t_id, t_cityid, t_duration, t_description, t_photos, t_languages, t_available)
		VALUES (p_u_id, v_tour_num, v_city_id, p_t_duration, p_t_description, p_t_photos, p_t_languages, B'0');

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';