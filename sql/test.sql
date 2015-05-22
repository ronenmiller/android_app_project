CREATE OR REPLACE FUNCTION test()
	RETURNS SETOF view_tours_by_city AS 
$$
DECLARE 
	v_uid uuid;
BEGIN
    SELECT users.u_id INTO v_uid FROM users WHERE users.u_name = 'ronen';
    INSERT INTO tours VALUES (v_uid ,30,7275,30,'Amazing tour',NULL,1,9.7,'{comment 1}');
	INSERT INTO tours VALUES (v_uid ,35,7275,45,'Fantastic tour',NULL,1,9.2,'{comment 2}');
	RETURN QUERY SELECT * FROM search_tour_by_city('Los Angeles','California','United States');
	RETURN;
END;
$$
LANGUAGE 'plpgsql';

SELECT * FROM test();