DROP FUNCTION IF EXISTS validate_unique_username(CHARACTER VARYING);
CREATE OR REPLACE FUNCTION validate_unique_username(_u_name VARCHAR(80))
	RETURNS BOOLEAN AS 
$$
DECLARE
	temp users;
BEGIN

	SELECT * 
	INTO temp
	FROM users 
	WHERE users.u_name = _u_name; 
	
	-- FOUND is set on a SELECT INTO statement, SELECT is not enough
	RETURN FOUND;
	
END;
$$ 
LANGUAGE 'plpgsql';
