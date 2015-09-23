CREATE OR REPLACE FUNCTION request_tour(p_u_id uuid, p_ts_id INTEGER, p_r_num_participants INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	v_ts_active		BIT;
	v_ts_vacant		INTEGER;
	v_exception_err TEXT;
BEGIN
	SELECT ts_vacant, ts_active
	INTO v_ts_vacant, v_ts_active
	FROM slots
	WHERE ts_id = p_ts_id;
	
	IF v_ts_active != B'1' THEN
		RAISE EXCEPTION 'ERROR: the requested slot is not active';
	END IF;
	IF (v_ts_vacant - p_r_num_participants) < 0 THEN
		RAISE EXCEPTION 'ERROR: too many participants for this slot. only % places remaining', v_ts_vacant;
	END IF; 
		
	BEGIN
		INSERT INTO requests(u_id, ts_id, r_num_participants, r_active)
		VALUES (p_u_id, p_ts_id, p_r_num_participants, B'0');

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