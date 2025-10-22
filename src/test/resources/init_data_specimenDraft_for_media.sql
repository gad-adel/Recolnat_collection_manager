-- Media Draft with media

INSERT INTO public.collection
(id, type_collection, collection_name_fr, collection_name_en, description_fr, institution_id, description_en,
 fk_institution_id)
VALUES ('9a342a92-6fe8-48d3-984e-d1731c051666'::uuid, 'Z', 'Tunicier', 'Tunicates',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)', 1,
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO public.collection_event
(id, no_collection_information, decimal_latitude, decimal_longitude, event_date, event_remarks, location_remarks,
 field_notes, field_number, geodetic_datum, georeference_sources, habitat, interpreted_date, interpreted_depth,
 maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters, minimum_elevation_in_meters,
 recorded_by, sensitive_location, verbatim_locality, interpreted_altitude, municipality, state_province, region, county,
 continent, country_code, country, water_body, island, island_group)
VALUES ('9776d4b8-8c2c-4a69-a840-5dfdf39133bb'::uuid, true, 0, 0, '25/05/1998-24/01/1999', 'string', NULL, 'string',
        'string', 'string', 'string', 'string', true, true, 0, 0, 0, 0, 'string', true, 'string', true, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);


INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection, state,
 fk_geo_id, fk_colevent_id, fk_other_id, collection_code)
VALUES ('9fdca0c7-2712-46a6-aff5-f88fe6999c1e'::uuid, 'Passeriformes', '2022-08-10 19:00:39.648', 'zied', NULL,
        '1900-01-01 00:00:00.000', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '9a342a92-6fe8-48d3-984e-d1731c051666'::uuid, 'DRAFT', NULL, '9776d4b8-8c2c-4a69-a840-5dfdf39133bb'::uuid, NULL,
        NULL);


INSERT INTO public.media
(id, contributor, creator, description, license, "source", media_url, fk_id_specimen, media_name)
VALUES ('e16b8c89-a0b8-4a13-b112-7b2f7e5aebba'::uuid, 'media part', 'new media', 'lucane-cerf', 'CC-BY-SA-4.0', 'me',
        NULL, '9fdca0c7-2712-46a6-aff5-f88fe6999c1e'::uuid, 'lucane-cerf.jpg');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('1e69ba9b-aefa-48fa-b0bc-a28384b68457'::uuid, true, '2022-05-03', 'string', 'string', 'false', 'string',
        'string', 'string', '9fdca0c7-2712-46a6-aff5-f88fe6999c1e'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, "family", genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification)
VALUES ('19e074b3-eb0a-4ac3-9e71-6ea309d399e9'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, ' ', 'string', 'string', 'string', NULL, NULL, '1e69ba9b-aefa-48fa-b0bc-a28384b68457'::uuid);

-- Media Reviewed with media
INSERT INTO public.collection_event
(id, no_collection_information, decimal_latitude, decimal_longitude, event_date, event_remarks, location_remarks,
 field_notes, field_number, geodetic_datum, georeference_sources, habitat, interpreted_date, interpreted_depth,
 maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters, minimum_elevation_in_meters,
 recorded_by, sensitive_location, verbatim_locality, interpreted_altitude, municipality, state_province, region, county,
 continent, country_code, country, water_body, island, island_group)
VALUES ('48722c5d-9b30-4416-b3b3-d8a88b5168da'::uuid, true, 0, 0, '25/05/1998-24/01/1999', 'string', NULL, 'string',
        'string', 'string', 'string', 'string', true, true, 0, 0, 0, 0, 'string', true, 'string', true, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO public.specimen
(id, legacy_id, created_at, created_by, modified_by, modified_at, catalog_number, record_number, basis_of_record,
 preparations, individual_count, sex, life_stage, occurrence_remarks, legal_status, donor, fk_id_collection, state,
 fk_geo_id, fk_colevent_id, fk_other_id, collection_code)
VALUES ('f98e0678-8c27-4867-bbfc-12a09dfbce3d'::uuid, 'Passeriformes', '2022-08-16 12:54:57.196', 'ziedcollection',
        'ziedcollection', '2022-08-16 15:14:56.846', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        '9a342a92-6fe8-48d3-984e-d1731c051666'::uuid, 'REVIEW', NULL, '48722c5d-9b30-4416-b3b3-d8a88b5168da'::uuid,
        NULL, NULL);

INSERT INTO public.media
(id, contributor, creator, description, license, "source", media_url, fk_id_specimen, media_name)
VALUES ('453af161-2b70-4477-b69a-f47f00e51ccb'::uuid, 'media part', 'new media', 'media part', 'CC-BY-SA-4.0', 'me',
        NULL, 'f98e0678-8c27-4867-bbfc-12a09dfbce3d'::uuid, 'Insecte_1.jpg');

INSERT INTO public.identification
(id, current_determination, date_identified, error_message, identification_remarks, identification_verification_status,
 identified_byid, type_status, verbatim_identification, fk_id_specimen)
VALUES ('07866f4f-ab9e-44a3-b810-c65e2f69e1e3'::uuid, true, '2022-05-03', 'string', 'string', 'true', 'string',
        'string', 'string', 'f98e0678-8c27-4867-bbfc-12a09dfbce3d'::uuid);

INSERT INTO public.taxon
(id, kingdom, phylum, taxon_class, taxon_order, sub_order, "family", genus, sub_genus, specific_epithet,
 infraspecific_epithet, scientific_name, scientific_name_authorship, taxon_remarks, vernacular_name, referential_name,
 referential_version, fk_id_identification)
VALUES ('c16224a5-14b5-4a1f-96a2-70bdc2e22215'::uuid, 'string', 'string', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        NULL, 'scientificName', 'string', 'string', 'string', NULL, NULL, '07866f4f-ab9e-44a3-b810-c65e2f69e1e3'::uuid);

