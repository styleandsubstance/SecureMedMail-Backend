# --- !Ups
ALTER TABLE Upload ADD COLUMN deletion_attempts int;
UPDATE Upload SET deletion_attempts = 0;
ALTER TABLE Upload ALTER COLUMN deletion_attempts SET NOT NULL;


# --- !Downs
ALTER TABLE Upload DROP COLUMN deletion_attempts;