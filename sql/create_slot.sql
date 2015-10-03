DROP FUNCTION IF EXISTS create_slot(TEXT, INTEGER, INTEGER, BIGINT, INTEGER);
CREATE OR REPLACE FUNCTION create_slot(_u_id TEXT, _t_id INTEGER, _date INTEGER,
 _time BIGINT, _capacity INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	_v_exception_err TEXT;
BEGIN

	BEGIN
		INSERT INTO slots(u_id, t_id, s_date, s_time, s_capacity, s_active)
		VALUES (_u_id::uuid, _t_id, _date, _time, _capacity, B'1');

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
