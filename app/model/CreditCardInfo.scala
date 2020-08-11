package model

class CreditCard( 
    var cc_name: String,
    var cc_address_line_1: String,
    var cc_address_line_2: Option[String],
    var cc_city: String,
    var cc_state: String,
    var cc_zipcode: String,
    var cc_number: String,
    var cc_exp_month: String,
    var cc_exp_year: String,
    var cc_security_code: String) {
}


object CreditCard {
  
  def apply(cc_name: String, cc_address_line_1: String, cc_address_line_2: Option[String], cc_city: String, 
      cc_state: String, cc_zipcode: String, cc_number: String, cc_exp_month: String, cc_exp_year: String, cc_security_code: String): CreditCard = {
      new CreditCard( cc_name, cc_address_line_1, cc_address_line_2, cc_city, cc_state, cc_zipcode, cc_number, cc_exp_month, cc_exp_year, cc_security_code)
  }
  
  def unapply(creditCard: CreditCard): Option[(String, String, Option[String], String, String, String, String, String, String, String)]  = {
     Option(creditCard.cc_name, creditCard.cc_address_line_1, creditCard.cc_address_line_2, creditCard.cc_city, 
	      creditCard.cc_state, creditCard.cc_zipcode, creditCard.cc_number, creditCard.cc_exp_month, creditCard.cc_exp_year, creditCard.cc_security_code)
    
  }
}
