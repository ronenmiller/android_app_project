CREATE OR REPLACE FUNCTION add_rating_2_guide(p_u_id uuid, p_g_id uuid, p_g_rating SMALLINT)
	RETURNS VOID AS 
$$
DECLARE
	v_g_name		VARCHAR(80);
	v_g_type		BIT;
	v_exception_err TEXT;
BEGIN
	SELECT u_type
	INTO v_g_type
	FROM users
	WHERE u_id = p_g_id;
	
	IF v_g_type != B'1' THEN
		RAISE EXCEPTION 'ERROR: user % is not a guide', v_g_name;
	END IF;
	IF p_u_id = p_g_id THEN
		RAISE EXCEPTION 'ERROR: a guide cannot rate himeself/herself';
	END IF;
	
	BEGIN
		INSERT INTO rate2guide(u_id, g_id, g_rating)
		VALUES (p_u_id, p_g_id, p_g_rating);

	EXCEPTION 
		WHEN foreign_key_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE foreign_key_violation USING MESSAGE = v_exception_err;
		WHEN unique_violation THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE unique_violation USING MESSAGE = v_exception_err;
		WHEN OTHERS THEN
			GET STACKED DIAGNOSTICS v_exception_err = MESSAGE_TEXT;
			RAISE EXCEPTION USING MESSAGE = v_exception_err;

	END;
	
	UPDATE users
	SET u_rating = (SELECT DISTINCT avg(g_rating) OVER (PARTITION BY g_id) FROM rate2guide WHERE g_id = p_g_id)
	WHERE u_id = p_g_id;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';