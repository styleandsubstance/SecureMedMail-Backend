# --- !Ups
UPDATE Account SET organization = 'Default Organization' WHERE organization is NULL;
ALTER TABLE Account ALTER COLUMN organization SET NOT NULL;
ALTER TABLE AccountMember ADD COLUMN archive_time timestamp;

# --- !Downs