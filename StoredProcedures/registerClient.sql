DROP FUNCTION IF EXISTS registerClient();
CREATE or REPLACE FUNCTION registerClient() RETURNS text AS
$result$
DECLARE
  result text;
  cid BIGINT;
BEGIN

cid := NEXTVAL('client_id_seq');

INSERT INTO client(id) VALUES(cid);

result := CAST(cid AS text);

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
