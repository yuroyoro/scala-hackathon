importとpackage
--------------------------------

Scalaのimport文はJavaより高機能
________________________________

いままでもなんとなくは使っていましたが、Scalaのimport文について説明します。
Javaと同じく、importキーワードのあとに、importしたいクラス名を指定するだけです。

ただ、Javaに比べて様々な機能が追加されており、柔軟なimportができます。

* "import java.util._"と書くと、パッケージ以下の全てのクラスをimportできる
* "import java.util.Arrays._"と、クラス名に対して"._"を指定すると、そのクラスのメソッドをimportできる(Javaのstatic importに相当)

ここまではJavaと同じです。

* import文は、class宣言内や関数本体など、どの箇所に書いてもよい。
  importは、書かれたスコープでのみ有効となる
* "import java.io.{File, FileFilter}"のように、{}に複数のクラスを書くことが可能
* importするクラスに別名をつけることができる。
  "import java.util.{List => JList }"と、 =>のあとに別名を指定。この例では、
  java.util.ListはJListという名前で参照される。
* "import java.util.{ List => JList , _ }"で、別名をつけたあと残りのクラス(メンバー)をまとめてimport
* "import java.util.{ List => _, _ }"と、別名に該当する箇所に"_"を指定すると、そのクラス(List)はimportされず、java.utilの残りが一括importされる
* 相対import。import文は、現在のパッケージから相対的に解決される。


packageについて
________________________________

package宣言をすることで、そのソースファイルに定義されているクラスがどのパッケージに属するかを定義するのは、Javaと同じです。

基本的に、Javaのパッケージと同じ記述方法と慣習に従えば問題はありません。

ただ、Scalaでは一つのソースファイルに複数のパッケージを入れ子で宣言できます。

.. code-block:: scala

  package foo {
    class Foo
    package bar{
      class Bar
    }
    package baz{
      class Baz
    }
  }

インタプリタでは動作しませんが、この内容をソースファイルに記述してコンパイルと、きちんとfoo.barやfoo.bazパッケージに沿ってクラスBarやBazがコンパイルされます。

通常は、ほとんど使うことはないと思います。

アクセス制御について
________________________________

TODO あとで
