# --- !Ups
INSERT INTO TransferStatus(status) VALUES ('Deleted');


# --- !Downs
DELETE FROM TransferStatus WHERE status = 'Deleted';