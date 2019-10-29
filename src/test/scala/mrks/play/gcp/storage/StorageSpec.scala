package mrks.play.gcp.storage

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.google.cloud.storage.contrib.nio.testing.LocalStorageHelper
import mrks.test.{MaterializerPerSuite, SourceMatchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest._

import scala.concurrent.ExecutionContext
import scala.collection.convert.ImplicitConversions._

class StorageSpec extends WordSpec
  with BeforeAndAfterEach
  with MustMatchers
  with SourceMatchers
  with MaterializerPerSuite
  with ScalaFutures
  with TryValues
  with OptionValues {

  private val mockService = LocalStorageHelper.getOptions.getService

  private val storage = new Storage {
    override implicit def ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    override def service = mockService
  }

  override implicit def patienceConfig = PatienceConfig(
    timeout   = scaled(Span(2000, Millis)),
    interval  = scaled(Span(100, Millis))
  )

  private val testBucket  = "testing"
  private val testContent = ByteString("testing\nmultiple\nrows")

  override def afterEach(): Unit = {
    super.afterEach()

    mockService.list(testBucket).getValues.foreach(blob => mockService.delete(blob.getBlobId))
  }

  "Get" when {
    "service returns null" should {
      "return none" in {
        whenReady(storage.get(BlobId(testBucket, "a"))) {
          _ mustBe None
        }
      }
    }
    "service returns blob" should {
      "return blob" in {
        storage.create(BlobId(testBucket, "exists"), "text/plain", testContent).futureValue

        whenReady(storage.get(BlobId(testBucket, "exists"))) {
          _.value mustBe Blob(BlobId(testBucket, "exists", Some(0L)), testContent.length, Some("text/plain"))
        }
      }
    }
  }

  "Source" should {
    "create source of blob contents" in {
      val blob = storage.create(BlobId(testBucket, "source"), "text/plain", testContent).futureValue

      storage.source(blob) mustBe sourceOf(testContent)
    }
  }

  "Sink" should {
    "create sink for given blob id" in {
      val sink    = storage.sink(BlobInfo(BlobId(testBucket, "sink"), "text/plain"))
      val result  = Source(List(testContent, testContent)).runWith(sink).futureValue
      val blob    = result.success.value

      storage.source(blob) mustBe sourceOf(testContent ++ testContent)
    }
  }
}
