Actorライブラリによる非同期処理
------------------------------------

Actorモデルとは?
__________________________________

Actorとよばれる概念を基本要素として、プログラムを構成するモデルです。
メッセージを送りあうことで、複数のActorが連携して動作します。

OOPにおいてオブジェクトがメッセージを送りあうのと似ていますが、Actorの場合は以下のような特徴を持ちます。

- 非同期メッセージを基本とする
- Actorごとに独立した状態をもつ

Scalaでの特徴
__________________________________

スレッドを使用してActorを実現しています。
このため、ミュータブルなオブジェクトを複数のActorで共有することも可能となっています。

ただし、競合やデッドロックを避けるために、できる限りイミュータブルなオブジェクトのみを共有、ミュータブルなオブジェクトはActor内部のみで使用するのが望ましいです。

また、スレッドとActorは1:1に対応づいているわけではなく、必要に応じてプールされているスレッドがActorに割り当てられます。
Actorの数に対して必要なスレッド数が少なくすむため、スレッド数の限界を超えるような多量のActorを生成することができます。

メッセージの送信と受信
__________________________________

以下は、二つのActor間でメッセージを送受信するプログラムになります。
実行すると、標準出力に"Ping!"と表示された後に、"Pong!"と表示されます。

.. code-block:: scala

  import scala.actors.Actor
  import scala.actors.Actor._

  case class Ping(from: Actor)
  case object Pong

  object PingPong {
    def main(args: Array[String]): Unit = {
      val actor1 = new Actor {
        override def act = react {
          case Ping(from) =>
            println("Ping!")
            from ! Pong
        }
      }

      val actor2 = new Actor {
        override def act = {
          actor1 ! Ping(self)
          react {
            case Pong => println("Pong!")
          }
        }
      }

      actor1.start
      actor2.start
    }
  }

Actorを作成するには、Actorトレイトを継承しactメソッドを実装します。
startメソッドを呼び出してActorを開始することで、別スレッドでactメソッドが実行されます。

メッセージの送信には ! または !?、!! メソッドを使用します。メッセージには任意のオブジェクトを使用できます。

メッセージの受信にはreceiveまたはreactメソッドを使用します。
reactに続いて、パターンマッチを記述することで、それぞれのメッセージを受信できます。
