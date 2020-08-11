# --- !Ups
ALTER TABLE Account ADD COLUMN first_name text NOT NULL;

# --- !Downs
ALTER TABLE Account DROP COLUMN first_name;