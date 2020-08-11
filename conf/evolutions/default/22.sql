# --- !Ups
ALTER TABLE AccountMember ADD COLUMN creation_time timestamp;
UPDATE AccountMember SET creation_time = now();
ALTER TABLE AccountMember ALTER COLUMN creation_time SET NOT NULL;

# --- !Downs

