package util

/**
 * Created by sdoshi on 8/9/2014.
 */
class ValidatedAction(
  val action: () => Boolean,
  val errorMessage: String) {

   def execute() : Boolean = {
     action();
   }

}
