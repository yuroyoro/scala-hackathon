import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.jcl.Conversions._

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{ SelectionKey,
  Selector, ServerSocketChannel, SocketChannel }
import java.nio.charset.Charset

object EchoServerNIO{

  def main( args:Array[String] ){
    EchoActor.start()
    EchoActor ! Start( args.first.toInt )
  }
}

case class Select()
case class Start( port:Int )
case class Accept( key:SelectionKey )
case class Read( key:SelectionKey )

object EchoActor extends Actor {
  val BUF_SIZE = 1024
  lazy val selector = Selector.open
  lazy val serverChannel = ServerSocketChannel.open

  def act() = {
    loop {
      react {
        case Start( port ) => {
          serverChannel.configureBlocking(false)
          serverChannel.socket.bind( new InetSocketAddress( port ) )
          serverChannel.register(selector, SelectionKey.OP_ACCEPT )

          println( "EchoServeが起動しました。port=%d".format( port ) )
          EchoActor ! Select
        }
        case Select => {
          selector.select
          selector.selectedKeys.foreach{ key =>
            if( key.isAcceptable ){
              EchoActor ! Accept( key )
            }
            else if( key.isReadable ){
              EchoActor ! Read( key )
            }
          }
          EchoActor ! Select
        }
        case Accept( key ) => {
          val socket = key.channel.asInstanceOf[ServerSocketChannel]
          socket.accept match {
            case null =>
            case channel => {
              val remoteAddress = channel.socket.getRemoteSocketAddress.toString();
              println(remoteAddress + ":[接続しました]" )

              channel.configureBlocking(false)
              channel.register(selector, SelectionKey.OP_READ);
            }
          }
        }
        case Read( key ) => {
          val channel = key.channel.asInstanceOf[SocketChannel]
          val buf = ByteBuffer.allocate(BUF_SIZE)
          val charset = Charset.forName("UTF-8")

          val remoteAddress = channel.socket.getRemoteSocketAddress.toString();
          def close = {
            channel.close()
            println( remoteAddress + ":[切断しました]" )
          }

          channel.read( buf ) match {
            case -1 => close
            case 0 =>
            case x => {
              buf.flip
              println( "%s:%s".format( remoteAddress, charset.decode(buf).toString ))
              buf.flip
              channel.write( buf )
              close
            }
          }

        }
      }
    }
  }
}
