package mrks.play.gcp.parser

import javax.inject.Inject
import mrks.play.gcp.storage.{Blob, BlobInfo, Storage}
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{BodyParser, MultipartFormData, PlayBodyParsers}
import play.core.parsers.Multipart

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait GcpBodyParsers {
  implicit def executionContext: ExecutionContext

  def storage: Storage
  def playBodyParsers: PlayBodyParsers
  def temporaryBlobIdCreator: TemporaryBlobIdCreator

  def multipartFormData: BodyParser[MultipartFormData[Blob]] = playBodyParsers.multipartFormData(storageHandler)

  private def storageHandler: Multipart.FilePartHandler[Blob] = { info =>
    Accumulator(storage.sink(BlobInfo(temporaryBlobIdCreator.create(info), info.contentType))).mapFuture {
      case Failure(error) =>
        Future.failed(error)

      case Success(blob) =>
        Future.successful(FilePart(info.partName, info.fileName, info.contentType, blob))
    }
  }
}

class DefaultGcpBodyParsers @Inject()(
    val storage: Storage,
    val playBodyParsers: PlayBodyParsers,
    val temporaryBlobIdCreator: TemporaryBlobIdCreator
  )(implicit val executionContext: ExecutionContext) extends GcpBodyParsers
