CREATE OR REPLACE FUNCTION change_modified_at()
  RETURNS TRIGGER
  LANGUAGE PLPGSQL
  AS
$$
BEGIN
	NEW.data_change_ts := NOW();
	RETURN NEW;
END;
$$;
CREATE TRIGGER modified_at_updates
    BEFORE INSERT OR UPDATE
    ON article
    FOR EACH ROW
    EXECUTE PROCEDURE change_modified_at();
