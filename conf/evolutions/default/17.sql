# --- !Ups
INSERT INTO TransferStatus(status) VALUES ('Cancelled');


# --- !Downs
DELETE FROM TransferStatus WHERE status = 'Cancelled';