package mrks.play.gcp

import com.google.cloud.storage.{StorageOptions, Storage => StorageService}
import mrks.play.gcp.parser._
import mrks.play.gcp.storage.{DefaultStorage, Storage}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

class GcpModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[StorageService].toInstance(StorageOptions.getDefaultInstance.getService),
      bind[Storage].to[DefaultStorage],
      bind[TemporaryBlobIdCreator].to[DefaultTemporaryBlobIdCreator],
      bind[GcpBodyParsers].to[DefaultGcpBodyParsers],
      bind[ParserConfig].toInstance(parserConfig(configuration))
    )
  }

  private def parserConfig(configuration: Configuration) = ParserConfig(
    bucket = configuration.get[String]("gcp.parser.bucket"),
    prefix = configuration.get[String]("gcp.parser.prefix")
  )
}
