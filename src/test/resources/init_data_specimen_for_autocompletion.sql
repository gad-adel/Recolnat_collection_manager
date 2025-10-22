INSERT INTO collection (id, type_collection, institution_id, collection_name_fr,
                        collection_name_en, description_fr, description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7331', 'h', 1, 'botanique', 'botanique',
        'Herbier de la Société des Lettres de l''Aveyron', '', '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr,
                        collection_name_en, description_fr, description_en, fk_institution_id)
VALUES ('9a342a92-6fe8-48d3-984e-d1731c051666', 'IT', 1, 'Tunicier', 'Tunicates',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        'Tunicates collection (IT) of the Muséum national d''Histoire naturelle (MNHN - Paris)',
        '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO geological_context (id, age_absolute, bed, earliest_age_or_lowest_stage,
                                earliest_eon_or_lowest_eonothem, earliest_epoch_or_lowest_series,
                                earliest_era_or_lowest_erathem, earliest_period_or_lowest_system, formation,
                                highest_biostratigraphic_zone, latest_age_or_highest_stage,
                                latest_eon_or_highest_eonothem, latest_epoch_or_highest_series,
                                latest_era_or_highest_erathem, latest_period_or_highest_system,
                                lowest_biostratigraphic_zone, member, other_lithostratigraphic_terms, range,
                                verbatim_epoch, geo_group)
VALUES ('1e471187-8d58-4919-9332-895ffbefd22f', 'string', 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string');

INSERT INTO collection_event (id, no_collection_information, decimal_latitude, decimal_longitude, event_date,
                              event_remarks, location_remarks, field_notes, field_number, geodetic_datum,
                              georeference_sources, habitat, interpreted_date, interpreted_depth,
                              maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters,
                              minimum_elevation_in_meters, recorded_by, sensitive_location, verbatim_locality,
                              interpreted_altitude, municipality, state_province, region, county, continent,
                              country_code, country, water_body, island, island_group)
VALUES ('50be8c08-ae37-42a2-8d16-8e75a27c294f', true, 0, 0, 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string', true, true, 0, 0, 0, 0, 'Paul, M.', true, 'string', true, 'string', 'string', 'string',
        'string', 'Europe', 'FRA', 'France', 'string', 'string', 'string');

INSERT INTO collection_event (id, no_collection_information, decimal_latitude, decimal_longitude, event_date,
                              event_remarks, location_remarks, field_notes, field_number, geodetic_datum,
                              georeference_sources, habitat, interpreted_date, interpreted_depth,
                              maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters,
                              minimum_elevation_in_meters, recorded_by, sensitive_location, verbatim_locality,
                              interpreted_altitude, municipality, state_province, region, county, continent,
                              country_code, country, water_body, island, island_group)
VALUES ('a2be8c08-ae37-42a2-8d16-8e75a27c294f', true, 0, 0, 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string', true, true, 0, 0, 0, 0, 'Pauline, G.', true, 'string', true, 'string', 'string', 'string',
        'string', 'Asia', 'CHN', 'China', 'string', 'string', 'string');


INSERT INTO collection_event (id, no_collection_information, decimal_latitude, decimal_longitude, event_date,
                              event_remarks, location_remarks, field_notes, field_number, geodetic_datum,
                              georeference_sources, habitat, interpreted_date, interpreted_depth,
                              maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters,
                              minimum_elevation_in_meters, recorded_by, sensitive_location, verbatim_locality,
                              interpreted_altitude, municipality, state_province, region, county, continent,
                              country_code, country, water_body, island, island_group)
VALUES ('b2be8c08-ae37-42a2-8d16-8e75a27c294f', true, 0, 0, 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string', true, true, 0, 0, 0, 0, 'Paulette, K.', true, 'string', true, 'string', 'string', 'string',
        'string', 'Africa', 'MRC', 'Congo', 'string', 'string', 'string');

INSERT INTO collection_event (id, no_collection_information, decimal_latitude, decimal_longitude, event_date,
                              event_remarks, location_remarks, field_notes, field_number, geodetic_datum,
                              georeference_sources, habitat, interpreted_date, interpreted_depth,
                              maximum_elevation_in_meters, maximum_depth_in_meters, minimum_depth_in_meters,
                              minimum_elevation_in_meters, recorded_by, sensitive_location, verbatim_locality,
                              interpreted_altitude, municipality, state_province, region, county, continent,
                              country_code, country, water_body, island, island_group)
VALUES ('c2be8c08-ae37-42a2-8d16-8e75a27c294f', true, 0, 0, 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string', true, true, 0, 0, 0, 0, 'George, W, B.', true, 'string', true, 'string', 'string', 'string',
        'string', 'America', 'CAN', 'Canada', 'string', 'string', 'string');



INSERT INTO other (id, computerization_program, financial_aid, link_bold, link_ger_bank, link_other, remarks)
VALUES ('7ba58534-5197-47f2-a8a8-5db4154b15d4', 'string', 'string', 'string', 'string', 'string', 'string');


INSERT INTO public.specimen (id, legacy_id, created_at, created_by, modified_by, modified_at,
                             catalog_number, record_number, basis_of_record, preparations, individual_count, sex,
                             life_stage, occurrence_remarks, legal_status, donor, collection_code, fk_id_collection,
                             state,
                             fk_geo_id, fk_colevent_id, fk_other_id)
VALUES ('70029074-fde4-4f85-b3fe-9e25e7bfd9ea', 'string', '2022-07-16 07:09:02.812912', 'respinsttest1', null, null,
        'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'SLA',
        '8342cf1d-f202-4c10-9037-2e2406ce7331', 'DRAFT', '1e471187-8d58-4919-9332-895ffbefd22f',
        '50be8c08-ae37-42a2-8d16-8e75a27c294f', '7ba58534-5197-47f2-a8a8-5db4154b15d4');

INSERT INTO public.identification (id, current_determination, date_identified, error_message, identification_remarks,
                                   identification_verification_status, identified_byid, type_status,
                                   verbatim_identification, fk_id_specimen)
VALUES ('83930a4a-521f-45c3-96d7-5abb55db24ad', true, '2022-07-13', 'string', 'string', 'true', 'string', 'string',
        'string', '70029074-fde4-4f85-b3fe-9e25e7bfd9ea');

INSERT INTO public.taxon (id, kingdom, phylum, taxon_class, taxon_order, sub_order, family, genus, sub_genus,
                          specific_epithet, infraspecific_epithet, scientific_name, scientific_name_authorship,
                          taxon_remarks, vernacular_name, referential_name, referential_version, fk_id_identification)
VALUES ('66f098eb-87db-43bd-9120-4293ad67ea9f', 'string', 'string', null, null, null, null, null, null, null, null,
        'string', 'string', 'string', 'string', null, null, '83930a4a-521f-45c3-96d7-5abb55db24ad');

INSERT INTO literature (id, identifier, authors, book_title, citation, date, description, editors, keywords,
                        language, number, page_number, pages, publication_place, publisher, remarks, review,
                        title, url, volume, fk_id_specimen)
VALUES ('99457981-f96d-4875-80fe-09921543175e', 'string', 'string', 'string', 'string', '2022-07-13', 'string',
        'string', 'string', 'str', 'string', 'string', 'string', 'string', 'string', 'string', 'string', 'string',
        'string', 'string', '70029074-fde4-4f85-b3fe-9e25e7bfd9ea');

INSERT INTO media (id, contributor, creator, description, license, source, media_url, fk_id_specimen, media_name)
VALUES ('0bc8f18a-10dd-4af6-86bf-aeb2af61fe09', 'string', 'string', 'string', 'string', 'string', 'string',
        '70029074-fde4-4f85-b3fe-9e25e7bfd9ea', 'string');


