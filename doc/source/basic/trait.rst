トレイト(trait)
--------------------------------

トレイト(trait)によるMix-in
___________________________
トレイト(trait)は、Javaのインターフェースに似ており、一つのクラスが複数のtraitを継承できます。しかし、Javaのインターフェースと決定的に異なる点は、実装を定義できるということです。

traitを利用することにより、実装を再利用して既存のクラスに新しい機能を付加することができるようになります。

機能的にはRubyのモジュールとほぼ同じですが、traitの型もコンパイル時にチェックされますので、モジュールよりも型安全に利用することができます。

簡単な例を見てみましょう。helloという関数を持つEnglishというtraitと、"こんにちわ"という関数をもつJapaneseという二つのtraitを定義します。

.. code-block:: scala

  scala> trait English {
       |   val name:String
       |   def hello = println( "Hello," + name )
       | }
  defined trait English

  scala> trait Japanese {
       |   val name:String
       |   def こんにちわ = println("こんにちわ、" + name )
       | }
  defined trait Japanese

この2つのtraitを継承したPersonクラスを定義します。

.. code-block:: scala

  scala> class Person( val name:String ) extends English with Japanese
  defined class Person

Personクラスは、EnglishとJapaneseの2つのtraitから、「実装」を継承していることがわかると思います。

.. code-block:: scala

  scala> val p = new Person( "yuroyoro" )
  p: Person = Person@3e3a2536

  scala> p.hello
  Hello,yuroyoro

  scala> p.こんにちわ
  こんにちわ、yuroyoro

traitは、他のクラスやtraitを継承することができます。以下の例は、java.util.Dateを継承したtraitです。

.. code-block:: scala

  import java.util.Date
  trait EnglishDate extends Date{
    def enDate = this.toString
  }

  trait JapaneseDate extends Date{
    import java.text.SimpleDateFormat
    def jpDate =
      ( new SimpleDateFormat("yyyy年MM年dd日HH時mm分ss秒")).format( this )
  }

  class Clock extends EnglishDate with JapaneseDate

継承関係の直列化
___________________________

C++のような多重継承を許容する言語ですと、ダイヤモンド継承とよばれる問題が発生します。
2つのクラスAとBを継承するあるクラスCがあったとします。
AとBは、同じSクラスを継承しています。図にするとこうです。

.. code-block:: console

        S
        |
   +---------+
   |         |
   A         B
   +---------+
        |
        C

ここで、AとBはそれぞれSクラスのメソッドmをオーバーライドしていました。
さて、Cクラスに継承されるメソッドmは、AとBのどちらになるのでしょうか?

Scalaでは、このダイヤモンド継承問題に対して、Mix-inという手法と、継承の直列化で対応しています。

先ほどのダイヤモンド継承をScalaで書くとこのようになります。

.. code-block:: scala

  abstract clsss S { def m:String }
  trait A extends S { override def m:String = "A" }
  trait B extends S { override def m:String = "B" }
  class C extends A with B

では、早速実行してみましょう。どうなるでしょうか?

.. code-block:: scala

  scala> (new C).m
  res28: String = B

Bの実装が継承されています。これは、traitの継承関係が直列化されているからです。

つまり、class Cの継承関係を図にすると、このように解釈されているのです。

.. code-block:: scala

  C -> B -> A -> S

この"->"はextendsを表していると思ってください。traitは、withキーワードで指定された一番右から左へさかのぼって継承関係を一直線にするように解釈します。

では、先ほどのCクラスの宣言を変更して、class C extends B with Aとしたらどうなるでしょうか?

.. code-block:: scala

  scala>   class C extends B with A
  defined class C

  scala> (new C).m
  res29: String = A

今度はAの実装が継承されました。

ここで、traitをMix-inさせるためには、ある制約が発生します。あるクラスにMix-Inさせるtraitには、それぞれ共通のスーパークラスが必要だということです。

先ほどの例のBの継承先を全く異なるクラスTに変えてみるとどうなるか、試してみます。

.. code-block:: scala

  abstract class S { def m:String }
  abstract class T { def m:String }
  trait A extends S { override def m:String = "A" }
  trait B extends T { override def m:String = "B" }
  class C extends A with B

このままクラスCを宣言すると、以下のようにコンパイルエラーとなります。

.. code-block:: scala

  scala>   class C extends A with B with T
  <console>:18: error: illegal inheritance; superclass S
   is not a subclass of the superclass T
   of the mixin trait B
           class C extends A with B with T

これは、AとBの継承先のクラスが異なるためです。ここで、クラスTが抽象クラスではなく、traitになっている場合はエラーは発生しません。あくまで、具象クラスまたは抽象クラスを継承するtraitの場合です。

まとめると、こうなります。

* 複数のtraitをMix-Inするときは宣言する順番が重要。一番右から継承関係をさかのぼる。
* あるクラスを継承するtraitを複数withでMix-Inするためには、それぞれのtraitの継承先に共通のクラスが必要


