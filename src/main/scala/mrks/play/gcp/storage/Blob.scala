package mrks.play.gcp.storage

import com.google.cloud.storage.{Blob => JBlob}

case class Blob(id: BlobId, size: Long, contentType: Option[String])

object Blob {
  def fromJava(blob: JBlob): Blob = Blob(
    id          = BlobId(blob.getBucket, blob.getName, Option(blob.getGeneration)),
    size        = blob.getSize,
    contentType = Option(blob.getContentType)
  )
}
