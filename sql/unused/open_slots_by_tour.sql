CREATE OR REPLACE FUNCTION open_slots_by_tour(p_t_id INTEGER)
	RETURNS SETOF slots AS 
$$
BEGIN
	RETURN QUERY 
	SELECT *
	FROM slots
	WHERE slots.t_id = p_t_id
	AND slots.ts_active = B'1'
	HAVING slots.ts_vacant > 0;

	RETURN;
END;
$$ 
LANGUAGE 'plpgsql';