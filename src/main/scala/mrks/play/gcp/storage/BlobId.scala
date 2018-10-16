package mrks.play.gcp.storage

import com.google.cloud.storage.{BlobId => JBlobId}

case class BlobId(bucket: String, name: String, generation: Option[Long] = None) {
  def asJava: JBlobId = if (generation.isDefined) JBlobId.of(bucket, name, generation.get) else JBlobId.of(bucket, name)
}
