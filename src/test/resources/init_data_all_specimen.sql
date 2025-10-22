ALTER TABLE specimen
    DISABLE TRIGGER specimen_modified_at;

INSERT INTO public.management (id, storage_name)
VALUES ('54124f73-daf8-46a8-b19f-4419882c3834'::uuid, 'Sur l''étagère');
INSERT INTO public.management (id, storage_name)
VALUES ('54134f73-da23-46a8-b19f-441988255834'::uuid, 'Dans le batiment B');
INSERT INTO public.management (id, storage_name)
VALUES ('54134f73-da23-46a8-b19f-441988255835'::uuid, 'Dans le batiment B');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7331', 'h', 1, 'botanique', 'botanique',
        'Herbier de la Société des Lettres de l''Aveyron', '', '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('9a342a92-6fe8-48d3-984e-d1731c051666', 'MNHN', 1, 'Tunicier', 'Tunicates',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO institution(id, code, name, mandatory_description, optional_description, logo_url, partner_type, created_by,
                        created_at, institution_id)
VALUES (1, 'MNHN', 'Muséum National d''Histoire Naturelle', ' mandatory description MNHN', NULL,
        'http://mediaphoto.mnhn.fr/media/1703257596436moFvSbgrT4O1sPbL', 'PARTNER',
        'script', now(), '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO public.collection_event (id, country, continent, event_date, recorded_by)
VALUES ('54134f73-daf8-46a8-b19f-4419882c5123'::uuid, 'China', 'Asia', '2024-07-01', 'Jane Doe');

INSERT INTO public.collection_event (id, country, continent, event_date)
VALUES ('54134f73-daf8-46a8-b19f-4419882c5124'::uuid, 'France', 'Europe', '2025-07-01');

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection,
 state, fk_geo_id, fk_colevent_id, fk_other_id, collection_code, nominative_collection)
VALUES ('bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940'::uuid, 'Passeriformes', '2022-08-10 17:25:25.184', 'zied', 'zied',
        '2022-08-10 15:26:22.696', 'CHE033782', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid, 'VALID', NULL, NULL, NULL, NULL, 'Collection 1');

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection,
 state, fk_geo_id, fk_colevent_id, fk_other_id, collection_code, nominative_collection)
VALUES ('0b106e72-daa1-4942-a50b-7bd1ca9446ac'::uuid, 'Passeriformes', '2022-08-10 17:26:25.185', 'zied', 'zied',
        '2022-07-10 18:26:22.696', 'CHE033673', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid, 'VALID', NULL, '54134f73-daf8-46a8-b19f-4419882c5124', NULL,
        'COLL1', 'Collection 1');

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection,
 state, fk_geo_id, fk_colevent_id, fk_other_id, collection_code, nominative_collection)
VALUES ('359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid, null, '2022-08-10 17:27:53.309', 'zied', 'zied',
        '2022-08-10 18:26:22.696', 'CHE033773', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid, 'VALID', NULL, '54134f73-daf8-46a8-b19f-4419882c5123', NULL,
        'COLL2', 'Collection 1');

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection,
 state, fk_geo_id, fk_colevent_id, fk_other_id, collection_code, nominative_collection, fk_management_id)
VALUES ('9fdca0c7-2712-46a6-aff5-f88fe6999c1e'::uuid, 'Passeriformes', '2022-08-10 19:00:39.648', 'zied', NULL,
        '1900-01-01 00:00:00.000', 'UM-VEY 17', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '9a342a92-6fe8-48d3-984e-d1731c051666'::uuid, 'DRAFT', NULL, NULL, NULL, NULL, 'Collection 2',
        '54134f73-da23-46a8-b19f-441988255835'::uuid);

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection,
 state, fk_geo_id, fk_colevent_id, fk_other_id, collection_code, nominative_collection, fk_management_id)
VALUES ('e7e68a36-c30b-4891-95a0-bdb55ae1e6ee'::uuid, 'Passeriformes', '2022-08-31 15:42:41.970', 'zied', 'zied',
        '2022-09-20 09:21:30.462', 'CHE033772', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Donar-2',
        '9a342a92-6fe8-48d3-984e-d1731c051666'::uuid, 'REVIEW', NULL, NULL, NULL, NULL, 'Collection 2',
        '54134f73-da23-46a8-b19f-441988255834'::uuid);

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection,
 state, fk_geo_id, fk_colevent_id, fk_other_id, collection_code, nominative_collection, fk_management_id)
VALUES ('44074484-9867-476a-94c4-abd1c0e63d35'::uuid, 'Passeriformes', '2022-08-31 18:21:59.302', 'zieddataentry', NULL,
        '1900-01-01 00:00:00.000', 'UMC-IP 335', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '9a342a92-6fe8-48d3-984e-d1731c051666'::uuid, 'DRAFT', NULL, NULL, NULL, NULL, 'Collection 3',
        '54124f73-daf8-46a8-b19f-4419882c3834'::uuid);

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('5e502b52-8000-40ce-9bf7-65dcdee2670d'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', 'bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('8826f8ad-c595-4632-9827-be6823ad3211'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'a name', 'string', 'string', 'string', NULL, NULL,
        '5e502b52-8000-40ce-9bf7-65dcdee2670d'::uuid, 'MASTER');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('5e502b52-8000-40ce-9bf7-65dcdee2670c'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('8826f8ad-c595-4632-9827-be6823ad32ff'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'Scirpus triqueter', 'string', 'string', 'string', NULL, NULL,
        '5e502b52-8000-40ce-9bf7-65dcdee2670c'::uuid, 'MASTER');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('5e502b52-8000-40ce-9bf7-65dcdee2ff80'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', '9fdca0c7-2712-46a6-aff5-f88fe6999c1e'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('8826f8ad-c595-4632-9827-be6823ad5f41'::uuid, 'string', 'string', NULL, NULL, NULL, 'Canidae', 'Canis', NULL,
        'lupus',
        NULL, 'Canis lupus', 'string', 'string', 'string', NULL, NULL, '5e502b52-8000-40ce-9bf7-65dcdee2ff80'::uuid,
        'MASTER');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('152a55bb-bbc0-4f9b-ad33-ec8fd2a2d94b'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', 'e7e68a36-c30b-4891-95a0-bdb55ae1e6ee'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('54134f73-daf8-46a8-b19f-4419882c5836'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'scientificName', 'string', 'string', 'string', NULL, NULL, '152a55bb-bbc0-4f9b-ad33-ec8fd2a2d94b'::uuid,
        'MASTER');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('6491573f-b5ec-4ef7-9686-20c3562736f6'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', '44074484-9867-476a-94c4-abd1c0e63d35'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('c0fce433-2cc7-4843-a6c9-780ac0d5d060'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'scientificName', 'string', 'string', 'string', NULL, NULL, '6491573f-b5ec-4ef7-9686-20c3562736f6'::uuid,
        'MASTER');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('5e502b52-8000-40ce-9bf7-65dcdee2671c'::uuid, true, '2023-10-03', 'string', 'string', 'true', 'string',
        'string', 'string', '0b106e72-daa1-4942-a50b-7bd1ca9446ac'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('54134f73-daf8-46a8-b19f-4419882c5833'::uuid, 'string', 'string', NULL, NULL, NULL, 'Cyperaceae', 'Scirpus',
        NULL, 'triqueter',
        NULL, 'scientificName', 'string', 'string', 'string', NULL, NULL, '5e502b52-8000-40ce-9bf7-65dcdee2671c'::uuid,
        'MASTER');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('5e502b52-8000-40ce-9bf7-65dcdee2672c'::uuid, false, '2023-10-03', 'string', 'string', 'true', 'string',
        'string', 'string', '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification, level_type)
VALUES ('54134f73-daf8-46a8-b19f-4419882c5834'::uuid, 'string', 'string', NULL, NULL, NULL, 'Cyperaceae', 'Scirpus',
        NULL,
        NULL,
        NULL, 'Scirpus triqueter', 'string', 'string', 'string', NULL, NULL,
        '5e502b52-8000-40ce-9bf7-65dcdee2672c'::uuid, 'MASTER');



ALTER TABLE specimen
    ENABLE TRIGGER specimen_modified_at;
