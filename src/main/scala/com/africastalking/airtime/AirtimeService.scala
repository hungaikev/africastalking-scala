package com.africastalking.airtime

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Accept, RawHeader}
import com.africastalking.core.utils.TServiceConfig
import scala.concurrent.ExecutionContext.Implicits.global
import com.africastalking.core.commons.TService
import spray.json._
import scala.concurrent.Future


object AirtimeService extends TAirtimeService {
  import AirtimeJsonProtocol._

  override def send(airtime: Airtime) : Future[Either[String,AirtimeResponse]] = {
    val response = callEndpoint(airtime, "airtime/send")
    response
  }

  private def callEndpoint(payload: Airtime, endpoint: String): Future[Either[String, AirtimeResponse]] = {
    val url = s"$environmentHost$endpoint"
    val request: HttpRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = url,
      headers = List(RawHeader("apiKey", apiKey),Accept(MediaTypes.`application/json`)),
      entity = {
        val data = Map(
          "username" -> username,
          "recipients" -> payload.toString
        )
        println(data)
        FormData(data).toEntity
      }
    )
    makeRequest(request)
      .map { response =>
        response.responseStatus match {
          case StatusCodes.OK => Right(response.payload.toJson.convertTo[AirtimeResponse])
          case _ => Left(s"Sorry, ${response.payload}")
        }
      }
  }
}

trait TAirtimeService extends TService with TServiceConfig {
  def send(airtime: Airtime) : Future[Either[String,AirtimeResponse]]

  override lazy val environmentHost: String = if(environ.toLowerCase.equals("production")) apiProductionHost else apiSandboxHost
}
