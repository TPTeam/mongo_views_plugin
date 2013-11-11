package controllers

import play.api.mvc._
import play.api.libs.concurrent._
import models.persistance._
import com.mongodb.casbah.Imports._
import com.novus.salat.dao.SalatMongoCursor
import play.api.libs.json._
import tp_utils.Tryer._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import Json._
import play.api.data._
import models._
import controllerhelper._

trait CRUDer[C <: ModelObj] extends SingletonDefiner[C] {
  self: Controller =>
    
	def form: Form[C]
	
	def formTemplate(formb: Form[C])(implicit request: RequestHeader): play.api.templates.Html

	def viewCreateForm(implicit request: RequestHeader): (Form[C] => Result) = 
	  {formb: Form[C] => Ok(formTemplate(formb))}
	def viewEditForm(implicit request: RequestHeader): (Form[C] => Result) = 
	  {formb: Form[C] => Ok(formTemplate(formb))}
	def viewBadForm(implicit request: RequestHeader): (Form[C] => Result) = 
	  {formb: Form[C] => BadRequest(formTemplate(formb))}
	
    def createForm: Form[C] = form
    def editForm(elem: C): Form[C] = form.fill(elem)
   
	
   def create = Action  { 
      implicit request =>
      	viewCreateForm.apply(createForm)
  }

  def edit(id: String): EssentialAction = Action { 
    implicit request =>
      (obj.findOneById(new ObjectId(id))) match {
      	case Some(elem) =>
      	  viewEditForm.apply(editForm(elem))
      	case _ =>
      	  Ok("Cannot edit undefined object")
  	}
  }
  
  def submit =
    Action { 
    implicit request => {
      val data = 
      request.body.asFormUrlEncoded match {
        case Some(content) =>
          content
        case _ =>
          Map()
      }
      
      form.bindFromRequest.fold(
          badForm =>
            form.bind(
                badForm.data ++ 
                badForm.errors.seq.map(err => (err.key -> ""))
                ).fold(
                definitiveBad => viewBadForm.apply(badForm),
                elem => success(elem)),
          elem => success(elem)
        )
    }
  }
  
  def success(elem: C)(implicit request: Request[AnyContent]) = 
		  if (!checkRemove)
              Ok("Ok")
          else {
              obj.delete(elem.myId)
              Ok("Removed")
          }

}