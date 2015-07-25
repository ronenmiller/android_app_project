DROP FUNCTION IF EXISTS add_slot(INTEGER, date, TIME WITH TIME ZONE, REAL, INTEGER);
CREATE OR REPLACE FUNCTION add_slot(p_t_id INTEGER, p_t_date DATE, p_t_time TIME WITH TIME ZONE, p_t_vacant INTEGER)
	RETURNS SETOF VOID AS 
$$
DECLARE
	v_slot_num		INTEGER;
	v_exception_err TEXT;
BEGIN
	SELECT max(slots.ts_id) INTO v_slot_num FROM slots;
	IF v_slot_num IS NULL THEN
		v_slot_num = 1;
	ELSE
		v_slot_num = v_slot_num + 1;
	END IF;
	
	BEGIN
		INSERT INTO slots(t_id, ts_id, ts_date, ts_time, ts_vacant, ts_active)
		VALUES (p_t_id, v_slot_num, p_t_date, p_t_time, p_t_vacant, B'1');

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