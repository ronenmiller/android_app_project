CREATE OR REPLACE FUNCTION validate_username(p_u_name VARCHAR(80))
	RETURNS BOOLEAN AS 
$$
DECLARE
	temp users;
BEGIN
	SELECT * 
	INTO temp
	FROM users 
	WHERE users.u_name = p_u_name; 
	
	-- FOUND is set on a SELECT INTO statement, SELECT is not enough
	RETURN FOUND;
END;
$$ 
LANGUAGE 'plpgsql';