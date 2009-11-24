シンプルなエコーサーバー
____________________________


TODO あとで説明書く

.. code-block:: scala

  import scala.actors.Actor
  import scala.actors.Actor._

  import java.io._
  import java.net.{ServerSocket, Socket }

  object EchoServer{

    def main( args:Array[String] ) = {
      EchoActor.start()
      EchoActor ! Start( args.first.toInt )
    }
  }

  case class Start( port:Int )
  case class Wait( serverSocket:ServerSocket )
  case class Accept( socket:Socket )

  object EchoActor extends Actor {

    def act() = {
      loop{
        react {
          case Start( port ) => {
            val serverSocket = new ServerSocket( port )
            println( "EchoServeが起動しました。port=%d".format( port ) )
            EchoActor ! Wait( serverSocket )
          }
          case Wait( serverSocket ) => {
            val socket = serverSocket.accept
            val acceptActor = new AcceptActor
            acceptActor.start()
            acceptActor! Accept( socket )
            EchoActor ! Wait( serverSocket )
          }
        }
      }
    }
  }

  class AcceptActor extends Actor {
    def act() = {
      react {
        case Accept( socket ) => {
          val address = socket.getInetAddress
          println( address + ":[接続しました]" )

          val in = new BufferedReader( new InputStreamReader(socket.getInputStream ) )
          val out = new PrintWriter( socket.getOutputStream,  true)
          val s = in.readLine
          println( "%s:%s".format( address , s ) )
          out.println( s )

          out.close
          in.close
          socket.close
          println( address + ":[切断しました]" )
        }
      }
    }
  }
