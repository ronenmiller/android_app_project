CREATE OR REPLACE FUNCTION add_user(v_u_name VARCHAR(80), v_u_pass VARCHAR(16), v_email VARCHAR(80), v_phone_number VARCHAR(80), v_u_type BIT)
	RETURNS TEXT AS 
$$
DECLARE
 constraint_err text;
BEGIN
	BEGIN
		INSERT INTO users(u_id, u_name, u_pass, email, phone_number, u_type, u_last_login)
		VALUES (uuid_generate_v1(), v_u_name, v_u_pass, v_email, v_phone_number, v_u_type, now());

	EXCEPTION WHEN unique_violation THEN
		GET STACKED DIAGNOSTICS constraint_err = CONSTRAINT_NAME;
		RETURN constraint_err;
	END;

	RETURN NULL;
END;
$$ 
LANGUAGE 'plpgsql';

SELECT add_user('ronen','999mmfkd','a@gmail.com','05454948484',B'1');
SELECT add_user('liron','999mmfkd','la@gmail.com','05454948484',B'1');