DROP FUNCTION IF EXISTS createQueue();
CREATE or REPLACE FUNCTION createQueue() RETURNS text AS
$result$
DECLARE
  result text;
  qid BIGINT;
BEGIN

qid := NEXTVAL('queue_id_seq');

INSERT INTO queue(id) VALUES(qid);

result := CAST(qid AS text);

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
