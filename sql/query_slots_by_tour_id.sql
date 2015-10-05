DROP FUNCTION IF EXISTS query_slots_by_tour_id(INTEGER);CREATE OR REPLACE FUNCTION query_slots_by_tour_id(_tour_id INTEGER)	RETURNS SETOF view_slots AS $$BEGIN	RETURN QUERY 	SELECT slots.s_id, slots.u_id, users.u_name, users.email, users.u_rating, slots.s_date, slots.s_time, slots.s_capacity	FROM slots, users	WHERE slots.t_id = _tour_id AND slots.u_id = users.u_id AND slots.s_active = B'1'	ORDER BY slots.s_date ASC, slots.s_time ASC;	RETURN;END;$$ LANGUAGE 'plpgsql';