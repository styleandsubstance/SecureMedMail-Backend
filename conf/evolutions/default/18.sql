# --- !Ups
DROP TABLE plan;

CREATE TABLE Plan (
	id serial PRIMARY KEY,
	name text NOT NULL,
	description text NOT NULL,
	upload_rate money NOT NULL,
    download_rate money NOT NULL,
    charge money NOT NULL,
    is_default boolean NOT NULL,
    start_date date NOT NULL,
    end_date date
);

INSERT INTO Plan VALUES ( nextval('plan_id_seq'), 'A La Carte', 'Pay for uploads/downloads as you use them', '$1.79', '$1.79', '$1.79', true, now(), NULL);
INSERT INTO Plan VALUES ( nextval('plan_id_seq'), '25 for $25', 'Get 25 downloads/uploads for $25', '$1.00', '$1.00', '$1.00', false, now(), NULL);


CREATE TABLE AccountPlanTransition (
	id serial PRIMARY KEY,
	account_id integer REFERENCES Account(id) NOT NULL,
	authorized_by_member_id integer REFERENCES AccountMember(id) NOT NULL,
	from_plan_id integer REFERENCES Plan(id),
	to_plan_id integer REFERENCES Plan(id) NOT NULL,
	change_time timestamp NOT NULL
);

ALTER TABLE Account ADD COLUMN plan_id integer REFERENCES Plan(id);
UPDATE Account SET plan_id = ( SELECT id FROM plan where is_default = true);
ALTER TABLE Account ALTER COLUMN plan_id SET NOT NULL;

ALTER TABLE Account ADD COLUMN auto_renew_plan boolean;
UPDATE Account SET auto_renew_plan = false;
ALTER TABLE Account ALTER COLUMN auto_renew_plan SET NOT NULL;


CREATE TABLE AccountTransactionType (
	id serial PRIMARY KEY,
	name text NOT NULL
);


INSERT INTO AccountTransactionType VALUES ( nextval('accounttransactiontype_id_seq'), 'Plan');
INSERT INTO AccountTransactionType VALUES ( nextval('accounttransactiontype_id_seq'), 'Upload');
INSERT INTO AccountTransactionType VALUES ( nextval('accounttransactiontype_id_seq'), 'Download');

CREATE TABLE AccountTransactionClass (
	id serial PRIMARY KEY,
	name text NOT NULL
);

INSERT INTO AccountTransactionClass VALUES ( nextval('accounttransactiontype_id_seq'), 'Billing');
INSERT INTO AccountTransactionClass VALUES ( nextval('accounttransactiontype_id_seq'), 'Credit');


CREATE TABLE AccountTransaction (
	id serial PRIMARY KEY,
	account_id integer REFERENCES Account(id) NOT NULL,
	transaction_time timestamp NOT NULL,
    account_transaction_type_id integer REFERENCES AccountTransactionType(id) NOT NULL,
    account_transaction_class_id integer REFERENCES AccountTransactionClass(id) NOT NULL,
    amount money NOT NULL
);


CREATE TABLE BillingAccountTransaction (
	id serial PRIMARY KEY,
	account_transaction_id integer REFERENCES AccountTransaction(id) NOT NULL,
	credit_card_api_response text NOT NULL
);


CREATE TABLE CreditAccountTransaction (
	id serial PRIMARY KEY,
	account_transaction_id integer REFERENCES AccountTransaction(id) NOT NULL,
	from_balance money NOT NULL,
	to_balance money NOT NULL
);

CREATE TABLE PlanAccountTransaction (
     id serial PRIMARY KEY,
     account_transaction_id integer REFERENCES AccountTransaction(id) NOT NULL,
     plan_id integer REFERENCES Plan(id) NOT NULL
);


CREATE TABLE DownloadAccountTransaction (
     id serial PRIMARY KEY,
     account_transaction_id integer REFERENCES AccountTransaction(id) NOT NULL,
     download_id integer REFERENCES Download(id) NOT NULL
);


CREATE TABLE UploadAccountTransaction (
     id serial PRIMARY KEY,
     account_transaction_id integer REFERENCES AccountTransaction(id),
     upload_id integer REFERENCES Upload(id) NOT NULL
);


# --- !Downs
DROP TABLE UploadAccountTransaction;
DROP TABLE DownloadAccountTransaction;
DROP TABLE CreditAccountTransaction;
DROP TABLE BillingAccountTransaction;
DROP TABLE AccountTransaction;
DROP TABLE AccountTransactionClass;
DROP TABLE AccountTransactionType;
DROP TABLE AccountPlanTransition;
ALTER TABLE Account DROP COLUMN plan_id;
DROP TABLE plan;