/* TODO: need to remove all user's appearances in other tables as well */
DROP FUNCTION IF EXISTS rm_user(VARCHAR(80), VARCHAR(16));
CREATE OR REPLACE FUNCTION rm_user(p_u_name VARCHAR(80), p_u_pass VARCHAR(16))
	RETURNS VOID AS 
$$
DECLARE
	v_exception_err TEXT;
BEGIN
	BEGIN
		-- A person's details should not be deleted even if the user chooses to delete himself
		ALTER TABLE people DROP CONSTRAINT IF EXISTS people_u_id_fkey;
		ALTER TABLE people DROP CONSTRAINT IF EXISTS people_email_fkey;
		ALTER TABLE people DROP CONSTRAINT IF EXISTS people_phone_number_fkey;

		-- TODO: add users to slots and attach ON DELETE RESTRICT (if a user is signed up to a tour, delete operation should fail)
		DELETE FROM users 
		WHERE (users.u_name = p_u_name AND users.u_pass = p_u_pass);

		ALTER TABLE people ADD CONSTRAINT people_u_id_fkey FOREIGN KEY (u_id) REFERENCES users(u_id) NOT VALID;
		ALTER TABLE people ADD CONSTRAINT people_email_fkey FOREIGN KEY (email) REFERENCES users(email) NOT VALID;
		ALTER TABLE people ADD CONSTRAINT people_phone_number_fkey FOREIGN KEY (phone_number) REFERENCES users(phone_number) NOT VALID;

	EXCEPTION 
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;
	END;
	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';