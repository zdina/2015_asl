DROP FUNCTION IF EXISTS querybysender(bigint, bigint, boolean);
CREATE or REPLACE FUNCTION queryBySender(rid BIGINT, sid BIGINT, doDelete BOOLEAN) RETURNS text AS
$result$
DECLARE
  result text;
  idcount integer;
  mid BIGINT;
BEGIN

SELECT content, id INTO result, mid FROM message WHERE receiverid = rid AND senderid = sid ORDER BY times ASC, id LIMIT 1;

IF result IS NULL THEN
  SELECT count(id) INTO idcount FROM client WHERE id = sid;
  IF idcount = 0 THEN
    result := 'nosender';
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
