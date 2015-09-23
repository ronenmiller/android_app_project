CREATE OR REPLACE FUNCTION add_rating_2_tour(p_u_id uuid, p_t_id INTEGER, p_t_rating SMALLINT)
	RETURNS VOID AS 
$$
DECLARE
	v_u_id			uuid;
	v_exception_err TEXT;
BEGIN
	SELECT u_id
	INTO v_u_id
	FROM tours
	WHERE t_id = p_t_id;
	
	IF v_u_id = p_u_id THEN
		RAISE EXCEPTION 'ERROR: a guide cannot rate his own tour';	
	END IF;
	
	BEGIN
		INSERT INTO rate2tour(u_id, t_id, t_rating)
		VALUES (p_u_id, p_t_id, p_t_rating);

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
	
	UPDATE tours
	SET t_rating = (SELECT DISTINCT avg(t_rating) OVER (PARTITION BY t_id) FROM rate2tour WHERE t_id = p_t_id)
	WHERE t_id = p_t_id;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';