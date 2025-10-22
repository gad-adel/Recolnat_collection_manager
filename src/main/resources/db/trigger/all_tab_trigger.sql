CREATE OR REPLACE FUNCTION change_modified_at()
  RETURNS TRIGGER 
  LANGUAGE PLPGSQL
  AS
$$
BEGIN
	NEW.data_change_ts := NOW();
	RETURN NEW;
END;
$$  
;

CREATE TRIGGER collection_modified_at
  BEFORE INSERT OR UPDATE
  ON collection
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();

CREATE TRIGGER geological_context_modified_at
  BEFORE INSERT OR UPDATE
  ON geological_context
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();

CREATE TRIGGER collection_event_modified_at
  BEFORE INSERT OR UPDATE
  ON collection_event
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();

CREATE TRIGGER identification_modified_at
  BEFORE INSERT OR UPDATE
  ON identification
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();

CREATE TRIGGER literature_modified_at
  BEFORE INSERT OR UPDATE
  ON literature
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();

CREATE TRIGGER other_modified_at
  BEFORE INSERT OR UPDATE
  ON other
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();


CREATE TRIGGER taxon_modified_at
  BEFORE INSERT OR UPDATE
  ON taxon
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();
  
CREATE TRIGGER article_modified_at
  BEFORE INSERT OR UPDATE
  ON article
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();
  
CREATE TRIGGER institution_modified_at
  BEFORE INSERT OR UPDATE
  ON institution
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();
  
-- delimiter ;
-- rollback DROP TRIGGER IF EXISTS taxon_modified_at;


  