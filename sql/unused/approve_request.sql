CREATE OR REPLACE FUNCTION approve_request(p_u_id uuid, p_ts_id INTEGER, p_t_num_participants INTEGER)
	RETURNS VOID AS 
$$
BEGIN
	UPDATE slots
	SET ts_vacant = ts_vacant - p_t_num_participants
	WHERE ts_id = p_ts_id;

	UPDATE requests
	SET r_active = B'1'
	WHERE u_id = p_u_id
	AND ts_id = p_ts_id;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';