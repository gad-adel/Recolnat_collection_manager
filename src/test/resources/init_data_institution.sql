DELETE FROM institution;
INSERT INTO institution(id, code,name, mandatory_description, optional_description, logo_url, partner_type, created_by,created_at, institution_id, url, specimens_count)
VALUES (1,'MNHN','Muséum National d''Histoire Naturelle', ' mandatory description MNHN', NULL, NULL, 'PARTNER', 'script', now(),'50f4978a-da62-4fde-8f38-5003bd43ff64', 'http://test1.fr', 123456789);
INSERT INTO institution(id, code,name, mandatory_description, optional_description, logo_url, partner_type, created_by,created_at, institution_id, url, specimens_count)
VALUES (2,'CJBN','Conservatoire et jardins botaniques de Nancy', ' mandatory description CJBN', NULL, NULL, 'DATA_PROVIDER', 'script', now(),'21210632-5d32-42ba-af49-10142142ddf7', 'http://test2.fr', 123456788);
INSERT INTO institution(id, code,name, mandatory_description, optional_description, logo_url, partner_type, created_by,created_at, institution_id, modified_by, modified_at, data_change_ts, url, specimens_count)
VALUES (3,'SNSNMC','Société nationale des sciences naturelles et mathématiques de Cherbourg(CHE)', 'mandatory description SNSNMC', NULL, NULL, 'DATA_PROVIDER', 'script', now(),'1d5e16d0-4564-4ef4-93ab-ec434a23ae75', 'script', now(), now(), NULL, NULL);
