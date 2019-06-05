import akka.actor.ActorSystem
import akka.event.Logging
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.example.helloworld.{GreeterService, GreeterServiceClient, HelloRequest}

import scala.util.{Failure, Success}

object TestGreeter {
  def main(args: Array[String]): Unit = {
    // Boot akka
    implicit val sys = ActorSystem("HelloWorldClient")
    implicit val log = Logging(sys.getEventStream, "me")
    implicit val mat = ActorMaterializer()
    implicit val ec = sys.dispatcher

    val client: GreeterService = GreeterServiceClient(
      GrpcClientSettings.connectToServiceAt("localhost", 8081)
        .withTls(false)
    )
    Source(1 to 10000000)
      .map(_.toString).async
      .map(HelloRequest(_)).async
      .mapAsyncUnordered(12)(client.sayHello)
      .runForeach(println)
      .onComplete {
        case Success(_) => println("done"); System.exit(0)
        case Failure(t) => println(t.getMessage); System.exit(1)
      }
  }
}
