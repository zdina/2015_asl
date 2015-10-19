DROP FUNCTION IF EXISTS removeQueue(BIGINT);
CREATE or REPLACE FUNCTION removeQueue(qid BIGINT) RETURNS text AS
$result$
DECLARE
  result text;
  mid BIGINT;
  checkid BIGINT;
BEGIN

SELECT id INTO mid FROM message WHERE queueid = qid LIMIT 1;

IF mid IS NULL THEN
  SELECT id INTO checkid FROM queue WHERE id = qid;
  IF checkid IS NULL THEN
    result := 'noqueue';
  ELSE
    DELETE FROM queue WHERE id = qid;
    result := 'deleted';
  END IF;
ELSE
  result := 'inuse';
END IF;

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
