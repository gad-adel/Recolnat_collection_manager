insert into import (id, timestamp, user_name, email, institution_id, status)
values ('00000000-0000-0000-0000-000000000001', '2025-01-01 12:00:00.000000', 'John Doe', 'john.doe@mail.com',
        'd0ee2788-9aa0-4c5b-a596-53c8efc1a573', 'PROCESSED');

insert into specimen(id, collection_code, fk_id_collection, created_at, state, catalog_number)
values ('00000000-0000-0000-0000-000000000001', 'Collection Code 1', '8342cf1d-f202-4c10-9037-2e2406ce7331',
        '2025-01-01 12:00:00.000000', 'VALID', 'UCBL-FSL 15234');

insert into specimen(id, collection_code, fk_id_collection, created_at, state, catalog_number)
values ('00000000-0000-0000-0000-000000000002', 'Collection Code 1', '8342cf1d-f202-4c10-9037-2e2406ce7331',
        '2025-01-01 12:00:00.000000', 'VALID', 'UCBL-FSL 15235');

insert into specimen_update(fk_import_id, fk_specimen_id, mode)
values ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'UPDATE');
insert into specimen_update(fk_import_id, fk_specimen_id, mode)
values ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'CREATED');
