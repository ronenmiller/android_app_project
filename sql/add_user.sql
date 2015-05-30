/* Adds a new registered user to the database and assigns that user a unique ID.
*
* @param  u_name	    the desired user name
* @param  u_pass		the user's chosen password
* @param  email			the email address of the user
* @param  phone_number  the phone number of the user (for tours approval and security)
* @param  u_type		the user's type - a guide or a regular user
* @return VOID		  
*/

DROP FUNCTION IF EXISTS add_user(VARCHAR(80), VARCHAR(16), VARCHAR(80), VARCHAR(80), BIT);
CREATE OR REPLACE FUNCTION add_user(u_name VARCHAR(80), u_pass VARCHAR(16), email VARCHAR(80), phone_number VARCHAR(80), u_type BIT)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN
	BEGIN
		INSERT INTO users(u_id, u_name, u_pass, email, phone_number, u_type, u_last_login)
		VALUES (uuid_generate_v1(), u_name, u_pass, email, phone_number, u_type, now());

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