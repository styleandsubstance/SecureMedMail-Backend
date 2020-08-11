# --- !Ups
ALTER TABLE Upload ADD COLUMN confirmed_download_count int;
UPDATE Upload SET confirmed_download_count = ( SELECT COUNT(*) from Download where upload_id = Upload.id);
ALTER TABLE Upload ALTER COLUMN confirmed_download_count SET NOT NULL;


# --- !Downs