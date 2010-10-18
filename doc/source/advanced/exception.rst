例外処理
----------------------------

Scalaの例外処理について解説しますよ。

基本 - try,catch,finally
__________________________________________

例外処理の基本は、try,catch,finallyです。これはJavaと同じですね。


ただし、Scalaでのtry,catch,finallyは値を生成します。つまり、実行されたブロックの値が評価されます。


.. code-block:: scala

  scala> val n = try{ "99".toInt } catch { case e:Exception => -99 }
  n: Int = 99

  scala> val n = try{ "foo".toInt } catch { case e:Exception => -99 }
  n: Int = -99


上記のように、catch節には "case e:Exception => ..."のようなパターンマッチを書くことで、対応する例外の型に応じて補足することができます。try,catchを評価した結果は、実行されたブロックで最後に評価された式の値になるわけです。


また、finally節も同様に指定できます。ただし、finally節で評価された値は捨てられます。以下は、finallyを指定していますが、最終的な値はcatch節で評価されたものになっています。

.. code-block:: scala

  scala> val n = try{ "foo".toInt } catch {
       |   case e:Exception => -99 } finally{println("finnally"); 0 }
  finnally
  n: Int = -99


Scalaの例外は全て非チェック例外です
__________________________________________

Scalaにおける例外は、Javaとは異なり基本的に非チェック例外です。JavaのAPIを呼び出す際に、Java側のAPIでthrows宣言されているメソッドをScalaから呼び出しても、例外を補足する必要はないので、コンパイルエラーにはなりません。


例えば、java.io.FileInputStreamクラスをファイル名を与えて生成する場合、JavaではFileNotFoundExceptionをcatchする必要がありますが、Scalaでは別にcatchしなくてもよいのです。


.. code-block:: scala

  scala> val fin = new java.io.FileInputStream( "foo.txt")
  java.io.FileNotFoundException: foo.txt (No such file or directory)
    at java.io.FileInputStream.open(Native Method)


また、Scalaでは、メソッドの宣言時にthrows節で例外を明示することはできません。


ようは、チェック例外面倒だよねってことだと思います。あと、funciton typeに例外に関する情報ももたなくてはならなくてJava7のクロージャみたいになっちゃうからじゃないかと。


ただし、Javaとの相互運用を考えて、throws節をもったメソッドとしてbytecodeを生成させることはできます。メソッド宣言に@throwsアノテーションをつけることで、コンパイルされたbytecodeにはthrows節が付与されます。

.. code-block:: scala

  object ThrowRTE {
    @throws (classOf[RuntimeException])
    def apply = throw new RuntimeException
  }


このようなオブジェクトをscalacでコンパイルして、bytecodeをscalapで覗いてみると


.. code-block:: scala

  package ThrowRTE$;
  final class ThrowRTE$ extends scala.AnyRef with scala.ScalaObject {
    def this(): scala.Unit;
    def apply(): scala.runtime.Nothing$;
      throws java.lang.RuntimeException
  }
  object ThrowRTE$ {
    final val MODULE$: ThrowRTE$;
  }


applyには"throws java.lang.RuntimeException"がちゃんとついてます。

Optionを利用する
__________________________________________
単純な例外処理として、例外が発生したかどうかさえわかればOption型を返す関数にラップするのが便利です。


文字列をInt型に変換するときに、失敗したらなんらかの初期値を設定したい場合は、このような関数にラップします。

.. code-block:: scala

  scala> def toIntOption( s:String ):Option[Int] = try{ Some( s.toInt ) } catch { case _ =>None }}
  toIntOption: (s: String)Option[Int]


これで、toIntが失敗したらNoneが返るようになりました。あとは、Option.getOrElseで初期値を渡せばよいわけです。

.. code-block:: scala

  scala> val n = toIntOption( "foo" ) getOrElse( -1 )
  n: Int = -1


で、毎回こんな関数を定義するのは面倒なので、汎用的に処理をOptionに変換する疑似制御構文を用意しましょう。こんな感じの関数を用意します。


.. code-block:: scala

  def tryo[T]( f: => T )( implicit onError: Throwable => Option[T] = { t:Throwable => None }): Option[T] = {
    try {
      Some( f )
    } catch {
      case c => onError( c )
    }
  }


このtryo関数は、引数の関数を実行した結果をOption型に変換します。カリー化されて宣言されていて、第一引数が例外が起こるかもしれない処理、第2引数は省略可能で、例外が発生したときにThrowableを引数にもらってOption[T]を返す例外ハンドラです。


.. code-block:: scala

  scala> tryo{ "foo".toInt }
  res3: Option[Int] = None

  scala> tryo{ "99".toInt }
  res4: Option[Int] = Some(99)

  scala> tryo{ "foo".toInt }{ case _ => Some( -99 ) }
  res5: Option[Int] = Some(-99)


Eitherを利用する
__________________________________________

Eitherを利用すると、例外が発生するかもしれないメソッドの呼び出しをエレガントにラップすることができます。


Either型は、ふたつの型の値をもつ可能性があることを示す型です。Option型と同様に、Either型自身は抽象クラスで、サブタイプとしてLeft型とRight型があります。


Either[Int,String]型は、Left[Int]型であるかもしれないし、Right[String]型かもしれないという意味になります。


.. code-block:: scala

  class Either [+A, +B] extends AnyRef
  class Left [+A, +B] (a: A) extends Either[A, B] with Product
  class Right [+A, +B] (b: B) extends Either[A, B] with Product


例外処理において、Eitherは以下のように利用します。


.. code-block:: scala

  scala> def fileInputStream( filename:String ):Either[FileNotFoundException,FileInputStream] =
       |   try{
       |     Right( new FileInputStream( filename ) )
       |   }catch{
       |     case e:FileNotFoundException => Left(e )
       |   }
  fileInputStream: (filename: String)Either[java.io.FileNotFoundException,java.io.FileInputStream]



このように、FileNotFoundExceptionが発生した場合はLeft[FileNotFoundException]として例外オブジェクトを返し、正常の場合はRight[FileInputStream]を返すという関数にラップするわけです(Rightには"正しい"という意味もあって、成功した場合の値をRightでかえすようにするのがよいようです)。


.. code-block:: scala

  scala> fileInputStream( "foo.txt" ) match {
       |   case Left(e) => println( e.toString )
       |   case Right(fin ) => println( "suceess")
       | }
  java.io.FileNotFoundException: foo.txt (No such file or directory)


Either型も、Optionと同じようにパターンマッチで処理させるのが基本です。


Optionのときに汎用的にOptionにする関数を用意したように、Eitherでも同じようにできます。


.. code-block:: scala

  def trye[T]( f: => T )( implicit onError: Throwable => Either[Throwable,T] = { t:Throwable => Left( t ) }): Either[Throwable,T] = {
    try{
      Right( f )
    } catch {
      case c => onError( c )
    }
  }


これも、同じように使えます。


.. code-block:: scala

  scala> trye{ new FileInputStream( "foo") }
  res6: Either[Throwable,java.io.FileInputStream] = Left(java.io.FileNotFoundException: foo (No such file or directory))

  scala> trye{ new FileInputStream( "/Users/ozaki/.zshrc") }
  res7: Either[Throwable,java.io.FileInputStream] = Right(java.io.FileInputStream@7178820c)


scala.util.control.Exception
__________________________________________

さて、OptionやEitherに任意の処理を自動的にラップする疑似制御構文を作りましたが、Scala2.8からはscala.util.control.Exceptionとして既に用意されています。


[http://www.scala-lang.org/archives/downloads/distrib/files/nightly/docs/library/scala/util/control/Exception$.html:title]


こんな感じで使います。


.. code-block:: scala

  scala> import scala.util.control.Exception._
  import scala.util.control.Exception._

  scala> catching(classOf[NumberFormatException]) opt "foo".toInt
  res12: Option[Int] = None

  scala> catching(classOf[NumberFormatException]) opt "99".toInt
  res13: Option[Int] = Some(99)

  scala> allCatch opt "hoge".toInt getOrElse(-1)
  res14: Int = -1

  scala> allCatch either "foo".toInt
  res15: Either[Throwable,Int] = Left(java.lang.NumberFormatException: For input string: "foo")

  scala> allCatch either "1919".toInt
  res16: Either[Throwable,Int] = Right(1919)



allcatch/catching( classOf[例外型] ) opt/either { 処理 } のようにすることで、あぶなっかしい処理をOptionにしたりEitherにしたりできますお。その他の詳しい使い方はScalaDocみてくださいね。

