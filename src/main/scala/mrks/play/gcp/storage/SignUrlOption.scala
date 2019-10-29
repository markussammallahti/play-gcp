package mrks.play.gcp.storage

import com.google.cloud.storage.{HttpMethod => JHttpMethod}
import com.google.cloud.storage.Storage.{SignUrlOption => JSignUrlOption}

sealed trait SignUrlOption {
  def asJava: JSignUrlOption
}

object SignUrlOption {
  def apply(f: => JSignUrlOption): SignUrlOption = new SignUrlOption {
    override def asJava: JSignUrlOption = f
  }

  final val ContentType = SignUrlOption(JSignUrlOption.withContentType())

  object HttpMethod {
    final val GET     = SignUrlOption(JSignUrlOption.httpMethod(JHttpMethod.GET))
    final val POST    = SignUrlOption(JSignUrlOption.httpMethod(JHttpMethod.POST))
    final val PUT     = SignUrlOption(JSignUrlOption.httpMethod(JHttpMethod.PUT))
    final val DELETE  = SignUrlOption(JSignUrlOption.httpMethod(JHttpMethod.DELETE))
  }
}
