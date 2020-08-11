package model

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import org.squeryl._
import model.SquerylEntryPoint._
import java.util.Date

class Plan(var id: Long,
           var name: String,
           var description: String,
           var upload_rate: BigDecimal,
           var download_rate: BigDecimal,
           var charge: BigDecimal,
           var is_default: Boolean,
           var start_date: Timestamp,
           var end_date: Option[Timestamp]) extends KeyedEntity[Long] {

}


object Plan {
  
  def getPlanById(plan_id: Long): Iterable[Plan] = {
    from(Library.plan)(p => where(p.id === plan_id) select(p))
  }
  
  def listAllActivePlans(): Iterable[Plan] = {
    val now = new Timestamp(new Date().getTime())
    from(Library.plan)(p => where((p.end_date isNull) and (p.start_date <= now)) select(p))
  }
}
