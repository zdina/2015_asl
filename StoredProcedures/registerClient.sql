DROP FUNCTION IF EXISTS registerClient();
CREATE or REPLACE FUNCTION registerClient(cid BIGINT) RETURNS text AS
$result$
DECLARE
  result text;
  qid BIGINT;
BEGIN

INSERT INTO client(id) VALUES(cid) RETURNING id INTO qid;
result := CAST(qid AS text);

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
