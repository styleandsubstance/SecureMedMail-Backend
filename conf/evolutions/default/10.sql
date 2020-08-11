# --- !Ups
ALTER TABLE Download ADD COLUMN remote_ip_address text;
UPDATE Download SET remote_ip_address = '127.0.0.1';
ALTER TABLE Download ALTER COLUMN remote_ip_address SET NOT NULL;


# --- !Downs
