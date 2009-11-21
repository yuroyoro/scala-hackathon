関数編
----------------------------
Scalaの関数は、ファーストクラス関数です。関数オブジェクトを変数に代入したり、"関数を生成する関数"(高階関数)を定義したり、クロージャとして利用することができます。

関数の宣言については先ほど説明しましたので、具体的にScalaの関数が持つ強力な機能をこれから紹介します。


関数の呼び出し
___________________________

Scalaで関数を呼び出すには、Javaなどと同じように、関数を持っているオブジェクトに.(ドット)をつけて呼び出します。

.. code-block:: scala

  scala> val s = "Foo"
  s: java.lang.String = Foo

  scala> s.charAt(0)
  res16: Char = F

引数を取らない関数を呼び出す場合、()を省略できます。ただし関数の宣言でdef foo = {...}のように予め()なしで宣言されている関数を呼び出すときには、()をつけて呼び出すとエラーになります。

.. code-block:: scala

  scala> s.length()
  res17: Int = 3

  scala> s.length
  res18: Int = 3

引数なしまたは1引数の関数を呼び出す場合、.(ドット)の変わりにスペースを利用することができます。

.. code-block:: scala

  scala> s.length()
  res17: Int = 3

  scala> s length
  res19: Int = 3

１引数の関数を呼び出す場合、引数の指定で()を利用する変わりに、スペースの後に引数を指定することでも呼び出すことが可能です。

.. code-block:: scala

  scala> s.charAt(2)
  res20: Char = o

  scala> s charAt 2
  res21: Char = o

  scala> (s.charAt(2)).toLong
  res39: Long = 111

  scala> s charAt 2 toLong
  res40: Long = 111

このような記述が許されているため、ScalaではDSLのように自然言語に近い形の記述ができるようになっているのです。


ファーストクラス関数
___________________________

Scalaの関数は、ファーストクラスオブジェクトです。これは、関数をオブジェクトとして扱い、変数に"関数オブジェクト"を代入したり、関数の引数に"関数オブジェクト"を渡して呼び出し先の関数から"関数オブジェクト"を呼び出す、"関数オブジェクト"を生成する関数を定義する、などが可能になる、ということを意味します。

具体的な例を見てみましょう。

.. code-block:: scala

  scala> def isEven( n:Int ):Boolean = { n % 2 == 0 }
  isEven: (Int)Boolean

  scala> val func = isEven _
  func: (Int) => Boolean = <function>

  scala> func( 3 )
  res4: Boolean = false

最初に、"isEven"というInt型の引数を一つ取る関数を定義しています。

次に、この関数の"関数オブジェクト"を、funcという変数に代入しています。
関数名のあとにスペースを空けて、"_"(アンダースコア)を書くと、その関数のオブジェクトを取り出すことができるのです(正確には、部分適用された関数ですが)。

最後に、func変数が保持している関数(実態はisEven関数)に引数3を渡して呼び出しています。

関数の"型"
___________________________

先ほどの例で、func変数に代入した際に、以下のように出力されました。この出力は何を表しているのでしょうか?

.. code-block:: scala

  func: (Int) => Boolean = <function>

この"(Int) => Boolean"は、その関数の"型"を表しています。この表記の意味は、Int型の引数をひとつ取り、Boolean型の結果を返す関数であることを表現しているのです。

IntとStringの二つの引数を取って、List[String]型の結果を返す関数は、"(Int,String) => List[String]となりますし、String型の引数を取って何も返さない(Unit型)関数は、"(String) => Unit"となります。

関数を引数に取る関数
___________________________

関数はオブジェクトですので、ある関数の引数に"関数"を渡すことができます。具体的には、どのように定義するのでしょうか。以下の例を見てください。

.. code-block:: scala

  def boolean2String( f:(Int) => Boolean,  n:Int ) = {
    if( f( n ) ) "偶数" else "奇数"
  }

このboolean2String関数の引数の宣言に注目してください。第1引数が"f:(Int) => Boolean"と書かれています。
これは「fという引数の型は(Int) => Booleanである。」という意味です。つまり第一引数fは、Int型の引数をひとつ取ってBoolean型の結果を返す関数オブジェクトである、ということになります。

引数名の後の:(コロン)以降は、その引数の型を指定することを思い出してください。単純に、引数fの型を、先ほど説明した"関数の型"の表記で指定しているだけなのです。

では、このboolean2Stringを呼び出してみましょう。以前に定義したisEven関数を第1引数に渡すことにします。

.. code-block:: scala

  scala> val func = isEven _
  func: (Int) => Boolean = <function>

  scala> boolean2String( func ,  3 )
  res6: java.lang.String = 奇数

  scala> boolean2String( isEven _ ,  3 )
  res7: java.lang.String = 奇数

  scala> boolean2String( isEven  ,  3 )
  res8: java.lang.String = 奇数


関数リテラル
___________________________

先ほどの例では、一度"def"キーワードを利用してisEven関数を定義して、変数funcに代入するということを行いました。ただ、わざわざ名前をつけて定義するまでもない、使い捨ての関数をその場で定義したい場合もあるでしょう。関数の宣言と代入を、一度に行う方法はないのでしょうか?

その答えが、関数リテラルです。文字列は、その場で"foo"のように生成できて、わざわざ"new String"aaa")"なんて書きませんよね?
これと同じことを、関数リテラルは実現します。

関数リテラルは、このような書式で記述します。

.. code-block:: scala

  (引数1:型, 引数2:型, ･･･) => { 処理 }

最初の()の中身は、defで関数を宣言する場合の引数の定義と同じです。
その引数の宣言の()のあとに"=>"(rocket-dashというらしいです)を書いて、その後に関数の具体的な処理を記述する形になります。

結果型を宣言する必要はありません。結果型は、記述した処理の内容から型推論されるためです。

具体例を見てましょう。以前定義したisEven関数を関数リテラルで変数fretに代入してみます。

.. code-block:: scala

  scala> val fret:(Int) => Boolean = (n:Int) => { n % 2 == 0 }
  fret: (Int) => Boolean = <function>

  scala> val fret = (n:Int) =>  n % 2 == 0
  fret: (Int) => Boolean = <function>

この２つのコードは、どちらも同じことを行っています。
最初の書き方は、きちんと変数fretの型が"(Int) => Boolean"型であることを宣言したうえで、 =の後に関数リテラルで引数nの値が偶数の場合Trueを返す"無名関数"をfretに代入しています。

2番目の書き方は、引数fretの型の宣言を省略し(型推論されるので)、関数リテラルで無名関数を定義しています。関数本体が一行で収まる内容のため、さらに{}も省略してこのような形になっています。

無名関数を受け取る左辺の変数に、"関数の型"が指定されている場合は、さらに関数リテラルで指定する引数の型指定を省略することができます。

.. code-block:: scala

  scala> val fret:(Int) => Boolean = ( n ) => { n % 2 == 0 }
  fret: (Int) => Boolean = <function>

  scala> val fret:(Int) => Boolean = n =>  n % 2 == 0
  fret: (Int) => Boolean = <function>

最初の書き方は、引数nの型指定が省略されていますね。2番目の書き方は、()と{}を省略したものです。このように、引数の型指定を書略する書き方は、関数の引数に関数リテラルで生成した関数を渡すときによく利用されます。

さてここで、"1関数を引数に取る関数"としてboolean2String関数を作ったことを思い出してください。このboolean2String関数の引数に、関数リテラルで無名関数を渡すと、このような書き方になります。

.. code-block:: scala

  scala> boolean2String( (n:Int) => n % 2 == 0 ,  9 )
  res11: java.lang.String = 奇数

第1引数が、関数リテラルの形になっていることに注意して見てください。

.. code-block:: scala

  scala> boolean2String( n => n % 2 == 0 , 3)
  res12: java.lang.String = 奇数

この例は、関数リテラルの引数の型指定を省略したものです。かなり短くなりましたね。

この記述は、この後説明する引数のプレースホルダーを利用することで、もっと短く書くことができるようになります。

なお、ScalaのコレクションAPIは、関数を引数にとることが多いです。コレクションAPIを利用する際には、このように関数リテラルでその場で関数を定義して引数に渡すということがよく行われます。

引数のプレースホルダー
___________________________

defで関数を宣言するときも、関数リテラルを利用して無名関数を定義する際にも、引数は"引数名:型"の形になっていました。

関数リテラルを利用して無名関数を定義するときに限り、この"引数名"の宣言すら省略することができるのです。

引数名を省略したとしたら、関数本体ではどのように引数に渡された値を受け取ればよいのでしょうか?
プレースホルダーを利用することで、"無名引数"とでもいうべき変数が利用できるようになるのです。

.. code-block:: scala

  scala> val fret:(Int) => Boolean = _ % 2 == 0
  fret: (Int) => Boolean = <function>

この例は、プレースホルダーを利用して、引数名の宣言なしにisEven関数を定義したものです。以前はnという名前の引数担っていた部分が、"_"(アンダースコア)になっていますね。

引数名を省略した場合は、変わりに"_"(アンダースコア)で渡された引数の値を受け取ることができるのです。

引数が2つある関数はどうでしょうか? Int型の引数を2つとって、足し算した結果を返す関数を、プレースホルダーを利用して作成してみます。

.. code-block:: scala

  scala> val add:(Int, Int) => Int = { _ + _ }
  add: (Int,  Int) => Int = <function>

  scala> add( 7,  11 )
  res12: Int = 18

引数が2つある場合でも、"_"(アンダースコア)を2つ書くことで、最初の"_"には1番目の引数が、次の"_"には2番目の引数が渡されます。

さて、ここで引数をプレースホルダーで受け取る場合に注意しなければならないことが2つあります。

1. 変数に代入する場合などは、受け取り側の変数には"関数の型"を指定する必要があります。
   先ほどの例では、add変数には型が指定されていました。
2. 関数本体の処理の中で、"_"を利用できるのは1回のみ。
   2つの引数をプレースホルダーで受ける場合を考えて見ると、2番目に登場した"_"には2番目の引数が入ります。"_"が登場するたびに、順番に引数が渡されていくのです。

他にも、"_"が一意に特定できる状況でなければコンパイルエラーになります。

実際に、このようなプレースホルダーを利用した無名関数は、どのような局面で利用されるのでしょうか?

よく使われるのが、ScalaのコレクションAPIを利用する時に、関数を引数に取る関数を呼び出すときに、関数リテラルとプレースホルダーを利用した無名関数を引数として渡して呼び出す方法です。

List型のfilter関数は、Boolean型を返す関数を受け取って、その関数を各要素に適用してtrueを返した要素を集めたListを返す関数です。
以下の例は、List[String]型のListに対して、filter関数で「文字列の長さが2以上ならtrueを返す関数」を引数に渡して実行した例です。"_.length"は、String型のlengthメソッドを呼び出しているのです。

.. code-block:: scala

  scala> val l = List( "a", "bb", "ccc", "d", "ee", "fff")
  l: List[java.lang.String] = List(a,  bb,  ccc,  d,  ee,  fff)

  scala> l.filter( _.length >= 2 )
  res13: List[java.lang.String] = List(bb,  ccc,  ee,  fff)


高階関数とクロージャ
___________________________

「関数を生成する」関数
^^^^^^^^^^^^^^^^^^^^^^^^^^

高階関数とは、関数を引数として受け取ったり、関数を生成して返す関数のことです。先ほど定義したboolean2String関数は関数を引数にもらいましたね?今度は、関数を生成する関数を定義してみましょう。

例として、「Int型の引数nを取って、"文字列をn回繰り返す関数"を生成して返す」関数を作成してみます。


.. code-block:: scala

  scala> def repeat( n:Int ) = { ( s:String ) => s * n }
  repeat: (Int)(String) => String

これが、関数の定義です。{}で囲まれた部分が、関数リテラルになっていますね。このrepeat関数を呼び出すと、引数に与えた数だけ文字列を繰り返して返す関数が、戻り値として返されます。

では、このrepeat関数を実際に利用してみましょう。以下の実行例を見てください。

.. code-block:: scala

  scala> val repeat3 = repeat(3)
  repeat3: (String) => String = <function>

  scala> repeat3( "Foo" )
  res7: String = FooFooFoo

  scala> repeat( 4 )("Bar")
  res8: String = BarBarBarBar

最初の呼び出しでは、repeat関数の引数に3を与えて、生成された「文字列を3回繰り返す関数」を変数repeat3に代入しています。次の呼び出して、変数repeat3に代入された関数を呼び出しています。変数に格納されている関数オブジェクトも、通常の関数呼び出しと同様に(引数)をつけて呼び出すことができることを思い出してください。

最後の例は、repeat関数で生成された関数を変数に格納せず、その場で呼び出している例です。

クロージャ
^^^^^^^^^^^^^^^^^^^^^^^^^^^

先ほどの例で使用したrepeat関数の本体の中で、生成される関数はrepeat関数に渡された引数を利用していました。

ということは、repeat関数で生成された関数は、生成されたときに渡されたrepeat関数の引数を覚えていて、その後もずっと利用できることになります。

このように、生成されたときに外部の変数(repeat関数の例では引数n)を取り込んで動作する関数のことを"クロージャ"といいます。Java7に入る入らないでモメた、アレのことです。

そして、取り込まれる外部の変数のことを、自由変数といいます。

以下の例は、引数で渡した文字列より長いものをString型を要素に持つリストから探して、どのくらい超えているかと合わせて表示する関数を返しています。

.. code-block:: scala

  def printOverlength( s:String ) = {
    val len = s.length

    (xs:List[String]) => {
      for( e <- xs ;if e.length > len ){
        println( e + ":" + (e.length -len ) )
      }
    }
  }

この例ですと、lenが自由変数になり、List[String]を受け取ってfor式で繰り返し処理をしている部分がクロージャということになります。実行例は、以下の通りです。


.. code-block:: scala

  scala> val printOverFoo = printOverlength( "Foo" )
  printOverFoo: (List[String]) => Unit = <function>

  scala> printOverFoo( List( "Bar", "Baaaaz", "Aaa", "Hoge") )
  Baaaaz:3
  Hoge:1

実は、このprintOverlength関数は、もう少し短く書くことができます。余裕があったら、どのようになるか考えてみるとよいでしょう。


ネストした関数
___________________________

Javaでは、メソッドの中でメソッドを宣言することはできませんでした(匿名クラスを定義することはできますが)。Scalaでは、関数の処理の中で、関数を宣言することができます。

例を見てみましょう。これは、String型を要素に持つListの各要素を、指定した文字列が含んでいたら"アリ"、含まないなら"ナシ"という文字列に変換したListを返す関数です。

.. code-block:: scala

  def containsOfString( xs:List[String], s:String ) = {
    def convert( e:String ) =
      if( e contains s ) "アリ" else "ナシ "

    for( e <- xs ) yield { convert( e ) }
  }

関数の内部で、defキーワードを利用してconvert関数を定義しています。このconvert関数は、実は自由変数としてcontainsOfString関数に渡された引数sを取り込んだ、クロージャになっています。

以下は実行結果です。

.. code-block:: scala

  scala> containsOfString( List("Foooo", "AFoo", "Booo") ,  "Foo" )
  res19: List[java.lang.String] = List(アリ,  アリ,  ナシ )


ちなみに、このcontainsOfString関数ですが、for式を使わないで以下のように書くこともできます。

.. code-block:: scala

  def containsOfString = xs.map{ e => if( e contains s ) "アリ" else "ナシ" }

List型(正確には"Iterable型")が持つmap関数に変換する無名関数を渡しています。for式で処理するより、関数のレシーバーが走査対象のListオブジェクトになっている分、何に対してどのような操作を適用するのかが明確になるため、筆者はこのような書き方の方が好みです。


再帰する関数
___________________________

Scalaでの再帰関数
^^^^^^^^^^^^^^^^^^^^^^^^^^^
再帰といえば、関数の中で自身を再度呼び出すことです。再帰からの脱出条件を間違えてStackOverflowを引き起こした経験を皆さんお持ちでしょう。

もちろん、Scalaでも関数を再帰で呼び出すことができます。むしろ、関数型言語の考え方として可能な限り再帰で書く方がよいとされています。

では、例として、自然数の階乗n!を計算する関数を定義します。以下の例を見てください。

.. code-block:: scala

  def factorial( n:BigInt):BigInt  =
    if( n <= 1 ) 1 else n * factorial( n - 1)

このfactorial関数は、nの値を1づつ減らしながら、nが1になるまで自身を再帰で呼び出していることがわかると思います。また、factorial関数は結果型がIntであると明示してます。
結果型を宣言しない場合は、"error: recursive method factorial needs result type"と、再帰する関数は結果型を明示するようコンパイラに求められます。

では、1から10までの階乗を計算させてみましょう。

.. code-block:: scala

  scala> (1 to 10 ).foreach( n => print( factorial( n ) + ", "))
  1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800,

末尾再帰による最適化
^^^^^^^^^^^^^^^^^^^^^^^^^^^
再帰を扱う場合、呼び出しのたびにスタックフレームを生成するため実行効率がわるい、StackOverflowを発生させないよう呼び出し階層が深くならないよう注意深くプログラミングする必要があるなど、様々なことを考える必要があります。

Scalaで再帰を行った場合、やはりStackOverflowが発生してしまうのでしょうか?

以下の例は、1からnまでの和を計算する関数です。(先ほどの階乗の例だと扱う数値が大きくなりすぎるので)
一発で計算することもできるのですが、例ですのであえて再帰を使っています。

.. code-block:: scala

  scala> def add( n:Long ):Long = if( n <= 1 ) 1 else n + add( n - 1 )
  add: (Long)Long

  scala> add( 10 )
  res0: Long = 55

では、大きな数を与えて再帰回数が多くなるようにしてみましょう。

.. code-block:: scala

  scala> add( 10000 )
  java.lang.StackOverflowError
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<console>:12)
    at .add(<con...

やはりStackOverflowが発生してしまいました。やはり再帰を扱うのは難しいのでしょうか?

しかし、Scalaには末尾再帰の最適化という機能があります。この機能により、関数本体の最後の計算が自分自身を再帰で呼び出している場合に限り、コンパイラが実行時にはwhileループで実装したようなバイトコードを生成してくれるのです。

つまり、末尾再帰になっている関数は、StackOverflowが発生しないのです。


先ほどのadd関数は、計算の最後が、"n + add( n - 1 )"でした。これは、add関数を呼び出した後に結果をnと加算しているので、末尾再帰ではありません。

では、add関数を末尾再帰になるように書き直してみます。
以下のコードを見てください。

.. code-block:: scala

  def add( n:Long ) = {
    def calc( sum:Long, m:Long ):Long =
      if( m <= 1 ) sum + 1 else calc( sum + m , m - 1 )
    calc( n , n - 1 )
  }

これが末尾再帰版のadd関数です。add関数自体は再帰されていないのですが、内部でcalcという作業用の関数を用意して、このcalc関数が再帰するようになっています。calc関数の最後は、自身呼び出していますね。

早速試してみましょう。書き換える前のadd関数はn=10000でStackOverflowが発生していました。

.. code-block:: scala

  scala> add( 10000 )
  res5: Long = 50005000

ちゃんと計算できましたね!n=10000000でも、すぐに結果が帰ってきます。

このように、Scalaで再帰を扱う場合は末尾再帰になるようにするのが基本です。add関数を書き直したときのように、場合によっては処理の中で作業用の関数を用意し、その作業用関数を末尾再帰にするというのはよく使われるテクニックなのです。

部分適用とカリー化
___________________________

部分適用された関数
^^^^^^^^^^^^^^^^^^^^^^^^^^^
2つの文字列を引数でもらい、重複する文字のみを取り出した文字列を返す関数を考えてみます。次のような関数です。

.. code-block:: scala

 def findDuplicateChar( s1:String, s2:String ) =
   (for( c1 <- s1; c2 <- s2 if c1 == c2 ) yield { c1 }) mkString

.. code-block:: scala

 scala> findDuplicateChar("abcdefg", "bdfghijk")
 res34: String = bdfg

このfindDuplicateChar関数に対して、第1引数の文字は同じで、第2引数の文字を変えて繰り返し実行するとします。
このときに、第1引数を固定した状態にできると便利ですよね。

Scalaでは、関数の部分適用という形でこれを実現できます。
以下のように、通常の関数呼び出しのなかで、固定したい引数を与え、可変にしたい引数を"_ :型"にすることで、与えた引数で固定された関数オブジェクトを得ることができます。

.. code-block:: scala

  val paf = findDuplicateChar( "aBcdeFghIjKLmNOPqrsTuVWxyZ",  _ :String )

"_" と":"の間はスペースが必要なことに注意しましょう。

やってみましょう。

.. code-block:: scala

  scala> val l = List( "abc","def","ghi","jkl","mno","pqr","stu","vwx")
  l: List[java.lang.String] = List(abc, def, ghi, jkl, mno, pqr, stu, vwx)

  scala> val paf = findDuplicateChar( "aBcdeFghIjKLmNOPqrsTuVWxyZ", _ :String )
  paf: (String) => String = <function>

  scala> for( s <- l ){ println( paf( s ) )}
  ac
  de
  gh
  j
  m
  qr
  su
  x

カリー化
^^^^^^^^^^^^^^^^^^^^^^^^^^^

カリー化とは、複数の引数をとる関数を、引数が「もとの関数の最初の引数」で戻り値が「もとの関数の残りの引数を取り結果を返す関数」に変換することをいいます。

具体的には、3つの引数を取って加算する関数があったとして、一つ目の引数を与えると、2引数を取って加算した結果を最初に与えた引数に加えて返す"関数"が返ります。

具体的に見てみましょう。以下は、カリー化された3つのIntを加算する関数です。

.. code-block:: scala

  def curriedSum( n:Int )( m:Int )( i:Int ) = n + m + i

Scalaでは、引数宣言の()を複数書くことができ、これですでにcurriedSum関数はカリー化されています。
では、どのように呼び出すのでしょうか。利用例は以下の通りです。

.. code-block:: scala

  scala> curriedSum( 10 )( 11 )( 12 )
  res37: Int = 33

呼び出すときも、宣言されている()と同じだけ(引数)を渡せばよいわけです。

では、1番目の引数を与えて、「もとの関数の残りの引数を取り結果を返す関数」を得るにはどのようにするとよいのでしょうか?
関数名に対してスペースを空けて"_"を指定することで関数オブジェクトを取り出すことができました。これと同じようにすればよいのです。

.. code-block:: scala

  scala> val cf = curriedSum( 10 )( 11 ) _
  cf: (Int) => Int = <function>

  scala> cf( 12 )
  res39: Int = 33

変数cfには、ちゃんと10 + 11 に引数を加えて返す関数が入っていますね。

可変長引数
___________________________

Javaでは可変長の引数を取るメソッドを定義することができました。Scalaでも可能です。

.. code-block:: scala

  scala> def printString( args:String * )= for( s <- args ){ print( s + ":" )}
  printString: (String*)Unit

  scala> printString( "aaa", "bbb", "ccc" )
  aaa:bbb:ccc:

定義の仕方は簡単で、可変にしたい引数の型指定のあとに、スペースを空けて"*"をつけるだけです。これで、printString関数は複数個の文字列を取ることができるようになりました。

受け取り側の関数の内部では、引数argsはArray[String]型になります。

配列を可変長引数を取る関数に対して展開して与えたい場合は、以下のように呼び出します。

.. code-block:: scala

  scala> val array = Array("aaa", "bbb", "ccc")
  array: Array[java.lang.String] = Array(aaa,  bbb,  ccc)

  scala> printString( ar: _ * )
  aaa:bbb:ccc:

展開したい配列を引数に与えるときには、": _*"をつけて呼び出します。":"と"_*"の間はスペースが必要なことに注意してください。


