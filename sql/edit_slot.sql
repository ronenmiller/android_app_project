DROP FUNCTION IF EXISTS edit_slot(BIGINT, INTEGER, BIGINT, INTEGER);
CREATE OR REPLACE FUNCTION edit_slot(_s_id BIGINT, _date INTEGER,
 _time BIGINT, _capacity INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	_v_exception_err TEXT;
BEGIN

	BEGIN
	    UPDATE slots
	    set s_capacity = s_capacity + (_capacity - s_total_capacity)
	    WHERE s_id = _s_id;

		UPDATE slots
		SET s_date = _date, s_time = _time, s_total_capacity = s_total_capacity + (_capacity - s_total_capacity)
    	WHERE s_id = _s_id;

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
