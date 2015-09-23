DROP FUNCTION IF EXISTS validate_credentials(CHARACTER VARYING, CHARACTER VARYING);
CREATE OR REPLACE FUNCTION validate_credentials(_u_name VARCHAR(80), _u_pass VARCHAR(80))
	RETURNS TABLE(u_id uuid, u_type BIT) AS 
$$
BEGIN
    RETURN QUERY
	SELECT users.u_id, users.u_type
	FROM users 
	WHERE ((users.u_name = _u_name OR users.email = _u_name) AND users.u_pass = _u_pass);
	
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';