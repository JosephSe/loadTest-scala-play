package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.libs.json.Json
import java.io._
import java.util.UUID

import model.AsyncServerEntity

import scala.util.Random

//import io.swagger.annotations.{ApiOperation, ApiResponses, ApiResponse, Api}
import model.{ResponseData, MongoEntity, XmlFile, ZipFile}
import play.api.mvc.{Result, Request, Action, Controller}
import service.{FileService, MessageBroadcaster, ResponseService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.Logger._
import org.apache.commons.io.FileUtils
/**
  * Created by Joseph Sebastian on 26/01/2016.
  */
//@Api(value = "/booking", description = "Async booking/search requests")
class AsyncController @Inject()(val responseService: ResponseService, messageBroadcaster: MessageBroadcaster, fileService: FileService) extends Controller {

  val logger = Logger("AsyncController")
  val printer = new xml.PrettyPrinter(10, 4)
  val random  = Random

  //  @ApiOperation(nickname = "/booking", value = "Async booking/search requests", notes = "Returns 200", httpMethod = "POST")
  //  @ApiOperation(nickname = "/booking", value = "Async booking/search requests", notes = "Returns 200", response = classOf[Results], httpMethod = "POST")
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "Created"),
  //    new ApiResponse(code = 400, message = "Bad request")))
  def booking = Action.async { implicit request =>
    val fileName = request.getQueryString("fileName")
    val contentType = request.headers.get("Content-Type").get
    logger.debug(s"$contentType recieved :${fileName.get}")
    if (fileName.isDefined) {
      contentType match {
        case "application/zip" =>
          val content = request.body.asRaw.get.asBytes()
          val cFile = request.body.asRaw.get.asFile
//          val contentFile = new FileInputStream(request.body.asRaw.get.asFile)
          logger.debug(s"filePath:${cFile.getAbsolutePath}")
          println(s"filePath:${cFile.getAbsolutePath}")
          val file = new File(s"/tmp/${fileName.get+random.nextInt}.zip")
//          val file = new FileOutputStream(s"/tmp/$fileName.zip")
          FileUtils.copyFile(request.body.asRaw.get.asFile, file)
          var c = 0
//          save(new ZipFile(fileName.get, content.get.), "zip")
          save(new ZipFile(fileName.get, "".getBytes()), "zip")
        case _ =>
          val xmlContent = request.body.asXml.getOrElse(request.body.asText.get)
          save(new XmlFile(fileName.get, xmlContent.toString()), "xml")
      }
    } else Future {
      BadRequest
    }
  }

  private def save(entity: AsyncServerEntity, typ: String): Future[Result] = {
    responseService.saveFile(entity).map {
      case Right(mEntity) =>
        broadcast(mEntity)
        Created(mEntity.id.get.toString)
      case Left(left) => BadRequest(left)
    }
  }

  private def broadcast(file: AsyncServerEntity) = {
    file match {
      case xml: XmlFile => messageBroadcaster.broadcast(Json.toJson(new ResponseData(xml).withNew.withoutData))
      case zip: ZipFile => messageBroadcaster.broadcast(Json.toJson(new ResponseData(zip).withNew.withoutData))
    }
  }

}
