package controllers

import play.api.data._
import com.mongodb.casbah.Imports._
import tp_utils.Tryer._
import play.api.libs.json._
import play.api.mvc._

package object controllerhelper {
  
  /*
   * The following workaround is needed by MongoDB's
   *  
	Restrictions on Field Names
    Field names cannot contain dots (i.e. .), dollar signs (i.e. $), or null characters.
   * 
   */
  def fromMongoToView(x: String) = 
    x.replace("%£","$").replace("%§",".")
  def fromViewToMongo(x: String) = 
    x.replace("$", "%£").replace(".","%§")
  
  implicit def fromMappingToVerifiedMongoString(x: Mapping[String]) =
    new  {
	  def convertMongo:  play.api.data.Mapping[String] = {
	    x.transform(
	        x1 => fromViewToMongo(x1), 
	        x2 => fromMongoToView(x2))
	  }
  	}

  implicit def fromMappingToVerifiedId(x: Mapping[String]) =
    new {
	  def verifyId:  play.api.data.Mapping[String] =
	    x.verifying(id => {
	       checko({new ObjectId(id);true})
	    })
  	}
  
  implicit def fromMappingToVerifiedIdOption(x: Mapping[String]) =
    new {
	  def verifyOptId:  play.api.data.Mapping[String] =
	    x.verifying(id => {
	     if (id.trim.equals(""))
	       true
	     else
	       checko({new ObjectId(id);true})
	    })
  	}
  
  implicit def fromMappingToVerifiedJson(x: Mapping[String]) =
    new {
	  def verifyOptJson:  play.api.data.Mapping[String] =
	    x.verifying(data => {
	     if (data.trim.equals(""))
	       true
	     else
	       checko(Json.parse(data)!=JsNull)
	    })
  	}
  
  def checkRemove(implicit request: Request[AnyContent]) = 
		  (for {
            elems <- request.body.asFormUrlEncoded
            values <- elems.get("remove")
            value <- values.headOption
          } yield {
            (checko(value.toBoolean) || checko(value.equals("on")))
          }).getOrElse(false)
  
}