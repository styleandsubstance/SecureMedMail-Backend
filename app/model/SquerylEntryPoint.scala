package model

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast._
import java.sql.Timestamp

/**
 * Created by sdoshi on 8/29/2014.
 */
object SquerylEntryPoint extends PrimitiveTypeMode {

  def get_deletion_date_for_upload(f: TypedExpression[Long, TLong])(implicit a: TypedExpressionFactory[Timestamp, TTimestamp]) =
    a.convert(new FunctionNode("get_deletion_date_for_upload", Seq(f)))
}
