CREATE OR REPLACE FUNCTION add_user(p_u_name VARCHAR(80), p_u_pass VARCHAR(16), p_email VARCHAR(80), p_phone_number VARCHAR(80), p_u_type BIT)
	RETURNS VOID AS 
$$
DECLARE
	v_exception_err TEXT;
BEGIN
	BEGIN
		INSERT INTO users(u_id, u_name, u_pass, email, phone_number, u_type, u_last_login)
		VALUES (uuid_generate_v1(), p_u_name, p_u_pass, p_email, p_phone_number, p_u_type, now());

	EXCEPTION 
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;
	
	END;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';