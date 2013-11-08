package models

import com.mongodb.casbah.Imports.ObjectId
import models.persistance._
import com.novus.salat._

case class Reference[C <: ModelObj](id: ObjectId) {

}

trait ReverseRefPersistanceCompanion[T <: ModelObj, R <: ModelObj] { 

  def referenceChanged: ((Option[Reference[R]], Reference[T]) => Unit)
  
}

trait DirectRefPersistanceCompanion[T <: ModelObj, R <: ModelObj] {
  
	def removeFrom(toBeRemoved: List[Reference[R]], from: List[T]): Unit
	
	def addTo(toBeAdded: List[Reference[R]], to: T): Unit
  
}

package object RefObj {
  
  implicit def makeReference[C <: ModelObj](x: ModelObj): Reference[C] = 
    Reference[C](x.myId)
      
}