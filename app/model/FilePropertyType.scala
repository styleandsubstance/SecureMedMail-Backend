package model

object FilePropertyType extends Enumeration {
  type file_property_type = Value

  val Text = Value(1, "Text")
  val Numeric = Value(2, "Numeric")
  val Checkbox = Value(3, "Boolean")
  
}

