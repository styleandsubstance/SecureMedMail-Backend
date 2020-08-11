# --- !Ups
ALTER TABLE Upload ADD COLUMN filename text;
UPDATE Upload set filename='' || guid || '.txt';
ALTER TABLE Upload ALTER COLUMN filename SET NOT NULL;
ALTER TABLE Upload DROP COLUMN file_type;

# --- !Downs
