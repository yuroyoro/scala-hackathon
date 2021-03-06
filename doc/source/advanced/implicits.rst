Implicit ConversionとImplicit Parameter
--------------------------------------------

Scalaの特徴的な言語仕様である、Implicit Conversion(暗黙の型変換)とImplicit Parameter(暗黙の引数)について解説します。

Implicit Conversion (暗黙の型変換)
________________________________________

Implicit Conversion (暗黙の型変換)とは?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Scalaの特徴としてまずあげられるのは、このImplicit Conversion (暗黙の型変換)です。

Implicit Conversion (暗黙の型変換)とは、ある型から別の型への変換を、ユーザ定義の変換関数を定義しておくことにより自動的に行わせる仕組みのことです。

単純な例を見てみましょう。java.util.Date型の時刻をLong型に自動的に型変換させてみます。

.. code-block:: scala

  scala> import java.util.Date
  import java.util.Date

  scala> implicit def date2Long( d:Date ):Long = d.getTime
  date2Long: (java.util.Date)Long

  scala> val time:Long = new Date
  time: Long = 1258858956402

型変換を行わせる関数は、"implicit"キーワードを付与して宣言します。

.. code-block:: scala

  implicit def date2Long( d:Date ):Long = d.getTime

これが、Date型からLong型への型変換を行う関数です。関数名は特に規約があるわけではないのですが、一般的には変換元To(2)変換先という名前になっています。
引数には、変換元のオブジェクトを取るようにします。関数の変換結果として、変換後の値を返すようにします。

.. code-block:: scala

  scala> val time:Long = new Date
  time: Long = 1258858956402

このように、Long型の変数timeに、Date型のオブジェクトを代入してもエラーになりません。これは、代入の際にdate2Long関数が呼び出された結果なのです。




既存のオブジェクトに機能を追加する
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

先ほどの例は、単純な値の変換でしたが、Implicit Conversionを利用すると、あたかも既存のオブジェクトに新しい関数を追加させているかのようなことができます。

では、java.util.Data型に、日本語表記の時刻を返すjpDate関数を追加する、ということをImplicit Conversionを利用してやってみようと思います。

.. code-block:: scala

  import java.util.Date
  class JpDate( val d:Date ) {
    def jpdate:String = {
      import java.text.SimpleDateFormat
      ( new SimpleDateFormat("yyyy年mm年dd日hh時mm分ss秒")).format( d )
    }
  }

  implicit def date2JpDate( d:Date ):JpDate= new JpDate( d )

これは、Date型をラップするJpDateクラスを定義し、日本語の時刻を返すjpdate関数を追加しています。次に、Date型をJpDate型に変換する関数をimplicit defで定義します。

これで、Date型に対してjpdate関数を呼び出してみましょう。本来、Date型にはjpdateなんて関数は無いのでコンパイルエラーになるはずですよね?

.. code-block:: scala

  scala> (new Date ).jpdate
  res40: String = 2009年53年22日01時53分45秒

ちゃんと動いています。あたかもDate型に新しい関数が追加されたようにみえますね。
実際は、コンパイラがDate型に対するjpdate関数の呼び出しに、ここはJpDate型を要求しているものと推論して、date2JpDate関数を呼び出してImplicit Conversionを行っているのです。

実は、ScalaでもInt型に存在しない関数(例えばtoHexString関数など)を呼び出した時には、RichInt型へImplicit Conversionを利用して変換しています。


.. code-block:: scala

  scala> val i = 100
  i: Int = 100

  scala> i.toHexString
  res48: String = 64

このRichWrapperへの変換関数は、scala.PreDefオブジェクトに実装されています。scala.PreDefオブジェクトに定義された関数はデフォルトでimportされるので、意識しなくてもRichWrapper型への変換を利用できるのです。

Implicit Conversionの注意点
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Implicit Conversionは、うまく使えば強力な機能なのですが、暗黙のうちに型を変換するので思わぬ副作用をプログラムに及ぼす可能性があります。

ただし、Implicit Conversionが行われるのは、変換が行われる箇所内のスコープにimplicit defが存在する場合に限ります。

適切なスコープにimplicit defを定義またはimportすることで、副作用の影響範囲を局所化できるので、定義するスコープは慎重に選択して利用すべきです。

また、変換関数の実装にもよるのですが、たいていは新しいオブジェクトを生成して変換するので、パフォーマンスへの影響も多少はあります。

加えて、変換対象となるクラスは、イミュータブルなオブジェクトに限定すべきです。変換したあとのオブジェクトに対して変更しても、もとのオブジェクトは変更されない、ということが起こりえるからです。



Implicit Parameter(暗黙の引数)
________________________________________

次は、Implicit Parameter(暗黙の引数)についての解説です。
Implict Conversionと名前は似ていますが、機能としては異なります。

一言でImplict Parameterを説明するとすれば、「引数の型に適したデフォルト引数を自動的に選択する仕組み」と言えそうです。

では、具体的にどのような動作をするかと言うと、"implicit val"と宣言されている変数がスコープ内に存在する際に、引数の宣言で"implicit"が付与された引数を省略することが可能です。

「暗黙の引数」の名前通り、"implicit val"で宣言された変数は、"implicit"が付与されている関数の引数に、自動的に渡されるのです。

さっそくコードを見てみましょう。

.. code-block:: scala

  scala> def addPrefix( xs:List[String])(implicit prefix:String) = xs.map( prefix + _ )
  addPrefix: (List[String])(implicit String)List[java.lang.String]

  scala> implicit val pre = "Prefix_"
  pre: java.lang.String = Prefix_

  scala> addPrefix( List( "foo","bar","baz") )
  res0: List[java.lang.String] = List(Prefix_foo, Prefix_bar, Prefix_baz)

addPrefix関数は、List[String]型の引数xsの各要素に、引数prefixで受けた文字列を先頭に付与したListを返す関数です。このaddPrefix関数は、カリー化された状態で宣言されています。

２番目の引数をよく見てください。通常の引数の宣言の前に、"implicit"キーワードが付与されていますこれは、この第２引数は呼び出し側のスコープに"implicit val"で宣言されている変数が存在する場合は省略可能であることを示します。

次に、addPrefix関数を呼び出す前に、"implict val"で暗黙のうちに引数として渡したい値を宣言します。この場合は、`Prefix_`という文字列にしています。

この状態で、addPrefix関数の第2引数を省略した形で呼び出すと、第2引数に`Prefix_`が引き渡されている場合と同じ結果が得られます。"implicit val"による引数が暗黙のうちにaddPrefix関数に渡された結果です。

さて、このaddPrefix関数ですが、明示的に第2引数を渡して呼び出すことも可能です。"implicit val"による引数は、あくまで第2引数を省略した場合にのみ機能します。

