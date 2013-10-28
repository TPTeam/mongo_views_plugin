package models.persistance

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import models.mongoContext._
import models._
import models.ModelObj

abstract class PersistanceCompanion[T <: ModelObj](implicit m: Manifest[T],ctx: Context) 
extends ModelCompanion[T, ObjectId]
{
      
  val collectionName: String
  
  lazy val dao = new SalatDAO[T, ObjectId](collection = mongoCollection(collectionName)) {}
  
  def create(obj: T) = {
     save(obj)
     obj
  }
  
  def update(id: ObjectId, obj: T): Unit =
    update(
        MongoDBObject("_id" -> id), toDBObject(obj), false, false, new WriteConcern)
  
  //def delete(obj: T) =
  //   dao.removeById(obj.id)
  def delete(id: ObjectId): Boolean =
    (findOneById(id)) match {
    case Some(obj) => dao.removeById(id); true
    case _ => false}
    
  def number: Long = 
	dao.count()
	
  val defaultSort = "id"
  
  class SimpleOrdering(x: Int) {
    def typ = x
  }
  case class Asc() extends SimpleOrdering(1)
  case class Desc() extends SimpleOrdering(-1)
  
  def getTablePage(start: Int, quantity: Int, sortItem: String =  "id", so: SimpleOrdering = new Asc) = 
    findAll().sort(orderBy = MongoDBObject(sortItem -> so.typ))
      .skip(start)
      .limit({if ((start+quantity) > number) (number - start)
    		  else quantity}.toInt)
      .toList
  
  def findOneByIdString(id: String): Option[T] =
    try {super.findOneById(new ObjectId(id))} catch {case _ : Throwable => None}
    
  implicit def retrieveInstance(ref: Reference[T]): T =
    findOneById(ref.id).get
}