package mrks.play.gcp.storage

import com.google.cloud.storage.{BlobInfo => JBlobInfo}

case class BlobInfo(id: BlobId, contentType: Option[String]) {
  def asJava: JBlobInfo = {
    val builder = JBlobInfo.newBuilder(id.asJava)
    contentType.foreach(builder.setContentType)
    builder.build()
  }
}

object BlobInfo {
  def apply(id: BlobId): BlobInfo = BlobInfo(id, None)
  def apply(id: BlobId, contentType: String): BlobInfo = BlobInfo(id, Some(contentType))
}
