DROP FUNCTION IF EXISTS sendMessage(sid BIGINT, rid BIGINT, qid BIGINT);
CREATE or REPLACE FUNCTION sendMessage(sid BIGINT, rid BIGINT, qid BIGINT, c TEXT) RETURNS text AS
$result$
DECLARE
  result text;
  rcheckid BIGINT;
  qcheckid BIGINT;
BEGIN

SELECT id INTO rcheckid FROM client WHERE id = rid;

IF rcheckid IS NULL THEN
  result := 'noreceiver';
ELSE
  SELECT id INTO qcheckid FROM queue WHERE id = qid;
  IF qcheckid IS NULL THEN
    result := 'noqueue';
  ELSE
    INSERT INTO message(senderid, receiverid, content, queueid) VALUES(sid, rid, c, qid);
    result := 'sent';
  END IF;
END IF;

RETURN result;
END;
$result$
LANGUAGE 'plpgsql';
