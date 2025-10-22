INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7331', 'h', 1, 'botanique', 'botanique',
        'Herbier de la Société des Lettres de l''Aveyron', '', '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO public.specimen
(id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection, state,
 fk_geo_id, fk_colevent_id, fk_other_id, collection_code)
VALUES ('bf25ed41-f55c-41ec-bcb8-064268420e78'::uuid, '2022-08-17 15:26:11.527', 'zied', NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid,
        'VALID', NULL, NULL, NULL, NULL);

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('5e502b52-8000-40ce-9bf7-65dcdee260c7'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', 'bf25ed41-f55c-41ec-bcb8-064268420e78'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification)
VALUES ('8826f8ad-c595-4632-9827-be6823ad3ff2'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'scientificName', 'string', 'string', 'string', NULL, NULL, '5e502b52-8000-40ce-9bf7-65dcdee260c7'::uuid);

