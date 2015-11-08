DROP FUNCTION IF EXISTS delete_tour(INTEGER);
CREATE OR REPLACE FUNCTION delete_tour(_t_id INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN

	BEGIN
		UPDATE tours SET t_available = B'0' WHERE tours.t_id = _t_id;

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
