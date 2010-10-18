型のパラメータ化
----------------------------

Scalaでは、Javaと同様に型パラメータを利用することが出来ます

Genericsの基本
____________________________

型パラメータとは、クラスや関数について、扱う型を抽象化して扱うことを可能とする物です。
例えば、ScalaのList型は、そのListの要素の型を型パラメータとして取ります。

Int型を要素に持つListを定義してみましょう。結果が、List[Int]のように表示されました。この[Int]の部分が、List型の型パラメータにInt型が与えられていることを示しています。

.. code-block:: scala

  scala> List(1, 2)
  res2: List[Int] = List(1,  2)

Listクラスの定義を見てみましょう。以下のようになっています。

.. code-block:: scala

  sealed abstract class List[+A] extends Seq[A] with Product

List[+A]とクラスが宣言されています。この[+A]は、List型がAという名前で何らかの型を受け取るクラスである、という意味です。+については、後ほど説明します。

さて、このように型パラメータを利用すると、何が便利なのでしょうか?一番の利点は、キャストが不要になり、コンパイル時に型のチェックが出来るようになることでしょう。

Listの最後の要素を返す関数は、このような定義です。

.. code-block:: scala

  override def last : A

つまり、last関数はListが作られた時にAという型変数名でうけとった型を結果型として返す、という定義です。List[Int]であればlast関数はIntを返し、List[String]であればString型を返します。

このように、String型の変数にList[Int]のlast関数の結果を代入するとコンパイルエラーとなります。

.. code-block:: scala

  scala> val s:String = List(1, 2).last
  <console>:8: error: type mismatch;
    found   : Int
    required: String
       val s:String = List(1, 2).last

List型は、型パラメータを受け取ってList[+A]型を生成する型コンストラクタとも呼ばれます。

型パラメータは、クラスだけではなく、関数にも指定することが可能です。
以下のemptyMap関数はAという型パラメータを受け取って、キーと値にA型を持つ空のMapを返す関数です。

.. code-block:: scala

  scala> def emptyMap[A] = Map.empty[A,A]
  emptyMap: [A]scala.collection.immutable.Map[A,A]

  scala> emptyMap[Int]
  res12: scala.collection.immutable.Map[Int,Int] = Map()


上限境界と下限境界
____________________________

通常の型パラメータは、任意の型を指定することができます。しかし、型パラメータとして受け取る型に制限を指定したい場合があります。

上限境界や下限境界を指定することで、型パラメータTがあるクラスのスーパークラスである、あるいはサブクラスであるというような制限を指定することが可能です。


// TODO なんらかの例


共変と反変
____________________________

Listクラスの宣言では、List[+A]と型パラメータAの前に'+'がついていました。この'+'が何を意味するのか説明します。

Javaでは、List<Object>型の変数に対してList<String>型を代入することはできませんでした。String型はList型のサブタイプなので、このような代入ができても良さそうですが、様々な理由によりこのような代入はできないようにJava言語仕様は定められています。

Scalaでは、型パラメータに'+'が付与されている場合は、このような型パラメータで受け取った型のサブタイプを型パラメータにもつものをサブタイプとして扱えるようになります。
具体的には、List[String]型はList[AnyRef]型のサブタイプとして代入が可能です。

.. code-block:: scala

  scala> val l:List[AnyRef] = List[String]()
  l: List[AnyRef] = List()

また、型パラメータに'-'を付与することで、'+'とは逆の関係を定義できます。Foo[-A]という宣言は、Foo[String]型がFoo[AnyRef]型のスーパータイプであるという関係になります。


// TODO なんらかの例
