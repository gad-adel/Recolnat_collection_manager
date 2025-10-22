CREATE OR REPLACE FUNCTION change_spec_modified_at()
  RETURNS TRIGGER 
  LANGUAGE PLPGSQL
  AS
$$
BEGIN
    IF NEW.state = 'VALID' THEN
	    NEW.data_change_ts := NOW();
	END IF;
	NEW.modified_at := NOW();
	RETURN NEW;
END;
$$  
;
CREATE TRIGGER specimen_modified_at
    BEFORE INSERT OR UPDATE
     ON specimen
     FOR EACH ROW
     EXECUTE PROCEDURE change_spec_modified_at();
  
-- delimiter ;
-- rollback DROP TRIGGER IF EXISTS specimen_modified_at;
