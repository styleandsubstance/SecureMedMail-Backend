# --- !Ups

ALTER TABLE Upload ADD COLUMN description text;
ALTER TABLE AccountMember ADD COLUMN email text;
UPDATE AccountMember am SET email = ( SELECT email FROM account where id = am.account_id);
ALTER TABLE Account DROP COLUMN email;
ALTER TABLE AccountMember ALTER COLUMN email SET NOT NULL;

# --- !Downs
