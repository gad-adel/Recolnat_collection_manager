INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7331', 'h', 1, 'botanique', 'botanique',
        'Herbier de la Société des Lettres de l''Aveyron', '', '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('9a342a92-6fe8-48d3-984e-d1731c051666', 'IT', 1, 'Tunicier', 'Tunicates',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('e82e315f-c4a0-4a3d-942c-19c26151a1b1', 'CCJBN', 2, 'NCJBN french', 'NCJBN en',
        'NCJBN collection (CJBN) of the Conservatoire et jardins botaniques de Nancy',
        'TNCJBN collection (CJBN) of the Conservatoire et jardins botaniques de Nancy',
        'afea161b-70bb-4cad-aa04-d20698f5721c');

INSERT INTO specimen(id, created_at, created_by, modified_by, modified_at, fk_id_collection, state)
values ('9c6ab9ea-d049-47b5-972c-18e7831bdd4e', '2022-03-25 12:22:30.965616', 'created_by', null, null,
        '8342cf1d-f202-4c10-9037-2e2406ce7331', 'VALID');

SELECT collection.id::text, collection.type_collection, specimen.state
FROM COLLECTION collection
         INNER JOIN SPECIMEN specimen ON collection.id = specimen.fk_id_collection;

SELECT collection.id::text,
       collection.type_collection,
       specimen.state,
       specimen.id::text as specimenId
FROM COLLECTION collection
         INNER JOIN SPECIMEN specimen ON collection.id = specimen.fk_id_collection
where specimen.state = 'VALID';

INSERT INTO public.specimen
(id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection, state,
 fk_geo_id, fk_colevent_id, fk_other_id, collection_code)
VALUES ('359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid, '2022-08-10 17:27:53.309', 'zied', NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid,
        'VALID', NULL, NULL, NULL, NULL);

INSERT INTO public.media
(id, contributor, creator, description, license, source, media_url, fk_id_specimen, media_name)
VALUES ('91a1bf3f-5cdd-4a9d-860a-c5c1ef22f48b'::uuid, 'media part', 'new media', 'papillon', 'CC-BY-SA-4.0', 'me',
        'https://mediaphoto.mnhn.fr/media/1660148777364m90qnta9kyzgZr6p', '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid,
        'papillon.png');

INSERT INTO institution(id, code, name, mandatory_description, optional_description, logo_url, partner_type, created_by,
                        created_at, institution_id)
VALUES (1, 'MNHN', 'Muséum National d''Histoire Naturelle', ' mandatory description MNHN', NULL, NULL, 'PARTNER',
        'script', now(), '50f4978a-da62-4fde-8f38-5003bd43ff64');
