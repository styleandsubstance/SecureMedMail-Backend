# --- !Ups

CREATE FUNCTION get_deletion_date_for_upload(desired_upload_id integer) RETURNS timestamp AS $$
DECLARE
	deletion_days_file_property_value text;;
    deletion_date_interval interval;;
    deletion_days_file_property_id integer := 6;;
    default_days_for_deletion text := '180';;
	deletion_timestamp timestamp;;
BEGIN
	SELECT file_property_value into deletion_days_file_property_value 
		FROM uploadfilepropertyvalue 
		WHERE upload_id = desired_upload_id  AND
			file_property_id = deletion_days_file_property_id;;
    IF deletion_days_file_property_value IS NULL OR deletion_days_file_property_value = '' THEN
		deletion_date_interval := (default_days_for_deletion  || ' days')::INTERVAL;;
	ELSE
		deletion_date_interval := (deletion_days_file_property_value || ' days')::INTERVAL;;
    END IF;;


    SELECT INTO deletion_timestamp upload.end_time + deletion_date_interval
		FROM Upload
		WHERE id = desired_upload_id;;

	RETURN deletion_timestamp;;
END;;
$$ LANGUAGE plpgsql;

# --- !Downs

DROP FUNCTION get_deletion_date_for_upload(integer);