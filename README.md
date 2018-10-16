# play-gcp

[Google Cloud Platform](https://cloud.google.com/) utilities for [Play Framework](https://www.playframework.com/)

### Usage

#### Add dependency
```
resolvers += Resolver.bintrayRepo("mrks", "maven")
libraryDependencies += "mrks" %% "play-gcp" % "0.1"
```

#### Setup authentication

[See google library documentation](https://cloud.google.com/storage/docs/reference/libraries#setting_up_authentication)

#### Configure body parser
```
play.modules.enabled += "mrks.play.gcp.GcpModule"

gcp.parser {
  bucket = "target-bucket"
  prefix = "file-name-prefix"
}
```
Generated temporary Blob ID will be "**[gcp.parser.bucket]**/**[gcp.parser.prefix]**/**[random uuid v4]**"

#### User gcp body parser and storage

```scala
package controllers

import javax.inject.Inject
import mrks.play.gcp.parser.GcpBodyParsers
import mrks.play.gcp.storage.{BlobId, Storage}
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class ExampleController @Inject()(
    cc: ControllerComponents,
    gcpBodyParsers: GcpBodyParsers,
    storage: Storage
  )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def uploadAndStreamBack = Action.async(gcpBodyParsers.multipartFormData) { request =>
    request.body.file("file") match {
      case Some(temp) =>
        for {
          blob  <- storage.copy(temp.ref, BlobId("target-bucket", "target/path"))
          _     <- storage.delete(temp.ref)
        } yield {
          Ok.chunked(storage.source(blob)).as(blob.contentType.getOrElse(BINARY))
        }

      case _ =>
        Future.successful(BadRequest)
    }
  }
}
```
