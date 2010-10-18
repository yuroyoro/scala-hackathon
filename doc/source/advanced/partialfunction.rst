PartialFunction - 部分関数
--------------------------------

Scalaには、PartialFunctionというものがあります。


直訳すると部分関数ですが、これはなにかっていうと「特定の引数に対しては結果を返すけど、結果を返せない引数もあるような中途半端な関数」です。


どうやって使うのん?
_______________________________________

まぁ、ちょっと例を見てましょうや。PartialFunctionであるfooPfは、引数が"foo"だったら"bar"を返して、"foo"以外は知らんというてきとーな関数です。


.. code-block:: scala

  scala> val fooPf:PartialFunction[String,String] = { case "foo" => "bar" }
  fooPf: PartialFunction[String,String] = <function1>

  scala> fooPf("foo")
  res5: String = bar

  scala> fooPf("hoge")
  scala.MatchError: hoge
    at $anonfun$1.apply(<console>:5)
    at $anonfun$1.apply(<console>:5)




PartialFunctionは、Function1のサブトレイトで、ようは引数をひとつもらう関数です。fooPfは、String型をもらってString型を返すので、PartialFunction[String,String]ですよ。


PartialFunctionを定義する際には、match式の中のcase句の集合として定義できます。このような書き方は実はシンタックスシュガーなんですけどね。

引数に対して結果を返すかisDefinedAtで調べる
_______________________________________________

で、fooPf("foo")と引数に"foo"を与えるとちゃんと"bar"が返りますが、"hoge"とかで呼び出すとMatchErorrをなげるとんでもないヤツです。


これだけだと使いにくくてしょうがないですが、PartialFunctionには事前に引数に対して結果を返すか調べる関数が定義されています。isDefinedAt[A]です。


.. code-block:: scala

  scala> fooPf.isDefinedAt("foo")
  res6: Boolean = true

  scala> fooPf.isDefinedAt("hoge")
  res7: Boolean = false



このようにして、PartialFunctionが結果を返すかは事前にしることができます。

orElseでPartialFunctionを合成する
_______________________________________________

PartialFunctionは、orElseという関数で他のPartialFunctionと合成できます。以下のような引数が"baz"だったら"hoge"を返すPartialFunctionとfooPfをorElseで合成すると、引数が"foo"または"baz"だったら結果を返す新しいPartialFunctionであるfooOrBazPFが生成されます。


.. code-block:: scala

  scala> val bazPf:PartialFunction[String,String] = { case "baz" => "hoge" }
  bazPf: PartialFunction[String,String] = <function1>

  scala> val fooOrBazPF = fooPf orElse bazPf
  fooOrBazPF: PartialFunction[String,String] = <function1>

  scala> fooOrBazPF( "foo")
  res8: String = bar

  scala> fooOrBazPF( "baz")
  res9: String = hoge

  scala> fooOrBazPF( "aaa")
  scala.MatchError: aaa
    at $anonfun$1.apply(<console>:5)
    at $anonfun$1.apply(<console>:5)


まぁ、ぶっちゃけて感覚的にいうと、PartialFunctionってのはmatch式のパターンを部分的にオブジェクトとして取り扱うことができるってことです。いろいろな条件のPartialFunctionを作っておいて、orElseで合成しながら条件に応じたmatch式をランタイムにくみ上げることができますよ。


Liftでも、URLのmappingあたりでPartialFunctionが使われてます。

コレクションとPartialFunction
_______________________________________________

で、PartialFunctionの一番の使いどころは、コレクションに対するMap操作でしょうか。


まず、引数のStringがnullまたは空文字だったらNoneでそれ以外はSome[String]を返すPartialFunctionをこんな風に定義しておきます。

.. code-block:: scala

  scala> val pf:PartialFunction[String,Option[String]] = {
       |   case null => None
       |   case "" => None
       |   case s => Some(s)
       | }
  pf: PartialFunction[String,Option[String]] = <function1>

  scala> pf( null)
  res11: Option[String] = None

  scala> pf( "")
  res12: Option[String] = None

  scala> pf( "hogehoge")
  res13: Option[String] = Some(hogehoge)


で、Seq[String]であるコレクションから、nullと空文字の要素を削除したいわけです。
こんな風に使います。


.. code-block:: scala

  scala> val list = Seq( "foo",null,"bar","","","baz")
  list: Seq[java.lang.String] = List(foo, null, bar, , , baz)

  scala> list filter{ pf.isDefinedAt } map{ pf }
  res14: Seq[Option[String]] = List(Some(foo), None, Some(bar), None, None, Some(baz))

  scala> list filter{ pf.isDefinedAt } map{ pf } flatten
  res15: Seq[String] = List(foo, bar, baz)


このように、Seq#filter( f: A => Boolean )でfilterする条件として、PartialFunciton#isDefinedAtを呼び出すようにして、PartailFunctionの条件に一致するものだけ、Seq#map[B]( f: A => B )の引数にPartailFunctionを渡すことで、条件に応じてmapされたSeqができます。


で、結果はSeq[Option[String]]なので、Seq#flattenを呼び出せばNoneが消えるという訳です。


さて、Scala2.8からは引数にPartailFunctionをもらって条件に応じた要素だけmapするTraversableLike#collect[B, That](pf: PartialFunction[A, B]): Thatという関数があります。


さきほどの処理は、もう少し簡単にするとこんな感じです。

.. code-block:: scala

  scala> val pf:PartialFunction[String,String] = { case s if s != null || s != "" => s }
  pf: PartialFunction[String,String] = <function1>

  scala> list.collect( pf )
  res16: Seq[String] = List(foo, bar, baz)

  scala> list collect { case s if s != null || s != "" => s }
  res17: Seq[java.lang.String] = List(foo, bar, baz)



おまけ。カッコイイ書き方
_______________________________________________

水島さんに教えてもらったんですが、以下のようなtype aliasを定義しておくと、"pf:String --> Option[String]"みたいにPartailFunctionがかっこよく書けますよ!


.. code-block:: scala

  scala> type -->[A,B] = PartialFunction[A,B]
  defined type alias $minus$minus$greater

  scala> val pf:String --> Option[String] = { case s if s == null || s == "" => None; case s => Some(s) }
  pf: -->[String,Option[String]] = <function1>

  scala> list collect pf flatten                                                                  res20: Seq[String] = List(foo, bar, baz)


Scalaの奇妙なFizzBuzz - PartialFunctionとimplicit conversionを添えて
________________________________________________________________________

PartialFunctionの利用例として、奇妙なFizzBuzzを書いてみましたよ。


.. code-block:: scala

  // PartialFunction[A,B]を "A --> B"のように書けるようにしておく
  type -->[A,B] = PartialFunction[A,B]

  // FizzBuzz用のPartailFunctionを生成するユーティリティ
  def toPF[A]( r:String )( f: A => Boolean): A --> String =
  { case v if f(v) => r }

  // FizzBuzzできるSeq
  class FizzBuzzSeq[A]( theSeq:Seq[A] ) extends Seq[A] {
    def apply(idx: Int):A = theSeq( idx )
    def length = theSeq.length
    def iterator = theSeq.iterator

    val defaultPF:A --> String = { case v => v.toString }

    def fizzBuzz( pfSeq:A --> String * ) =
      theSeq.collect( ( pfSeq :+ defaultPF ) reduceLeft { (a,b) => a orElse b } )
  }

  // implicit conversionでSeq[A]をFizzBuzzSeq[A]にする
  implicit def seqToFizzBuzz[A]( theSeq:Seq[A] ) = new FizzBuzzSeq( theSeq )

  // PatrialFunctionを渡してFizzBuzz
  // implicit conversionで(1 to 100)はFizzBuzzSeq[Int]になる
  (1 to 100 ).fizzBuzz(
    toPF( "FizzBuzz") {  v => v % 15 == 0 },
    toPF( "Fizz") {  v => v % 3 == 0 },
    toPF( "Buzz") {  v => v % 5 == 0 }
  ).foreach{ println }

  // こう書いても同じ
  (1 to 100 ).fizzBuzz(
    { case v if v % 15 == 0 => "FizzBuzz" },
    { case v if v % 3 == 0 => "Fizz" },
    { case v if v % 5 == 0 => "Buzz" }
  ).foreach{ println }


まず、type aliasでPartialFunction[A,B]を "A --> B"のように書けるようにしておきます。Intを受け取ってStringを返すPFは"Int --> String" と書けて関数リテラルっぽくてカコイイですよね?


.. code-block:: scala

  // PartialFunction[A,B]を "A --> B"のように書けるようにしておく
  type -->[A,B] = PartialFunction[A,B]


次に、返すべき結果をStringで受け取り、caseのifのパターンガードに適用する"A => Boolean"型の関数オブジェクトを受け取ってPFを生成するユーティリティを用意しますよ。


.. code-block:: scala

  // FizzBuzz用のPartailFunctionを生成するユーティリティ
  def toPF[A]( r:String )( f: A => Boolean): A --> String =
  { case v if f(v) => r }


これで"val pf3 = toPF("Fizz"){ v => v % 3 == 0 }"のようにPFを生成できると。


そいでもって、FizzBuzzできるクラスであるFizzBuzzSeqを定義します。コイツはFizzBuzz対象のSeq[A]を持っていて、fizzBuzzメソッドで引数にもらったPFをorElseで連結して、対象のSeq[A]のcollectにPFを渡してFizzBuzzさせます。


.. code-block:: scala

  // FizzBuzzできるSeq
  class FizzBuzzSeq[A]( theSeq:Seq[A] ) extends Seq[A] {
    def apply(idx: Int):A = theSeq( idx )
    def length = theSeq.length
    def iterator = theSeq.iterator

    val defaultPF:A --> String = { case v => v.toString }

    def fizzBuzz( pfSeq:A --> String * ) =
      theSeq.collect( ( pfSeq :+ defaultPF ) reduceLeft { (a,b) => a orElse b } )
  }


ポイントは、fizzBuzzメソッドの引数PFに一致しない場合のデフォルトを"val defaultPF:A --> String = { case v => v.toString }"で定義してあって、引数のpfSeq(可変長引数なのでArray[ A --> String]型)にくっつけた後、reduceLeftですべてのPFをorElseで連結した新しいPFを作っているとこですね。


foldLeftとかreduceLeftとか大好きすぎて生きてるのがツライ。


そして、対象のSeq[A]に前回紹介したcollect[B, That](pf: PartialFunction[A, B]): Thatを連結したPFを引数に呼び出すと、FizzBuzzされたSeq[String]ができるって寸法さぁ!


で、implicit conversionを利用してSeq[A]をFizzBuzzできるFizzBuzzSeq[A]型に変換するための関数を定義しておきますよ。これで"(1 to 100).fizzbuzz(...)"でPF渡してFizzBuzzできるようになりますね。


.. code-block:: scala

  // implicit conversionでSeq[A]をFizzBuzzSeq[A]にする
  implicit def seqToFizzBuzz[A]( theSeq:Seq[A] ) = new FizzBuzzSeq( theSeq )


実際にFizzBuzzするには、FizzBuzzするものを作ってそいつに対してfizzBuzzメソッドをPFを引数に呼び出せばOK。implicit conversionでFizzBuzzSeqに変換されたあとにfizzBuzzメソッドが呼ばれます。PFを作るには、さっき定義したtoPF[A]( r:String)(f:A => Boolean)でやってます。


.. code-block:: scala

  // implicit conversionで(1 to 100)はFizzBuzzSeq[Int]になる
  (1 to 100 ).fizzBuzz(
    toPF( "FizzBuzz") {  v => v % 15 == 0 },
    toPF( "Fizz") {  v => v % 3 == 0 },
    toPF( "Buzz") {  v => v % 5 == 0 }
  ).foreach{ println }

  // こう書いても同じ
  (1 to 100 ).fizzBuzz(
    { case v if v % 15 == 0 => "FizzBuzz" },
    { case v if v % 3 == 0 => "Fizz" },
    { case v if v % 5 == 0 => "Buzz" }
  ).foreach{ println }



