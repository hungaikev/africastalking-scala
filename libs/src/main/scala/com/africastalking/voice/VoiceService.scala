package com.africastalking.voice

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import com.africastalking.core.commons.TService
import com.africastalking.core.utils.TServiceConfig

import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VoiceService extends TVoiceService {

  import VoiceJsonProtocol._


  /**
    *
    * @param to is the list of callers
    * @param from is the callee
    * @return
    */
  override def call(to: List[String], from: String): Future[Either[String, CallResponse]] = {
    val callPayload = CallPayload(
      username = username,
      to       = to.mkString(","),
      from     = from
    )

    Marshal(callPayload)
      .to[RequestEntity]
      .flatMap(entity => callEndpoint(entity, "call", payload => payload.parseJson.convertTo[CallResponse]))
  }

  /**
    *
    * @param phoneNumber
    * @return
    */
  override def fetchQueuedCalls(phoneNumber: String): Future[Either[String, QueuedCallsResponse]] = {
    val queuedCallsPayload = QueuedCallsPayload(
      username     = username,
      phoneNumbers = phoneNumber
    )

    Marshal(queuedCallsPayload)
      .to[RequestEntity]
      .flatMap(entity => callEndpoint(entity, "queueStatus", payload => payload.parseJson.convertTo[QueuedCallsResponse]))
  }

  /**
    *
    * @param phoneNumber
    * @param url
    * @return
    */

  override def uploadMediaFile(phoneNumber: String, url: String): Future[Either[String, String]] = {
    val mediaUploadPayload = MediaUploadPayload(
      username    = username,
      phoneNumber = phoneNumber,
      url         = url
    )

    Marshal(mediaUploadPayload)
      .to[RequestEntity]
      .flatMap(entity => callEndpoint(entity, "mediaUpload", payload => payload))
  }
}

trait TVoiceService extends TService with TServiceConfig {
  def call(to: List[String], from: String): Future[Either[String, CallResponse]]

  def fetchQueuedCalls(phoneNumber: String): Future[Either[String, QueuedCallsResponse]]

  def uploadMediaFile(phoneNumber: String, url: String): Future[Either[String, String]]

  override lazy val environmentHost: String = if(environ.toLowerCase.equals("production")) voiceProductionHost else voiceSandboxHost
}
