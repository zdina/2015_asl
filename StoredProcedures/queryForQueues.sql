DROP FUNCTION IF EXISTS queryForQueues(bigint);
CREATE or REPLACE FUNCTION queryForQueues(rid BIGINT) RETURNS text AS
$result$
DECLARE
  result text;
  qid BIGINT;
  count INTEGER;
BEGIN

result := '';
count := 0;

FOR qid IN SELECT DISTINCT queueid FROM message WHERE receiverid = rid OR receiverid = 0 LOOP
  result := result||' '||CAST(qid AS text);
  count := count + 1;
END LOOP;

result := count||result;

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
