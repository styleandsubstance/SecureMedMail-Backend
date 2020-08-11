# --- !Ups
ALTER TABLE Download ADD COLUMN streaming_success boolean;
ALTER TABLE Download ADD COLUMN confirmed_download boolean;
ALTER TABLE Download ADD COLUMN guid text UNIQUE;
ALTER TABLE Download ADD COLUMN streaming_end_time timestamp;
UPDATE Download SET streaming_success = false;
UPDATE Download SET confirmed_download = false;
UPDATE Download SET guid = id;
ALTER TABLE Download ALTER COLUMN streaming_success SET NOT NULL;
ALTER TABLE Download ALTER COLUMN confirmed_download SET NOT NULL;
ALTER TABLE Download ALTER COLUMN guid SET NOT NULL;


# --- !Downs