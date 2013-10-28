package models

import com.mongodb.casbah.Imports._
import persistance._
import play.api.mvc._
import com.mongodb.casbah.Imports._
import play.api.libs.json._

abstract class ModelObj(id: ObjectId) {
  type C <: ModelObj
  type PC = PersistanceCompanion[_]
  
  def myId = id 
  
  val singleton: PC
  
}