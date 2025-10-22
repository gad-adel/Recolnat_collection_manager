INSERT INTO "import" (id, "timestamp", user_name, email, institution_id, status)
VALUES ('c29b7295-8ea0-4a87-9714-68164ddf7abc'::uuid, '2025-03-12 17:19:43.139', 'admintest1',
        'admintest12@recolnat.com', 'd0ee2788-9aa0-4c5b-a596-53c8efc1a573'::uuid, 'PENDING');

INSERT INTO import_file (id, fk_import_id, file_type, file_name, line_count, "mode")
VALUES ('b11de00f-0fbd-496e-ad50-c01f5f322000'::uuid, 'c29b7295-8ea0-4a87-9714-68164ddf7abc'::uuid, 'SPECIMEN',
        'specimen_full_fields.csv', 1, 'IGNORE');

INSERT INTO import_file (id, fk_import_id, file_type, file_name, line_count, "mode")
VALUES ('b11de00f-0fbd-496e-ad50-c01f5f322001'::uuid, 'c29b7295-8ea0-4a87-9714-68164ddf7abc'::uuid, 'IDENTIFICATION',
        'identification_full_fields.csv', 1, null);

INSERT INTO import_file (id, fk_import_id, file_type, file_name, line_count, "mode")
VALUES ('b11de00f-0fbd-496e-ad50-c01f5f322002'::uuid, 'c29b7295-8ea0-4a87-9714-68164ddf7abc'::uuid, 'LITERATURE',
        'publication_full_fields.csv', 1, null);
