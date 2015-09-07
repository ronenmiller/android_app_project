DROP FUNCTION IF EXISTS validate_credentials(CHARACTER VARYING, CHARACTER VARYING);
CREATE OR REPLACE FUNCTION validate_credentials(_u_name VARCHAR(80), _u_pass VARCHAR(80))
	RETURNS BOOLEAN AS 
$$
DECLARE
	temp users;
BEGIN
	SELECT * 
	INTO temp
	FROM users 
	WHERE ((users.u_name = _u_name OR users.email = _u_name) AND users.u_pass = _u_pass); 
	
	-- FOUND is set on a SELECT INTO statement, SELECT is not enough
	RETURN FOUND;
END;
$$ 
LANGUAGE 'plpgsql';