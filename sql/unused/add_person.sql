/* Adds a regisitered user's personal details to the database.

* @param  u_id	    	the allocated user id to each person
* @param  p_first		person's first name
* @param  p_last		person's last name
* @param  email			person's email address
* @param  phone_number  person's phone number (for tours approval and security)
* @param  city_str		person's city of residence
* @param  state_str		the state where the above city is located
* @param  country_str   the country where the above state is located
* @param  languages		preffered language to be used in the app UI
  
* @return VOID		  
*/

DROP FUNCTION IF EXISTS add_person(uuid, VARCHAR(16), VARCHAR(80), VARCHAR(80), VARCHAR(255), VARCHAR(255), VARCHAR(255), INTEGER);
CREATE OR REPLACE FUNCTION add_person(u_id uuid, p_first VARCHAR(80), p_last VARCHAR(80), email VARCHAR(80), phone_number VARCHAR(80), city_str VARCHAR(255), state_str VARCHAR(255), country_str VARCHAR(255), languages INTEGER)
	RETURNS VOID AS 
$$
DECLARE
	_exception_err TEXT;
BEGIN
	BEGIN
		INSERT INTO people(u_id, p_first, p_last, email, phone_number, city_str, state_str, country_str, languages)
		VALUES (u_id, p_first, p_last, email, phone_number, city_str, state_str, country_str, languages);

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