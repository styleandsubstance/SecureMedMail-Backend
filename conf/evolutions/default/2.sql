# --- !Ups
CREATE Table TransferStatus (
  id serial PRIMARY KEY,
  status text NOT NULL
);

INSERT INTO TransferStatus(status) VALUES ('Initiated');
INSERT INTO TransferStatus(status) VALUES ('Transferring');
INSERT INTO TransferStatus(status) VALUES ('Complete');

ALTER TABLE Download ADD COLUMN transfer_status_id int REFERENCES TransferStatus;
ALTER TABLE Upload ADD COLUMN transfer_status_id int REFERENCES TransferStatus;

UPDATE Download SET transfer_status_id = ( SELECT id FROM TransferStatus where status = 'Complete'  );
UPDATE Upload SET transfer_status_id = ( SELECT id FROM TransferStatus where status = 'Complete'  );

ALTER TABLE Download ALTER COLUMN transfer_status_id SET NOT NULL;
ALTER TABLE Upload ALTER COLUMN transfer_status_id SET NOT NULL;

ALTER TABLE Account ALTER COLUMN organization DROP NOT NULL;
ALTER TABLE Account ALTER COLUMN cc_address_line_2 DROP NOT NULL;



# --- !Downs
ALTER TABLE Download DROP COLUMN transfer_status_id;
DROP TABLE TransferStatus;