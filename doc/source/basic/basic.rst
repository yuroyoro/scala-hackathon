基本的な文法
----------------------------

変数
___________________________

変数の宣言
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Scalaの変数には、再代入できない(Javaでのfinal宣言に該当する)valと、再代入できるvarがあります。

以下のように、varで宣言した変数は再度別の値を代入できますが、valで宣言した変数に対して再度代入するとコンパイルエラーとなります。

.. code-block:: java

   var foo:String = "Foo" // var 変数名:型 = 値
   foo = "Bar"            // 代入できる
   val bar:String = "Bar" // val 変数名:型 = 値
   bar = "Baz"            // コンパイルエラーとなる

一般的には、再代入可能なvarは副作用の温床となるため、Scalaでは可能な限りvalを用いてプログラミングするのがよいと言われています。

Scalaの型階層
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Scalaでは、全ての型はオブジェクトです。Javaでは、プリミティブ型とオブジェクト型が区別されていますが、Scalaでは値型もオブジェクトとして扱えますので、数値に対してメソッドを呼び出すことが可能です。

全て型のTopレベルは、Any型になります。Scalaでは、数値も文字列もコレクションもユーザー定義のクラスも、全てAny型のサブクラスになります。

Scalaで用意されている型には、値型(AnyVal)と参照型(AnyRef)があります。

値型(AnyVal)は、プリミティブ型に相当するクラスで，真偽値(Boolean)や整数(Byte，Short，Char，Int，Long)や浮動小数点を表す(Float，Double)クラスが用意されています。これらの値型は、AnyVal型を継承しています。Javaのvoid型に相当するUnit型も、値型(AnyRef)のサブクラスです。

参照型(AnyRef)は、java.lang.Objectに相当し、文字列(String)やListやMapなどが該当します。参照型は、全てAnyRef型のサブクラスとなります。

これらの型階層をまとめると、以下のようになります。

.. code-block:: console

  Any                 全ての型のスーパークラス
   |- AnyVal          値型のスーパークラス
   |   |- Short
   |   |- Int
   |   |- Float
   |   |- Double
   |   |- Boolean
   |   |- Unit
   |   |- ...
   |
   |- AnyRef          参照型のスーパークラス
       |- String
       |- List
       |- ...|

また、Scalaには、Javaとは異なり、全ての型のサブタイプが存在します。Null型は、全てのAnyRef型に対してサブタイプの関係となり、Nothing型は全ての型に対してのサブタイプです。

詳しくは、水島さんが書いたこちらの記事を参照してください。

  ITPro:第6回 Scala言語を探検する（4）Scalaの型システム
  http://itpro.nikkeibp.co.jp/article/COLUMN/20090106/322252/

配列型、List、Map
^^^^^^^^^^^^^^^^^^^^^^^^^^
Javaと異なり、Scalaの配列であるArray型は、特別扱いされることのない通常のScalaオブジェクトです。 配列は、以下のように宣言して利用することができます。

.. code-block:: scala

  scala> val intArray:Array[Int] = Array(2,  3,  7,  11,  13)
  intArray: Array[Int] = Array(2,  3,  7,  11,  13)

  scala> intArray(2)
  res0: Int = 7

  scala> intArray(0) = 1

  scala> intArray
  res2: Array[Int] = Array(1,  3,  7,  11,  13)


Array型は、要素の型を型パラメータ(後述します)に取ります。Int型の配列なら、Array[Int]のように宣言します。

配列の初期化は、Array(要素1, 要素2, 要素3, ･･･)と書きます。

配列の要素の取り出しは、intArray(0)のように、()の中に取り出したい添え字を指定することで取り出せます。配列への代入は、intArray(0) = 1 のようにします。

  以下の内容は、ちょっと高度な話なので今は読み飛ばして構いません。
  Array(0, 1, 2)のような配列の初期化は、ArrayクラスのコンパニオンオブジェクトであるArrayオブジェクトのdef apply [ A  < : AnyRef ]( xs : A * ) : Array [ A ]によって実現されています。このように、コンパニオンオブジェクトにファクトリメソッドを実装してシンタックスシュガーを提供するような設計が、Scalaではよく行われます。
  intArray(0)のような()による呼び出しは、Arrayクラスのdef apply ( i : Int ) : A に変換されます。また、intArray(0) = 1はdef update ( i : Int ,  x : A ) : Unitになります。
  演算子オーバーロードや、()による呼び出しや代入を任意に実装できることが、Scalaの強力な機能であり、これを利用して制御構造の追加やDSLへの適用がScalaの特徴でもあります。


RichWrapper、よく使う型
^^^^^^^^^^^^^^^^^^^^^^^^^^^
一般的な値型と文字列型は、Javaのプリミティブ型を機能強化したラッパー型が用意されています。

RichIntやRichStringなど、それぞれの型に"Rich"を先頭に付与した名前になっています。これらのRichWrapperは、RichWrapperが持つ関数を呼び出したタイミングで、適切に変換される(implicit conversion)ため、特に意識しなくとも利用することができます。

例えば、RichStringには、文字列を反転させるreverseという関数があります。String型のオブジェクトに対してこのreverse関数を呼び出すと、RichStringに変換されて実行されます。

.. code-block:: scala

  scala> val s = "AbCdE"
  s: java.lang.String = AbCdE

  scala> s.reverse
  res2: scala.runtime.RichString = EdCbA



上記のRichWrapperに加え、Listなどのコレクション型も含め、よく使う型を以下の表にまとめておきます。

TODO 表を書く


制御構造
___________________________

Scalaに組み込みで用意されている制御構造はそれほど多くありません。Scalaでは、関数リテラルなどを駆使して、一見組み込みの制御構造に見えるような処理を、ユーザーが定義することが可能だからです。

用意されている制御構造は、条件分岐のif、繰り返しのwhileとfor、例外処理のためのtry、パターンマッチのmatchです。

ちなみに、breakやcontinueはありません。Scala2.8からは利用できるようになります。

ここでは、ifとwhileとfor式について説明します。

ifによる条件分岐
^^^^^^^^^^^^^^^^^^^^^^^^^^^
if文は、通常のプログラミング言語と同じように書くことができます。

.. code-block:: scala

  if( 条件 ){ 真の時の処理 } else { 偽の時の処理 }

条件に指定する式は、Boolean型に評価されるものである必要があります。例えばPythonのように、条件に空のリストを指定したりすることはできません。

条件につづく処理は、一行に収まるような場合は{}を省略することができます。

else以降には、条件が偽の場合の処理を書きます。scalaでは、elseifのような指定はできないため、if文をネストして書くことになります。

.. code-block:: scala

  val i = 20
  if( i % 2 == 0 ) println("偶数") else println("奇数")

なお、Scalaのif文は、値を返します。
以下の例は、if文の条件を評価した結果、実行された式(ブロック)を評価した結果(以下の例では"偶数"または"奇数"という文字列)が、変数resultに代入されます。

.. code-block:: scala

  val i = 20
  val result = if( i % 2 == 0 ) "偶数" else "奇数"

if文にelseがない場合は、if文の評価結果はUnit型になります。
Unit型とは、voidに相当する"値を返さないこと"を表す型です。

whileによる繰り返し
^^^^^^^^^^^^^^^^^^^^^^^^^^
他の言語と同じように、whileによる繰り返しを記述することができます。

.. code-block:: scala

  while( 条件 ) { 処理 }

条件に指定する式は、if文と同様にBooleanに評価される式である必要があります。
以下は、whileを利用した繰り返しの例です。

.. code-block:: scala

  var count = 0
  while( count <= 10 ){
    println( count )
    var count = count + 1
  }

whileによる繰り返しは、varでの再代入可能な変数を条件に利用することが多く、できる限りvalを利用してプログラミングするというscalaの思想にはそぐわない場合があります。

実際、繰り返し処理をScalaで書く場合は、この後に説明するfor式を利用することの方が多いです。

標準入力やSocketからの入力待ちなど、blockingされるような呼び出しを利用する場合などが、whileの使いどころでしょうか?

for式による繰り返し(基本)
^^^^^^^^^^^^^^^^^^^^^^^^^^
繰り返し処理を記述するもう一つの方法は、for式を利用することです。

for式は、Javaの拡張for文に似ています。

.. code-block:: scala

  for( 一時変数 <- リストや配列 ){ 繰り返し処理 }

具体的に、先ほどのwhileをfor式で書くと、こんな感じになります。

.. code-block:: scala

  for( n <- 0 to 10 ){ println( n ) }

"0 to 10"と書いてありますが、これはなんでしょうか?

実は、これはRichInt型の"to"メソッドを呼び出して、"0から10までの範囲"を表すイテレータ(Range)を生成しているのです。以下のように書いても同じです。

.. code-block:: scala

  scala> val range = (0).to(10)
  range: Range.Inclusive = Range(0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  10)

  scala> for( n:Int <- range ){ println( n ) }


for式による繰り返し(ifによるfiler)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
for式は、さらに強力な機能を持っています。それは、繰り返しに条件を指定できると言うことです。
以下のように書くと、0から10の繰り返しの中で、偶数の場合のみ処理が実行されます。

.. code-block:: scala

  scala> for( n <- 0 to 10 if n % 2 == 0 ){ print( n + ", ") }
  0, 2, 4, 6, 8, 10,

ifは、複数指定することもできます。その場合は;(セミコロン)で区切ります。

.. code-block:: scala

  scala> for( n <- 0 to 10 if n % 2 == 0;if n % 4 == 0 ){ print( n + ", ") }
  0, 4, 8,

for式による繰り返し(一時変数)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
さらに、for式の中で、一時的に変数を定義して利用することができます。
以下の例では、for式の()のなかで、;(セミコロン)で区切って変数mを定義して、{}内の処理で利用しています。

.. code-block:: scala

  scala> for( n <- 0 to 10 ; m = n * 10 ){ print( m + ", ") }
  0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100,



for式による繰り返し(複数の要素の繰り返し)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Int型の配列が2つあり、それらを同時に繰り返し処理させたい場合は、どのように書くのでしょうか?
Javaの場合、以下のようにfor文を入れ子にするコードになりそうです。

.. code-block:: java

  int [] xs = {0, 2, 4, 6, 8};
  int [] ys = {1, 3, 5, 7, 9};
  for(int i ; i < xs.length; i++ ){
    for(int j; j < ys.length; j++ ){
      System.out.println( xs[i] + ":" + ys[j] );
    }
  }

Scalaでは、どのようになるのでしょうか?
もちろん、for式を入れ子にしてもよいのですが、もっと簡単に書く方法があります。

.. code-block:: scala

  scala> val xs = List(0, 2, 4, 6, 8)
  xs: List[Int] = List(0,  2,  4,  6,  8)

  scala> val ys = List(1, 3, 5, 7, 9)
  ys: List[Int] = List(1,  3,  5,  7,  9)

  scala> for( x <- xs; y <- ys ){ println( x + ":" + y ) }


インタプリタで、上記のコードを実行してみましょう。偶数の配列xsと奇数の配列ysを入れ子のfor文で出力した場合と、全く同じ結果になるでしょう。

Scalaのfor式では、()内に指定する"一時変数 <- 配列etc"(generatorといいます) を;(セミコロン)で区切っていくつも指定することができ、指定した順序で入れ子に繰り返しが実行されるのです。


for式による繰り返し(Listの生成)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
if文が値を返すことができるように、for式も評価した結果をリストにすることができます。つまり、for式の結果を変数に代入しておくことが可能なのです。

Pythonをご存じの方は、リスト内包表記というとなじみが深いでしょうか。

以下の例のように、for式の()と{}の間に"yield"と指定することで、for式は各繰り返しの結果を集めたListを生成します。

.. code-block:: scala

  scala> val xs = List( 1, 3, 5, 7 )
  xs: List[Int] = List(1,  3,  5,  7)

  scala> val xs2 = for( n <- xs ) yield { n * 2 }
  xs2: List[Int] = List(2, 6, 10, 14)

奇数のListであるxsの各要素を、2倍したListが変数xs2に代入されていることがわかると思います。
yieldを指定した場合、その後に指定したブロックの評価結果を集めたList(かならずしもList型とは限らないのですが･･･)が、for式を評価した結果となります。

関数
_________________

関数の宣言
^^^^^^^^^^^^^^^^^^^^^^^
Scalaでの関数宣言は、以下のような形式になります。

.. code-block:: scala

  def 関数名[型パラメータ]( 引数名:引数型,･･･ ):結果型 = {
    関数本体
  }

以下の例は、Int型の引数を2つとって足し算する関数です。

.. code-block:: scala

  def add1( n:Int, m:Int ):Int = { n + m }
  def add2( n:Int, m:Int ):Int =  n + m
  def some1():Unit = { println("Something") }
  def some2:Unit = { println("Something") }


関数の本体に、returnがないことに気がつきましたか?

Scalaでは、関数中で最後に評価された式の値が、関数の結果にはなります。

また、add2のように、関数の本体が一つの式である場合は、{}を省略することができます。

引数を取らない関数では、some2のように引数宣言の()そのものを省略できます。ただし、その関数が何らかの副作用(コンソールやファイルの出力、var変数の書き換えetc)を伴う場合は、引数を取らなくとも()をつけて宣言することが推奨されています。

また、注意しなければならないのは、Scalaでは関数は何らかのクラスやオブジェクトのメンバである必要があります。

Scalaインタプリタ上では、その場でdef fooのように関数を定義しても構いませんが、コンパイルするソースコード内では、classやobjectの中で関数が宣言されていないとコンパイルエラーとなります。

型推論
_________________

ScalaがJavaに比べて少ないタイプ数でコードを書けるのは、強力な型推論をScalaコンパイラがコンパイル時に実行して、明示的に変数や関数の結果型を宣言せずとも、コンパイラが推論してくれるからです。

型の宣言を省略できるのは、変数宣言での型指定と、関数の結果型の型指定です。

以下の例は変数の型指定を省略した場合です。インタプリタよって、変数の型が適切に推論されている様子がわかると思います。インタプリタ上だけではなく、scalacコマンドでコンパイルするときも、同様にコンパイラが省略されている型を推論します。

.. code-block:: scala

  scala> val s = "Foooooo!!"
  s: java.lang.String = Foooooo!!

  scala> val i = 10
  i: Int = 10

  scala> val l = 10000000L
  l: Long = 10000000

  scala> val b = true
  b: Boolean = true

  scala> val list = List("a", "b", "c")
  list: List[java.lang.String] = List(a,  b,  c)

List型の型パラメータも、推論の対象になっていることがわかりますね。

関数宣言での、結果型も省略可能です。

.. code-block:: scala

  scala> def add( n:Int,  m:Int ) = n + m
  add: (Int, Int)Int

結果型の省略では、注意しなければならないことが２つあります。

一つは、再帰で呼び出される関数の結果型の宣言は省略できないと言うことです。
以下の例のproduct10関数は、自身を再帰で呼び出してますが、結果型を明示していないためにコンパイルエラーとなっています。
def product10( n:Int ):Int = {...}のように、結果型を明示することでエラーがなくなります。

.. code-block:: scala

  scala> def product10( n:Int ) = {
       |   if( n < 10 ) product10( n * n )
       |   else n
       | }
  <console>:13: error: recursive method product10 needs result type
           if( n < 10 ) product10( n * n )
                      ^

もう一つの注意点は、if文などで関数が条件によって異なる型を返す場合は、それらの型の共通のスーパークラスが推論した結果になる、ということです。

この説明だけではわかりにくいと思いますので、以下の例を見てください。

.. code-block:: scala

  def anything( n:Int ) = {
    if( n > 0 ) 10L
    else "Foo"
  }

このanything関数の結果型は、Intでしょうか?それともStringでしょうか?

結果は、Any型になります。

上記の例は自明ですが、List型などを利用する時には注意が必要です。

.. code-block:: scala

  def anyList( n:Int ) = {
    if( n > 0 ) List( 1, 2, 3)
    else List("a", "b", "c")
  }

  val l:List[String] = anyList( -1 )

上記のanyList関数の結果型はList[Any]となるため、List[String]型の変数への代入はコンパイルエラーです。


