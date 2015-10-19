DROP FUNCTION IF EXISTS createQueue();
CREATE or REPLACE FUNCTION createQueue() RETURNS text AS
$result$
DECLARE
  result text;
  qid BIGINT;
BEGIN

INSERT INTO queue(name) VALUES('a') RETURNING id INTO qid;
result := CAST(qid AS text);

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
