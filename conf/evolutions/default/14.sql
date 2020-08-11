# --- !Ups
INSERT INTO TransferStatus(status) VALUES ('Deleting');


# --- !Downs
DELETE FROM TransferStatus WHERE status = 'Deleting';