MACHINE Lift

VARIABLES  floor

INVARIANT  floor : 0..100 /* NAT */

INITIALISATION floor :=




OPER1ATIONS

	inc = PRE floor<100 THEN floor := floor + 1 END ;
	dec = PRE floor>0 THEN floor := floor - 1 END;
	open_door = skip;
	close_door = skip

END
