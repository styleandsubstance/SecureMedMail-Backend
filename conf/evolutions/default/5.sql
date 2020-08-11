# --- !Ups
CREATE TABLE FilePropertyType (
  id serial PRIMARY KEY,
  file_property_type text NOT NULL
);

CREATE TABLE FileProperty (
  id serial PRIMARY KEY,
  name text NOT NULL,
  default_value text,
  description text,
  type_id int REFERENCES FilePropertyType(id) NOT NULL
);

CREATE TABLE FilePropertiesProfile (
  id serial PRIMARY KEY,
  name text NOT NULL,
  description text,
  account_member_id int REFERENCES AccountMember(id) NOT NULL,
  is_default_profile boolean NOT NULL,
  unique(account_member_id, name)
);

CREATE TABLE FilePropertiesProfileFilePropertyValue (
  id serial PRIMARY KEY,
  profile_id int REFERENCES FilePropertiesProfile(id) NOT NULL,
  file_property_id int REFERENCES FileProperty(id) NOT NULL,
  file_property_value text
);

CREATE TABLE UploadFilePropertyValue (
  id serial PRIMARY KEY,
  upload_id int REFERENCES Upload(id) NOT NULL,
  file_property_id int REFERENCES FileProperty(id) NOT NULL,
  file_property_value text
);

INSERT INTO FilePropertyType (id, file_property_type) VALUES ( nextval('filepropertytype_id_seq'), 'Text');
INSERT INTO FilePropertyType (id, file_property_type) VALUES ( nextval('filepropertytype_id_seq'), 'Numeric');
INSERT INTO FilePropertyType (id, file_property_type) VALUES ( nextval('filepropertytype_id_seq'), 'Boolean');

INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'DeleteAfterDownload', 'false', 'Should file be deleted after successful download', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Boolean'));
INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'MustBeAuthenticated', 'true', 'Does the person attempting to download a file need to be authenticated?', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Boolean'));
INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'MustBeAccountMember', 'false', 'Should this file be restricted to other members of this account?', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Boolean'));
INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'BillDownloadToUploader', 'false', 'Bill the uploading account member whenever this file is downloaded.', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Boolean'));
INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'DeleteAfterNumberOfDownloads', NULL, 'Delete the remote file after a set number of downloads', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Numeric'));
INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'DeleteAfterNumberOfDays', '30', 'Delete the remote file after a set number of days', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Numeric'));
INSERT INTO FileProperty (id, name, default_value, description, type_id) VALUES ( nextval('fileproperty_id_seq'), 'NotifyUploaderAfterDownload', 'false', 'Should the user who uploaed the file be notified via e-mail that someone has successfully downloaded the file?', (SELECT id FROM FilePropertyType WHERE file_property_type = 'Boolean'));

ALTER TABLE Account DROP COLUMN plan_id;

# --- !Downs
DROP TABLE UploadFilePropertyValue;
DROP TABLE FilePropertiesProfileValue;
DROP TABLE FilePropertiesProfile;
DROP TABLE FileProperty;
DROP TABLE FilePropertyType;