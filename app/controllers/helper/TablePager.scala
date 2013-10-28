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
import models._

trait TablePager[C <: ModelObj] extends SingletonDefiner[C] {
  self: Controller =>
  
    
	/*
	 * To implement or to
	 * override
	 */
    //val singleton: models.persistance.PersistanceCompanion[C]
    def elemValues(elem :C): Seq[String]
    val elemsToDisplay = Seq("name","description")
    val defaultDisplayLenth: Long = 10
	val defaultSortBy = "name"
    
    //lazy val obj = singleton
	def sortBy(implicit params: Map[String,Seq[String]]) =
		  tryo(elemsToDisplay(sortCol.toInt)).getOrElse(defaultSortBy)
	
    def iTotalRecords = obj.number
    def filter(implicit params: Map[String,Seq[String]]) = 
      	params.get("sSearch").getOrElse(Seq("")).head
    def pageSize(implicit params: Map[String,Seq[String]]) =
      	Integer.valueOf(params.get("iDisplayLength")
    				.getOrElse(Seq(""+defaultDisplayLenth)).head)
    def page(implicit params: Map[String,Seq[String]]) =
        Integer.valueOf(params.get("iDisplayStart")
    			.getOrElse(Seq("0")).head) / pageSize
    def order(implicit params: Map[String,Seq[String]]) = 
      if (params.get("sSortDir_0").getOrElse(Seq("asc")).head.compareTo("desc")==0)
    				-1 else 1
    def sortCol(implicit params: Map[String,Seq[String]]) =
      params.get("sSortCol_0").getOrElse(Seq("asc")).head
    
    def sEcho(implicit params: Map[String,Seq[String]]) =
      Integer.valueOf(params.get("sEcho").getOrElse(Seq("0")).head)
    
    def start(implicit params: Map[String,Seq[String]]) =
      (page)*pageSize 
    
    def filt(implicit params: Map[String,Seq[String]]) =
      ("(?i).*"+filter+".*")
      
	def iTotalDisplayRecords(implicit params: Map[String,Seq[String]]) =
    	  objsWithQuery.count
    	
    def objsWithQuery(implicit params: Map[String,Seq[String]]) =
      obj.find(MongoDBObject(
          "$or" -> elemsToDisplay.map(elem => MongoDBObject(elem -> filt.r))))
    
    def actualPage(implicit params: Map[String,Seq[String]]) =
      objsWithQuery
            .sort(orderBy = MongoDBObject(sortBy -> order))
            .skip(start)
            .limit(Integer.valueOf(
              {
                if ((start + pageSize) > iTotalRecords) (pageSize - start) toString
                else pageSize toString
              }))
  
    def table = Action { implicit request ⇒
    val promiseOfResult =
      Akka.future {
        implicit val params = request.queryString
        
        val isInit =
        for {
          ps <- params.get("init")
          p <-  ps.headOption
        } yield {
          p.equals("true")
        }
        
        (isInit) match {
          case Some(true) =>
            JsObject(
            		Seq(
            				"headers" -> toJson(elemsToDisplay),
            				"paginationType" -> toJson("cubePagination"),
            				"sDom" -> toJson("<\"dataTableHeader\"fp<\"tableToolbar\">><\"dataTableBody\"rt><\"dataTableFooter\">")
            			)
            )
            
          case _ =>
            /**
             * 	Construct the JSON to return
             */
            import Json._
            import tp_utils.Jsoner._
                
            JsObject(
            		Seq(
            				"sEcho" -> toJsVal(sEcho),
            				"iTotalRecords" -> toJsVal(iTotalRecords),
            				"iTotalDisplayRecords" -> toJsVal(iTotalDisplayRecords),
            				"aaData" -> 
            				JsArray(
            						actualPage.map(elem => 
            			  JsObject(
            			      elemValues(elem).zipWithIndex.map(e =>
            			      		e._2.toString -> toJson(e._1)
                            ))
                       ).toSeq)   
          )
        )
        }
      }

    Async {
      promiseOfResult.map(res ⇒
        Ok(res))
    }
  }
}