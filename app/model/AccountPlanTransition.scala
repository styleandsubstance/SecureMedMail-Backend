package model

import org.squeryl.KeyedEntity
import java.sql.Timestamp

class AccountPlanTransition(
	var id: Long,
	var account_id: Long,
	var authorized_by_member_id: Long,
	var from_plan_id: Option[Long],
	var to_plan_id: Long,
	var change_time: Timestamp

	) extends KeyedEntity[Long] {

}