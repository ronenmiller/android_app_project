DROP FUNCTION IF EXISTS validate_unique_email(CHARACTER VARYING);
CREATE OR REPLACE FUNCTION validate_unique_email(_email VARCHAR(80))
	RETURNS BOOLEAN AS 
$$
DECLARE
	temp users;
BEGIN

	SELECT * 
	INTO temp
	FROM users 
	WHERE users.email = _email; 
	
	-- FOUND is set on a SELECT INTO statement, SELECT is not enough
	RETURN FOUND;
	
END;
$$ 
LANGUAGE 'plpgsql';
