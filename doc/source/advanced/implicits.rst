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

では、java.util.Data型に、日本語表記の時刻を返すjpDate関数を追加する、ということをImplict Conversionを利用してやってみようと思います。

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

これで、Date型に対してjpdate関数を呼び出してみましょう。本来、Data型にはjpdateなんて関数は無いのでコンパイルエラーになるはずですよね?

.. code-block:: scala
  scala> (new Date ).jpdate
  res40: String = 2009年53年22日01時53分45秒

ちゃんと動いています。あたかもDate型に新しい関数が追加されたようにみえますね。




implicit Parameter(暗黙の引数)
________________________________________

