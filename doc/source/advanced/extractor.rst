Extractor(抽出子)
----------------------------

パターンマッチは便利すぎてこれを覚えてしまうと他の言語でも欲しくてたまらなくなりますね。


Scalaのパターンマッチは、実はユーザーが独自のパターンを定義することが可能になっています。

extractor(抽出子)によるパターンの拡張
__________________________________________

簡単な例を紹介します。java.util.Date型のオブジェクトを年月日に分解してパターンマッチさせたい、とします。こんな利用イメージです。

.. code-block:: scala

  scala> new Date match {
       |   case DateOf( y,m,d ) => println( "%d年%02d月%02d日" format( y,m,d ) )
       |   case _ =>
       | }
  2010年07月09日


"case DateOf( y,m,d) => ..."の部分で、Date型を年月日に分解しているわけです。ふつーにやろうとすると、DateOfというケースクラスを作ってimplicit conversionでDate型から変換でしょうか?でもimplicit conversionはあまり使いたくないですよね?


そこで、以下のようなDateOfオブジェクトを定義します。


.. code-block:: scala

  import java.util.{Date,Calendar}

  object DateOf {

    // 抽出メソッド
    // java.util.Date型のオブジェクトの構造を年月日に分解する
    def unapply( d:Date ):Option[(Long,Long,Long)] = {
      val c = Calendar.getInstance
      c.setTime( d )
      Some(( c.get( Calendar.YEAR ), c.get( Calendar.MONTH ) + 1,
             c.get( Calendar.DAY_OF_MONTH ) ))
    }
  }


このDateOfオブジェクトは、"unapply( d:Date ):Option[(Long,Long,Long)]"というメソッドを持っています。この"unapply"という名前のメソッドはScalaの言語仕様で特別あつかいされている名前で、抽出メソッドと言います。


パターンマッチのcaseにコンストラクタパターンが出現すると、対応する名前のオブジェクトのunapplyメソッドが呼び出されます。引数には、match式の対象(例ではjava.util.Dateオブジェクト)が渡されます。


unapplyは、引数のオブジェクトを解析し、結果をOption[(T1,T2,...)]型のようにTupleをOptionに入れて返すようにします。DataOfオブジェクトのunapplyは、結果をSome(年,月,日)で返します。パターンにマッチしない場合はNoneを返すようにします。


このunapplyの結果が、"case DateOF(y,m,d) => ..."の"(y,m,d)"に対応するわけです。よって、"case DateOf(y,m,d) => ..."と書くと、unapplyにより返された(年,月,日)というTupleがパターン変数(y,m,d)に束縛されるので、java.util.Date型を年月日に分解できる、って仕組みです。


このようにして定義されたunapplyによるパターンは、以下のような代入時にも利用できます。


.. code-block:: scala

  scala> val DateOf( y,m,d ) = new Date
  y: Long = 2010
  m: Long = 7
  d: Long = 9

ケースクラスとunapply
__________________________________________

さて、Scalaではケースクラスを定義するとパターンマッチで利用できるようになりました。実は、ケースクラスを定義すると、Scalaコンパイラはケースクラスのコンパニオンオブジェクトを自動生成し、ケースクラスのコンストラクタシグニチャに一致するunapplyメソッドを自動で生成します。


実際に生成されているか確認しましょう。以下のようなケースクラスFooがあったとして、

.. code-block:: scala

  case class Foo( n:Int, s:String )


これをscalapコマンドで生成されたクラスファイルの中身を覗いてみます。

.. code-block:: scala

  FILENAME = ./Foo$.class
  package Foo$;
  final class Foo$ extends scala.runtime.AbstractFunction2 with scala.ScalaObject {
    def this(): scala.Unit;
    def apply(scala.Any, scala.Any): scala.Any;
    def apply(scala.Int, java.lang.String): Foo;
    def unapply(Foo): scala.Option;
  }
  object Foo$ {
    final val MODULE$: Foo$;
  }


実際にunapplyが定義されていますね。Scalaでのコンストラクタパターンによるマッチは、実はunapplyによって実現されているのです。(言語仕様的にはケースクラスのパターンは8.1.5 Constructor Patternsでunapplyは8.1.7 Extractor Patterns のようですが、俺には違いがわかりません…。)


このような抽出メソッドunapplyが定義されているオブジェクトを"extractor(抽出子)"と言います。"extractor(抽出子)"は、かならずしもシングルトンオブジェクトである必要はなく、クラスのインスタンスメソッドとして定義しても問題ありません。インスタンスメソッドに抽出メソッドが定義されている例としては、次に解説するRegexクラスなどがあります。

可変長パターンマッチとunapplySeq
__________________________________________


正規表現のグループは、Scalaではこのようにキャプチャすることができます。

.. code-block:: scala

  scala> val datePattern = """(\d+)年(\d+)月(\d+)日""".r
  datePattern: scala.util.matching.Regex = (\d+)年(\d+)月(\d+)日

  scala> "2010年07月09日" match {
       |   case datePattern( y,m,d ) => println( "%s/%s/%s" format(y,m,d) )
       |   case _ =>
       | }
  2010/07/09


正規表現オブジェクトによるパターンマッチも、裏側では抽出メソッドが動いています。しかし、正規表現による"()"のパターン数は予めわかりません。このような可変長のパターンに対応するには、抽出メソッドとして"unapplySeq"を定義します。


以下は、scala.util.matching.Regexクラスのソースコードからの抜粋です。


.. code-block:: scala

  class Regex(regex: String, groupNames: String*) {
    val pattern = Pattern.compile(regex)

    /** Tries to match target (whole match) and returns
     *  the matches.
     *
     *  @param target The string to match
     *  @return       The matches
     */
    def unapplySeq(target: Any): Option[List[String]] = target match {
      case s: java.lang.CharSequence =>
        val m = pattern.matcher(s)
        if (m.matches) Some((1 to m.groupCount).toList map m.group)
        else None
      case Match(s) =>
        unapplySeq(s)
      case _ =>
        None
    }
  }

unapplySeqは、String型を引数に取ってOption[List[String]]を結果で返します。可変長パターンに対する抽出メソッドは、構造解析の結果をSeq[T]で返す必要があります。


ユーザー定義の抽出メソッドとして、このような可変長のパターンに対応するには、同様にunapplySeqを定義すればよいわけです。以下の例は"/usr/local/bin/"のようなパス表現を分解するようにunapplySeqを定義した例です。


.. code-block:: scala

  object Path {

    def unapplySeq(s: String): Option[Seq[String]] =
      if( s.trim.isEmpty ) None
      else Some( s.split("/").dropWhile( _.isEmpty ).takeWhile( _.nonEmpty ) )
  }


このように、String型のパス表現に対して可変長のマッチが可能となっています。

.. code-block:: scala

  scala> "/usr/local/bin/" match {
       |   case Path( "usr", p@_* ) => println( p )
       | }
  WrappedArray(local, bin)

  scala> "/var/tmp" match {
       |   case Path( "var", p@_* ) => println( p )
       | }
  WrappedArray(tmp)


まとめ
__________________________________________

"extractor(抽出子)"によるパターンの拡張は、オブジェクトを構造解析して別なオブジェクトの表現に変換する、という意味合いがあります。積極的にextractor(抽出子)を定義することで、様々な場所でのパターンの活用が可能になりステキですよね?


