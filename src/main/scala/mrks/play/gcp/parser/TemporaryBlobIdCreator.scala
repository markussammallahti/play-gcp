package mrks.play.gcp.parser

import java.util.UUID

import javax.inject.Inject
import mrks.play.gcp.storage.BlobId
import play.core.parsers.Multipart

trait TemporaryBlobIdCreator {
  def config: ParserConfig

  def create(info: Multipart.FileInfo): BlobId = {
    BlobId(config.bucket, s"${config.prefix}/${UUID.randomUUID}")
  }
}

class DefaultTemporaryBlobIdCreator @Inject()(val config: ParserConfig) extends TemporaryBlobIdCreator
