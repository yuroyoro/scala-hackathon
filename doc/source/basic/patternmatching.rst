ケースクラスとパターンマッチ
--------------------------------

パターンマッチは、構文としてはswitch-case文に似ていますが、ケースクラスを併用することで、条件分岐を記述する強力な機能となります。

ここでは、Scalaでのパターンマッチとケースクラスの活用方法について説明します。

パターンマッチ
___________________________

パターンマッチの基本
^^^^^^^^^^^^^^^^^^^^^^^^^^^

パターンマッチの構文は"match"キーワードを使います。

.. code-block:: scala

  変数 match {
    case パターン => { 処理 }
    case ...
    case _ => { どのパターンにも当てはまらない場合 }

単純な例です。文字列をマッチさせてみます。

.. code-block:: scala

  scala> "Foo" match {
       |   case "Foo" => println("match")
       |   case _ => println("ng")
       | }
  match


型に対するマッチ
^^^^^^^^^^^^^^^^^^^^^^^^^^^

型に対してマッチさせることもできます。パターンの部分に"変数名:型"と書きます。
instanceOfでswitch-caseさせるイメージだと思ってください。

.. code-block:: scala

  scala> def numberCheck( n:AnyVal ) = n match {
       |   case b:Boolean => println( "boolean" )
       |   case i:Int => println( "int" )
       |   case _ => println("other")
       | }
  numberCheck: (AnyVal)Unit

  scala> numberCheck( true )
  boolean

  scala> numberCheck( 4 )
  int

条件指定のパターン
^^^^^^^^^^^^^^^^^^^^^^^^^^^
パターンにマッチするときに、さらにifで条件を指定して絞り込むことができます。先ほどの例で、Int型にマッチした場合で、さらに0以上の場合という条件をつけるとこうなります。

.. code-block:: scala

  scala> def numberCheck( n:AnyVal ) = n match {
       |   case b:Boolean => println( "boolean" )
       |   case i:Int if i >= 0 => println( "int" )
       |   case _ => println("other")
       | }
  numberCheck: (AnyVal)Unit

  scala> numberCheck( true )
  boolean

  scala> numberCheck( 4 )
  int

  scala> numberCheck( -1 )
  other


マッチ変数への束縛
^^^^^^^^^^^^^^^^^^^^^^^^^^^
先ほどの例で、"case b:Boolean"や"case i:Int"とパターンを書きました。このiやbは、マッチ用の変数で、そのパターンにマッチした場合に、マッチした値が束縛される変数のことです。

ここで束縛された変数は、そのあとのifによる条件の絞り込みや、マッチした場合の処理の中で参照して利用することができます。

.. code-block:: scala

  scala> def numberCheck( n:AnyVal ) = n match {
       |   case b:Boolean => println( "boolean:" + b)
       |   case i:Int if i >= 0 => println( "int:" + i)
       |   case x => println("other:" + x)
       | }
  numberCheck: (AnyVal)Unit

  scala> numberCheck( true )
  boolean:true

  scala> numberCheck( 4 )
  int:4

  scala> numberCheck( -1 )
  other:-1

なお、パターンは記述した順序に評価され、最初にマッチしたパターンが適用されます。また、実行時にどのパターンにもマッチしなかった場合は、MatchErrorが発生します。


ケースクラス
___________________________

ケースクラスの定義
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
caseクラスとは、パターンマッチに使用できるクラスのことです。"case class Foo"のように、クラス宣言に"case"をつけます。

その他は通常のクラスと同様で、abstractにできますし、関数やフィールドを定義することもできます。

.. code-block:: scala

  abstract case class Sexuality
  case class Man( name:String ) extends Sexuality {
    override def toString = "Mr." + name
  }
  case class Woman( name:String ) extends Sexuality{
    override def toString = "Ms." + name
  }


ケースクラスの特徴
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

ケースクラスには、通常のクラスと異なるいくつかの特徴があります。

* ケースクラスのインスタンスはnewキーワードではなく、単純にクラス名でよい。
  "new Man("ozaki" )"ではなく、"Man("ozaki")"でインスタンスが作られる。
* ケースクラスのコンストラクタに指定された引数は、valが付与されているものとして扱われる。
  つまり、コンストラクタ引数名の読み取り専用フィールドが追加される。
* toString, hashCode, equalをコンパイラが適切に実装して追加する。
  つまり、"Man("ozaki") == Man("ozaki") "は、異なるインスタンスを参照しているが"=="での比較はtrueとなる

.. code-block:: scala

  scala> val man = Man( "Ozaki" )
  man: Man = Mr.Ozaki

  scala> man.name
  res11: String = Ozaki

  scala> man.name = "Tomohito"
  <console>:17: error: reassignment to val
         man.name = "Tomohito"
                  ^
  scala> val man2 = Man("Ozaki")

  scala> man = man2
  res12: Boolean = true

このような特徴から、caseクラスを利用するとイミュータブルなオブジェクトを簡単に定義できます。JavaBeanの代わりに、Scalaではケースクラスを多用します。

ケースクラスとパターンマッチ
________________________________

パターンからケースクラスを分解する
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

さて、やっとパターンマッチとケースクラスを組み合わせた、Scalaの強力な条件分岐の説明に入ります。

先ほどの例で利用したMan, Womanを利用してパターンマッチを行ってみます。


.. code-block:: scala

  def printSexuality( s:Sexuality ) =
    s match {
      case Man( n )   => println( "%sさんは男性です。".format( n ) )
      case Woman( n ) => println( "%sさんは女性です。".format( n ) )
  }

このコードのcaseの中で、Man( n )と記述されています。このパターンは、sがManクラスだった場合に、Manクラスを構築する際のコンストラクタ引数nameを変数nに束縛するということです。

つまり、SがMan("Ozaki")だった場合は、変数nに"Ozaki"が格納されます。

例では、コンストラクタ引数が同じ数のパターンでしたが、異なる引数のパターンも可能です。
新しく、Sexualityクラスのサブクラスとして、Otherケースクラスを作成してみます。

.. code-block:: scala

  case class Other( name:String, kind:String ) extends Sexuality

  def printSexuality( s:Sexuality ) =
    s match {
      case Man( n )   => println( "%sさんは男性です。".format( n ) )
      case Woman( n ) => println( "%sさんは女性です。".format( n ) )
      case Other( n , k) => println( "%sさんは%sです。".format( n, k ) )
  }

では、OtherケースクラスをこのprintSexuality関数に渡してみましょう。

.. code-block:: scala

  scala> val o = Other( "まりや", "男の娘" )
  o: Other = Other(まりや, 男の娘)

  scala> printSexuality( o )
  まりやさんは男の娘です。

それぞれコンストラクタ引数が異なるケースクラスでもパターンマッチできていて、複数の変数を束縛できていますね。

さて、この束縛する変数なのですが、値が何でもよい場合は"_"を指定することで、いわばワイルドカードのような指定をすることが可能です。

case Other(n , _ ) と書くと、Otherクラスのnameがnに束縛され、kindは問わない、というパターンになります。

ケースクラスによるパターンマッチは、いわばパターンからケースクラスを分解して構造チェックしているともいえます。

実際、ネストしたケースクラスへのマッチを可能とします。

.. code-block:: scala

  scala> case class Foo( s:String )
  defined class Foo

  scala> case class Bar( i:Int, foo:Foo )
  defined class Bar

  scala> val bar = Bar( 99 , Foo( "hoge") )
  bar: Bar = Bar(99,Foo(hoge))

  scala> bar match {
       |   case Bar( n , Foo( t )) => println( "Bar %s %s".format( n , t ) )
       |   case _ =>
       | }
  Bar 99 hoge

Barケースクラスの中のFooケースクラスの中身まで、"Bar( n, Foo(t))"というパターンで分解できていますね。

Listのパターンマッチ
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

List型は、以下の例のように"::"(セミコロン二つ)"で要素を結合することでも構築できます。

.. code-block:: scala

  scala> val l = "Foo"::"Bar"::"Baz"::Nil
  l: List[java.lang.String] = List(Foo,  Bar,  Baz)

最後の要素には必ずNil(空リスト)をつける必要があります。

さて、この"::"(セミコロン二つ)"は、パターンとしても使えます。

.. code-block:: scala

  scala> l match {
       |   case x::xs => println(  "先頭は%sで残りは%d個です。".format( x ,xs.length ) )
       |   case Nil => println( "空リストです。" )
       |   case _ =>
       | }
  先頭はFooで残りは2個です。

"x::xs"というパターンで、Listを先頭要素とそれ以外のListに分割できます。この形のパターンマッチは、List型を再帰処理で次々に受け渡していくような時に利用されます。

ちなみに、Nilは空リスト(List())にマッチします。

Tupleのパターンマッチ
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Tupleのパターンマッチは簡単です。case ( t1, t2)と書くだけです。

.. code-block:: scala

  scala> t match {
       |   case ( t1 ,t2 ) => println( "%sと%dのタプル".format( t1,t2 ) )
       | }
  aと1のタプル

パターンマッチによる代入とfor
________________________________

パターンマッチは、matchで条件分岐させるとき以外にも活用できます。

パターンマッチによる代入
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

代入の左辺には、パターンを使うことができます。

この例は、Listパターンで先頭と残りに分割して代入したり、先ほど作ったManクラスで名前だけ代入したりしています。

.. code-block:: scala

  scala> val headElem::tailList = List("a","b","c","d")
  headElem: java.lang.String = a
  tailList: List[java.lang.String] = List(b, c, d)

  scala> val name = Man( "Tomohito")
  name: Man = Mr.Tomohito


forによる代入
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

for式では、for( 変数 <- Listなど )という形で繰り返し毎の変数を利用できましたが、この"変数"の部分にパターンを利用することができます。

先ほどのManクラスのArrayをforで繰り返すときの例です。パターンを利用して、nameを取り出しています。

.. code-block:: scala

  scala> val men = Array( Man( "Ozaki") ,Man("Tanaka"),Man("Yoshida") )
  men: Array[Man] = Array(Mr.Ozaki, Mr.Tanaka, Mr.Yoshida)

  scala> for( Man( name ) <- men ){ println( name ) }
  Ozaki
  Tanaka
  Yoshida

これは、Mapをfor式で処理する場合にもよく使います。Mapの個々の要素は(key, value)というTupleですので、Tupleパターンを使ってfor式で受けています。

.. code-block:: scala

  scala> val map = Map( "a" -> 1 , "b" -> 2 ,"c" -> 3 )
  map: scala.collection.immutable.Map[java.lang.String,Int] = Map(a -> 1, b -> 2, c -> 3)

  scala> for( (k,v) <- map ){ println( k + ":" + v ) }
  a:1
  b:2
  c:3
