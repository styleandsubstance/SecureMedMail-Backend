# Users schema
 
# --- !Ups

CREATE TABLE Plan (
  id serial PRIMARY KEY,
  name text NOT NULL,
  upload_rate money NOT NULL,
  download_rate money NOT NULL
);
  

CREATE TABLE AccountMember (
  id serial PRIMARY KEY,
  username text UNIQUE NOT NULL,
  password text NOT NULL
);


CREATE TABLE Account (
  id serial PRIMARY KEY,
  admin_id integer References AccountMember(id) ON DELETE CASCADE,
  plan_id integer References Plan(id) NOT NULL,
  last_name text NOT NULL,
  organization text NOT NULL,
  address_line_1 text NOT NULL,
  address_line_2 text,
  city text NOT NULL,
  state text NOT NULL,
  zipcode text NOT NULL,
  email text NOT NULL,
  cc_name text NOT NULL,
  cc_address_line_1 text NOT NULL,
  cc_address_line_2 text NOT NULL,
  cc_city text NOT NULL,
  cc_state text NOT NULL,
  cc_zipcode text NOT NULL,
  cc_number text NOT NULL,
  cc_exp_month text NOT NULL,
  cc_exp_year text NOT NULL,
  cc_security_code text NOT NULL
);

ALTER TABLE AccountMember ADD COLUMN account_id integer REFERENCES Account(id) NOT NULL;

CREATE TABLE Upload (
  id serial PRIMARY KEY,
  start_time timestamp NOT NULL,
  end_time timestamp,
  guid text NOT NULL,
  file_type text NOT NULL,
  size bigint NOT NULL,
  uploaded_by_member_id integer References AccountMember(id) ON DELETE CASCADE

);

CREATE TABLE Download (
  id serial PRIMARY KEY,
  start_time timestamp NOT NULL,
  end_time timestamp,
  downloaded_by_member_id integer References AccountMember(id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE Download;
DROP TABLE Upload;
DROP TABLE AccountMember;
DROP TABLE Account;
DROP TABLE Plan;