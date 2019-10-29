package mrks.play.gcp.storage

import java.io.{InputStream, OutputStream}
import java.net.URL
import java.nio.channels.Channels
import java.util.concurrent.TimeUnit

import akka.stream.IOResult
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import com.google.cloud.storage.Storage.CopyRequest
import com.google.cloud.storage.{Storage => StorageService}
import javax.inject.Inject

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}

trait Storage {
  implicit def ec: ExecutionContext

  def service: StorageService

  def get(blobId: BlobId): Future[Option[Blob]] = {
    Future(blocking(Option(service.get(blobId.asJava)).map(Blob.fromJava)))
  }

  def create(blobId: BlobId, contentType: String, data: ByteString): Future[Blob] = {
    Future(blocking(Blob.fromJava(service.create(BlobInfo(blobId, contentType).asJava, data.toArray))))
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
    StreamConverters.fromInputStream(() => inputStream(blob.id))
  }

  def inputStream(blobId: BlobId): InputStream = {
    Channels.newInputStream(service.reader(blobId.asJava))
  }

  def sink(info: BlobInfo): Sink[ByteString, Future[Try[Blob]]] = {
    StreamConverters.fromOutputStream(() => outputStream(info)).mapMaterializedValue(_.flatMap {
      case IOResult(_, Failure(e)) =>
        Future.successful(Failure(e))

      case _ =>
        Future(blocking(Success(Blob.fromJava(service.get(info.id.asJava)))))
    })
  }

  def outputStream(info: BlobInfo): OutputStream = {
    Channels.newOutputStream(service.writer(info.asJava))
  }

  def signUrl(info: BlobInfo, duration: Duration, options: SignUrlOption*): URL = {
    service.signUrl(info.asJava, duration.toMillis, TimeUnit.MILLISECONDS, options.map(_.asJava):_*)
  }
}

class DefaultStorage @Inject()(val service: StorageService)(implicit val ec: ExecutionContext) extends Storage
