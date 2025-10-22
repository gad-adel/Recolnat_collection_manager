INSERT INTO institution(id, code, name, mandatory_description, optional_description, logo_url, partner_type, created_by,
                        created_at, institution_id)
VALUES (1, 'MNHN', 'Muséum National d''Histoire Naturelle', ' mandatory description MNHN', NULL, NULL, 'PARTNER',
        'script', now(), '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO public.collection
(id, type_collection, collection_name_fr, collection_name_en, description_fr, institution_id, description_en,
 fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid, 'h', 'botanique', 'botanique',
        'Herbier de la Société des Lettres de l''Aveyron', 1, '', '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO public.collection_event
(id, no_collection_information, decimal_latitude, decimal_longitude, event_date, event_remarks, location_remarks,
 field_notes, field_number, geodetic_datum, georeference_sources, habitat, interpreted_date, interpreted_depth,
 maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters, minimum_elevation_in_meters,
 recorded_by, sensitive_location, verbatim_locality, interpreted_altitude, municipality, state_province, region, county,
 continent, country_code, country, water_body, island, island_group)
VALUES ('626feab3-f54d-4b94-8239-ad073db4a707'::uuid, true, 0, 0, '25/05/1998-24/01/1999', 'string', NULL, 'string',
        'string', 'string', 'string', 'string', true, true, 0, 0, 0, 0, 'string', true, 'string', true, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection, state,
 fk_geo_id, fk_colevent_id, fk_other_id, collection_code)
VALUES ('359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid, 'Passeriformes', '2022-08-10 17:27:53.309', 'zied', NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid,
        'VALID', NULL, '626feab3-f54d-4b94-8239-ad073db4a707'::uuid, NULL, NULL);

INSERT INTO public.media
(id, contributor, creator, description, license, source, media_url, fk_id_specimen, media_name, is_cover)
VALUES ('91a1bf3f-5cdd-4a9d-860a-c5c1ef22f48b'::uuid, 'media part', 'new media', 'papillon', 'CC-BY-SA-4.0', 'me', NULL,
        '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid, 'papillon.png', false);

INSERT INTO public.media
(id, contributor, creator, description, license, source, media_url, fk_id_specimen, media_name, is_cover)
VALUES ('b5e0c7e6-b1c7-4d9f-8c7b-0f9e8d7c6b5a'::uuid, 'media part', 'new media 1', 'chien', 'CA-BY-SA-4.0', 'me', NULL,
        '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid, 'papillon2.png', true);

INSERT INTO public.media
(id, contributor, creator, description, license, source, media_url, fk_id_specimen, media_name, is_cover)
VALUES ('a4d9b6d5-a0b6-4c8e-7b6a-9e8d7c6b5a4b'::uuid, 'media part', 'new media 2', 'limace', 'CB-BY-SA-4.0', 'me', NULL,
        '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid, 'papillon3.png', false);

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('11a70251-c55a-4242-80f4-fdd96f4973f2'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'true', 'string',
        'string', '359eefe3-901a-4faf-bc3e-6f3fa266a465'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification)
VALUES ('fe03d0b5-63d1-483c-9f0e-9be44b481217'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'scientificName', 'string', 'string', 'string', NULL, NULL, '11a70251-c55a-4242-80f4-fdd96f4973f2'::uuid);

INSERT INTO public.collection_event
(id, no_collection_information, decimal_latitude, decimal_longitude, event_date, event_remarks, location_remarks,
 field_notes, field_number, geodetic_datum, georeference_sources, habitat, interpreted_date, interpreted_depth,
 maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters, minimum_elevation_in_meters,
 recorded_by, sensitive_location, verbatim_locality, interpreted_altitude, municipality, state_province, region, county,
 continent, country_code, country, water_body, island, island_group)
VALUES ('f5fd933b-daf2-465e-b323-0a20635b9ba6'::uuid, true, 0, 0, '25/05/1998-24/01/1999', 'string', NULL, 'string',
        'string', 'string', 'string', 'string', true, true, 0, 0, 0, 0, 'string', true, 'string', true, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);


INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection, state,
 fk_geo_id, fk_colevent_id, fk_other_id, collection_code)
VALUES ('bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940'::uuid, 'Passeriformes', '2022-08-10 17:26:25.184', 'zied', NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '8342cf1d-f202-4c10-9037-2e2406ce7331'::uuid,
        'VALID', NULL, 'f5fd933b-daf2-465e-b323-0a20635b9ba6'::uuid, NULL, NULL);
