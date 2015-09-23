DROP FUNCTION IF EXISTS add_slot(INTEGER, DATE, TIME WITH TIME ZONE, INTEGER);
CREATE OR REPLACE FUNCTION add_slot(id INTEGER, s_date DATE, s_time TIME WITH TIME ZONE, s_vacant INTEGER)
	RETURNS SETOF VOID AS 
$$
DECLARE
	_slot_num		INTEGER;
	_exception_err TEXT;
BEGIN
	SELECT max(slots.ts_id) INTO _slot_num FROM slots;
	IF _slot_num IS NULL THEN
		_slot_num = 1;
	ELSE
		_slot_num = _slot_num + 1;
	END IF;
	
	BEGIN
		INSERT INTO slots(t_id, ts_id, ts_date, ts_time, ts_vacant, ts_active)
		VALUES (id, _slot_num, s_date, s_time, s_vacant, B'1');

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = _exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = _exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = _exception_err;

	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';