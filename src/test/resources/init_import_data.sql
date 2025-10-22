INSERT INTO institution(id, code, name, mandatory_description, optional_description, logo_url, partner_type, created_by,
                        created_at, institution_id, data_change_ts)
VALUES (1, 'UCBL', 'Université Claude Bernard Lyon 1', '', NULL, NULL, 'PARTNER', 'script', now(),
        'd0ee2788-9aa0-4c5b-a596-53c8efc1a573', NULL);


INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7331', 'h', 1, 'UCB Lyon 1', null,
        null, null, 'd0ee2788-9aa0-4c5b-a596-53c8efc1a573');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7332', 'h', 1, 'UCB Lyon 2', null,
        null, null, 'd0ee2788-9aa0-4c5b-a596-53c8efc1a573');

INSERT INTO collection (id, type_collection, institution_id, collection_name_fr, collection_name_en, description_fr,
                        description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7333', 'h', 1, 'Collection générale', null,
        null, null, 'd0ee2788-9aa0-4c5b-a596-53c8efc1a573');
