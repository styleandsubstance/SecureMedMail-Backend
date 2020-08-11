# --- !Ups
ALTER TABLE Download ADD COLUMN upload_id integer REFERENCES Upload(id) NOT NULL;
ALTER TABLE Upload ADD UNIQUE(guid);

# --- !Downs
ALTER TABLE Upload DROP CONSTRAINT upload_guid_key;
ALTER TABLE Download DROP COLUMN upload_id;
