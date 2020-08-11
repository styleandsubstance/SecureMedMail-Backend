# --- !Ups
ALTER TABLE Upload ADD COLUMN remote_ip_address text;
UPDATE Upload SET remote_ip_address = '127.0.0.1';
ALTER TABLE Upload ALTER COLUMN remote_ip_address SET NOT NULL;


# --- !Downs
ALTER TABLE Upload DROP COLUMN remote_ip_address;
