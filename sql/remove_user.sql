/* TODO: need to remove all user's appearances in other tables as well */
CREATE OR REPLACE FUNCTION rm_user(p_u_name VARCHAR(80), p_u_pass VARCHAR(16))
	RETURNS VOID AS 
$$
DECLARE
	v_exception_err TEXT;
BEGIN
	BEGIN
		DELETE FROM users WHERE (u_name = p_u_name AND u_pass = p_u_pass);
	EXCEPTION 
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;
	END;
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';