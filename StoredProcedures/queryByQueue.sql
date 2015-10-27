DROP FUNCTION IF EXISTS querybyqueue(bigint, bigint, boolean);
CREATE or REPLACE FUNCTION queryByQueue(rid BIGINT, qid BIGINT, doDelete BOOLEAN) RETURNS text AS
$result$
DECLARE
  result text;
  idcount integer;
  mid BIGINT;
BEGIN

SELECT content, id INTO result, mid FROM message WHERE (receiverid = rid OR receiverid = 0) AND queueid = qid ORDER BY times ASC, id LIMIT 1;

IF result IS NULL THEN
  SELECT count(id) INTO idcount FROM queue WHERE id = qid;
  IF idcount = 0 THEN
    result := 'noqueue';
  ELSE
    result := 'empty';
  END IF;
ELSE
  IF doDelete = true THEN
    DELETE FROM message WHERE id = mid;
  END IF;
END IF;

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
