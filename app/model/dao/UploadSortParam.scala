package model.dao

object UploadSortParam extends Enumeration {
  type UploadSortParam = Value
  
  val guid = Value("guid")
  val start_time = Value("start_time")
  val end_time = Value("end_time")
  val description = Value("description")
  val filename = Value("filename")
  val confirmed_download_count = Value("confirmed_download_count")
}