# --- !Ups
ALTER TABLE AccountMember ADD COLUMN first_name text;
ALTER TABLE AccountMember ADD COLUMN last_name text;
ALTER TABLE AccountMember ADD COLUMN created_by_account_member_id int REFERENCES AccountMember(id);
ALTER TABLE AccountMember ADD COLUMN is_admin boolean;
ALTER TABLE AccountMember ADD COLUMN downloads_allowed boolean;
ALTER TABLE AccountMember ADD COLUMN uploads_allowed boolean;


UPDATE AccountMember SET first_name = '';
UPDATE AccountMember SET last_name = '';
UPDATE AccountMember SET is_admin = false;
UPDATE AccountMember SET is_admin = true where id in (SELECT admin_id FROM account);
UPDATE AccountMember SET downloads_allowed = true;
UPDATE AccountMember SET uploads_allowed = true;


ALTER TABLE AccountMember ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE AccountMember ALTER COLUMN last_name SET NOT NULL;
ALTER TABLE AccountMember ALTER COLUMN is_admin SET NOT NULL;
ALTER TABLE AccountMember ALTER COLUMN downloads_allowed SET NOT NULL;
ALTER TABLE AccountMember ALTER COLUMN uploads_allowed SET NOT NULL;


ALTER TABLE Account DROP COLUMN first_name;
ALTER TABLE Account DROP COLUMN last_name;
ALTER TABLE Account DROP COLUMN admin_id;

# --- !Downs