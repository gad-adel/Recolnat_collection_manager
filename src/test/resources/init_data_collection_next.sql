INSERT INTO public.collection (id, type_collection, collection_code, collection_name_fr, collection_name_en,
                               description_fr, description_en, fk_institution_id)
VALUES ('8342cf1d-f202-4c10-9037-2e2406ce7338', 'h', 'SLA', 'botanique', 'botanique',
        'Herbier de la Société des Lettres de l''Aveyron', '', '50f4978a-da62-4fde-8f38-5003bd43ff64');

INSERT INTO specimen(id, legacy_id, created_at, created_by, modified_by, modified_at, fk_id_collection, state)
values ('9c6ab9ea-d049-47b5-972c-18e7831bdd4f', 'legacy_id', '2022-03-25 12:22:30.965616', 'created_by', null, null,
        '8342cf1d-f202-4c10-9037-2e2406ce7338', 'VALID');
