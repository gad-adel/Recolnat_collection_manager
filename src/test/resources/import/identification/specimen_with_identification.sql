insert into specimen(id, collection_code, fk_id_collection, created_at, state, catalog_number)
values ('00000000-0000-0000-0000-000000000001', 'Collection Code 1', '8342cf1d-f202-4c10-9037-2e2406ce7331',
        '2025-01-01 12:00:00.000000', 'VALID', 'UCBL-FSL 15234');

insert into identification (id, fk_id_specimen, current_determination, verbatim_identification)
values ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', true, 'not verbatim')
