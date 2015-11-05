DROP FUNCTION IF EXISTS delete_reservation(BIGINT, TEXT);
CREATE OR REPLACE FUNCTION delete_reservation(_slot_id BIGINT, _user_id TEXT)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN

	BEGIN
		UPDATE reservations SET r_active = B'0' WHERE reservations.s_id = _slot_id AND reservations.u_id = _user_id::uuid;

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
