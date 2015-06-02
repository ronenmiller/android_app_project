CREATE OR REPLACE FUNCTION test()
	RETURNS VOID AS 
$$
DECLARE
	v_u_id_r uuid;
	v_u_id_l uuid;
	v_u_id_o uuid;
BEGIN
	BEGIN
		PERFORM add_user('ronen','999mmfkd','a@gmail.com','05454948484',B'1');
		PERFORM add_user('liron','999mmfkd','la@gmail.com','05454947484',B'1');
		PERFORM add_user('ori','999mmfkd','or@gmail.com','05453947484',B'0');
		SELECT users.u_id INTO v_u_id_r FROM users WHERE users.u_name = 'ronen';
		SELECT users.u_id INTO v_u_id_l FROM users WHERE users.u_name = 'liron';
		SELECT users.u_id INTO v_u_id_o FROM users WHERE users.u_name = 'ori';
		PERFORM add_tour(v_u_id_r,'Los Angeles','California','United States',40,'','{}',1);
		PERFORM add_tour(v_u_id_r,'Los Angeles','California','United States',30,'','{}',3);
		
		PERFORM add_slot(1, current_date, current_time, 0, 20);
		PERFORM add_slot(1, current_date, current_time, 8, 16);
		PERFORM add_slot(1, current_date, current_time, 14, 8);
		PERFORM add_slot(2, current_date, current_time, 14, 8);

		PERFORM add_rating_2_tour(v_u_id_o,1,9::SMALLINT);
		PERFORM add_rating_2_tour(v_u_id_l,1,8::SMALLINT);

		PERFORM add_rating_2_guide(v_u_id_l,v_u_id_r,9::SMALLINT);
		PERFORM add_rating_2_guide(v_u_id_o,v_u_id_r,8::SMALLINT);

	/*EXCEPTION -- useful for the test to supress errors
		WHEN OTHERS THEN
			-- do nothing
	*/
	END;

	RETURN;

END;
$$
LANGUAGE 'plpgsql';

--SELECT test();
--SELECT * FROM search_tour_by_city('Los Angeles','California','United States');
--SELECT * FROM find_cityid_by_name('Los Angeles','California','United States');
--SELECT validate_username('ronen');
--SELECT * FROM search_tour_by_guide('ronen');
--SELECT * FROM open_slots_by_tour(1);
--SELECT * FROM rate2tour;
--SELECT * FROM tours;
--SELECT * FROM users; 
SELECT * FROM query_city_by_name('New');