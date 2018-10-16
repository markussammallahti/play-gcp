package mrks.play.gcp.storage

import java.nio.channels.Channels

import akka.stream.IOResult
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import com.google.cloud.storage.Storage.CopyRequest
import com.google.cloud.storage.{BlobInfo, Storage => StorageService}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}

trait Storage {
  implicit def ec: ExecutionContext

  def service: StorageService

  private def blobInfo(blobId: BlobId, contentType: Option[String]) = {
    val builder = BlobInfo.newBuilder(blobId.asJava)
    contentType.foreach(builder.setContentType)
    builder.build
  }

  def get(blobId: BlobId): Future[Option[Blob]] = {
    Future(blocking(Option(service.get(blobId.asJava)).map(Blob.fromJava)))
  }

  def create(blobId: BlobId, contentType: String, data: ByteString): Future[Blob] = {
    Future(blocking(Blob.fromJava(service.create(blobInfo(blobId, Some(contentType)), data.toArray))))
  }

  def copy(from: Blob, to: BlobId): Future[Blob] = {
    val request = CopyRequest
      .newBuilder
      .setSource(from.id.asJava)
      .setTarget(to.asJava)
      .build

    Future(blocking(Blob.fromJava(service.copy(request).getResult)))
  }

  def delete(blob: Blob): Future[Boolean] = {
    Future(blocking(service.delete(blob.id.asJava)))
  }

  def source(blob: Blob): Source[ByteString, Future[IOResult]] = {
    StreamConverters.fromInputStream(() => Channels.newInputStream(service.reader(blob.id.asJava)))
  }

  def sink(blobId: BlobId, contentType: Option[String] = None): Sink[ByteString, Future[Try[Blob]]] = {
    StreamConverters.fromOutputStream(() => Channels.newOutputStream(service.writer(blobInfo(blobId, contentType)))).mapMaterializedValue(_.flatMap {
      case IOResult(_, Failure(e)) =>
        Future.successful(Failure(e))

      case _ =>
        Future(blocking(Success(Blob.fromJava(service.get(blobId.asJava)))))
    })
  }
}

class DefaultStorage @Inject()(val service: StorageService)(implicit val ec: ExecutionContext) extends Storage
