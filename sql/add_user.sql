DROP FUNCTION IF EXISTS add_user(VARCHAR(80), VARCHAR(16), VARCHAR(80), BIT);
CREATE OR REPLACE FUNCTION add_user(_u_name VARCHAR(80), _u_pass VARCHAR(16), _email VARCHAR(80), _u_type BIT)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN
	BEGIN
		INSERT INTO users(u_id, u_name, u_pass, email, u_type, u_last_login)
		VALUES (uuid_generate_v1(), _u_name, _u_pass, _email, _u_type, now());

	EXCEPTION 
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = _exception_err;
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = _exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS _exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = _exception_err;
	
	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';