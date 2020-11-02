package com.higherkingpud.activity.stream.consumer

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

import akka.actor.typed.ActorSystem
import com.amazonaws.encryptionsdk.AwsCrypto
import com.amazonaws.encryptionsdk.jce.JceMasterKey
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.{AWSKMS, AWSKMSClientBuilder}
import com.amazonaws.util.{Base64, IOUtils}
import com.higherkingpud.activity.stream.consumer.json.{ActivityEventJson, AuditLogJson, MonitoringRecordJson}
import io.circe.generic.auto._
import io.circe.parser._
import javax.crypto.spec.SecretKeySpec

import scala.jdk.CollectionConverters.MapHasAsJava

object ActivityDecoder {
  private val kms: AWSKMS = AWSKMSClientBuilder.defaultClient()

  case class DecodeError(message: String, cause: Throwable = null) extends Throwable(message, cause)

  class DecryptError(message: String)    extends DecodeError(s"DecryptError($message)")
  class DecompressError(message: String) extends DecodeError(s"DecompressError($message)")
  class JsonParseError(message: String)  extends DecodeError(s"JsonParseError($message)")

  private def decrypt(decoded: Array[Byte], decodedDataKey: Array[Byte])(
      implicit
      crypto: AwsCrypto): Either[DecryptError, Array[Byte]] = {
    // Create a JCE master key provider using the random key and an AES-GCM encryption algorithm
    val masterKey = JceMasterKey.getInstance(
      new SecretKeySpec(decodedDataKey, "AES"),
      "BC",
      "DataKey",
      "AES/GCM/NoPadding"
    )
    try {
      val decryptingStream = crypto.createDecryptingStream(masterKey, new ByteArrayInputStream(decoded))
      val out              = new ByteArrayOutputStream
      try {
        IOUtils.copy(decryptingStream, out)
        Right(out.toByteArray)
      } catch {
        case e: Throwable => Left(new DecryptError(e.getMessage))
      } finally {
        if (decryptingStream != null) decryptingStream.close()
        if (out != null) out.close()
      }
    }
  }

  def decodeActivityEvents(
      record: MonitoringRecordJson,
      dbcResourceId: String
  )(implicit crypto: AwsCrypto, system: ActorSystem[Nothing]): Either[DecodeError, Seq[ActivityEventJson]] = {
    val decoded    = Base64.decode(record.databaseActivityEvents)
    val decodedKey = Base64.decode(record.key)

    val decryptRequest = new DecryptRequest()
      .withCiphertextBlob(ByteBuffer.wrap(decodedKey))
      .withEncryptionContext(Map("aws:rds:dbc-id" -> dbcResourceId).asJava)
    val decryptResult = kms.decrypt(decryptRequest)
    for {
      decrypted <- decrypt(decoded, getByteArray(decryptResult.getPlaintext))
      // GZip なので展開する
      decompressed <- decompress(decrypted)
      _            <- Right(system.log.debug(decompressed))
      json         <- parse(decompressed).fold(e => Left(new JsonParseError(e.getMessage())), Right(_))
      auditLog     <- json.as[AuditLogJson].fold(e => Left(new JsonParseError(e.getMessage())), Right(_))
    } yield auditLog.databaseActivityEventList
  }

  private def getByteArray(b: ByteBuffer): Array[Byte] = {
    val byteArray = new Array[Byte](b.remaining)
    b.get(byteArray)
    byteArray
  }

  private def decompress(src: Array[Byte]): Either[DecompressError, String] = {
    try {
      val byteArrayInputStream = new ByteArrayInputStream(src)
      val gzipInputStream      = new GZIPInputStream(byteArrayInputStream)
      Right(IOUtils.toString(gzipInputStream))
    } catch { case e: Throwable => Left(new DecompressError(e.getMessage)) }
  }

}
