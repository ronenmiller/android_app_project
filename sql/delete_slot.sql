DROP FUNCTION IF EXISTS delete_slot(BIGINT);
CREATE OR REPLACE FUNCTION delete_slot(_s_id BIGINT)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN

	BEGIN
		UPDATE slots SET s_active = B'0', s_canceled = B'1' WHERE slots.s_id = _s_id;

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
