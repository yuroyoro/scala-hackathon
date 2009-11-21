クラス、オブジェクト、トレイト
--------------------------------

ここでは、Scalaどのようにオブジェクト指向のプログラムを作成するかを説明します。

基本的なクラスの定義方法から、Scalaの特徴であるシングルトンオブジェクトやtraitについて解説します。

クラスの宣言
___________________________

Scalaのクラスを定義するには、classキーワードを指定して宣言します。

.. code-block:: scala

  class Person( firstName:String ,lastName:String, n:Int ){

    println( "person created. %s %s".format( firstName, lastName ) )

    val fullName = "%s %s".format( firstName, lastName )
    private val age = n
    def greeting = { println( "Hello " + fullName ) }
  }


Personクラスの宣言で、( firstName:String , ... )と書いてあります。これは、コンストラクタの定義です。scalaでは、クラス名のあとに、関数定義の引数と同様の書式で、コンストラクタ引数を定義できます。

クラス定義の中で、いきなりprintln関数を呼び出していますね。クラス定義の中に書かれた処理は、そのクラスのインスタンスが生成された時に実行されます。つまり、コンストラクタ内の処理です。


このPersonクラスは、fullNameというフィールドとgreetingというメンバ関数を持っています。

フィールドは、valまたはvarで宣言します。フィールドのアクセス修飾子は、デフォルトでpublicとなります。細かいアクセス制御の方法は、後述します。

valで宣言したフィールドは、読み取り専用となります。varは、他のオブジェクトが値を更新することが可能です。


メンバ関数も、クラス定義内にdefキーワードで宣言します。こちらも、デフォルトのアクセス修飾子はpublicです。


Scalaでは、クラスにstaticなメンバ変数や関数を定義することができません。staticな定義を行いたい場合は、このあと説明するコンパニオンオブジェクトに定義する方法が一般的です。

では、先ほどのPersonクラスの定義をScalaインタプリタ上で入力してみましょう。インタプリタ上でも、クラスを定義することができます。

.. code-block:: scala

  scala> val p = new Person( "Tomohito",  "Ozaki" ,  20 )
  person created. Tomohito Ozaki
  p: Person = Person@ecf2c09

  scala> p.fullName
  res0: String = Tomohito Ozaki

  scala> p.greeting
  Hello Tomohito Ozaki

インスタンスの生成は、newキーワードです。Javaと同じですね。

インスタンスを生成すると、Personクラス内に記述してあったprintln関数が実行されて、インスタンスが生成されたことが出力からわかるかとおもいます。

fullNameフィールドにもアクセスできますし、greeting関数も呼び出すことが出来ています。

シングルトンオブジェクト
___________________________

Scalaには、シングルトンオブジェクトを定義する構文が用意されています。Javaでシングルトンオブジェクトを定義しようと思ったら、staticなgetInstanceメソッドを用意して、ロックに注意して実装する必要がありましたが、Scalaではキーワード一発です。

object クラス名 { 本体 } これでシングルトンオブジェクトを定義できます。簡単ですね。

.. code-block:: scala

  object OnlyOnePerson {
    val fullName = "Stevie Ray Vaughan"
    var albums = List("Texas Flood" , "In Step", "Soul to Soul")
    def showBio = albums.foreach{ println }
  }

これは、シングルトンオブジェクトの定義です。シングルトンオブジェクトは、newキーワードでインスタンスを作ることが出来ません。

シングルトンオブジェクトの関数を呼び出すには、オブジェクト名の後に"."や" "で関数を呼び出せばよいのです。

.. code-block:: scala

  scala> OnlyOnePerson.showBio
  Texas Flood
  In Step
  Soul to Soul

  scala> OnlyOnePerson.fullName
  res3: java.lang.String = Stevie Ray Vaughan


コンパニオンオブジェクト
___________________________

さて、Scalaのクラスにはstaticなメンバを持たせることができない、と書きました。では、staticな定義が必要な場合はどのようにするのかというと、コンパニオンオブジェクトというものを定義して利用します。

コンパニオンオブジェクトとは、あるクラスに対して同じスコープ内で同じ名前で定義されたシングルトンオブジェクトのことです。

さきほどのPersonクラスに対応するコンパニオンオブジェクトは、以下のように定義できます。

.. code-block:: scala

  object Person{
    def getAge( p:Person ) = p.age
  }

コンパニオンオブジェクトは、対となるクラスに対して特権的なアクセス権を持っています。つまり、クラスのprivateフィールドへアクセスすることが出来るのです。


PersonオブジェクトのgetAge関数は、本来privateなのでアクセスできないはずのPersonクラスのageフィールドを読み出しています。これが、特権的なアクセス権です。


通常、コンパニオンオブジェクトはクラスが定義されているのと同じソースファイル内に定義します。

コンパニオンオブジェクトである条件は、対象となるクラスと同じスコープなので、パッケージが異なる場合は同じ名前でもコンパニオンにはなりません。

コンストラクタについてもっと詳しく
____________________________________


複数のコンストラクタ
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Scalaのコンストラクタの定義は、クラス名の後に記述することになっていました。では、複数のコンストラクタをクラスに定義する場合はどうするのでしょう?

クラスに別のコンストラクタを持たせる場合は、def this(引数) と定義します。ただ、Scalaのコンストラクタは、Javaに比べると厳しい制約を持っています。

 * コンストラクタは、最初に必ず別のコンストラクタをthisで呼び出さなくてはならない
 * 親クラスのコンストラクタを呼び出すことはできない。

具体例を見てみます。以下のFooクラスは引数を1から3とるコンストラクタがそれぞれ定義されています。

.. code-block:: scala

  class Foo( i:Int ){
    println( "one: " + i )
    def this( i:Int, j:Int ) = {
      this( i )
      println( "two:" + j )
    }
    def this( i:Int , j:Int, k:Int )={
      this( i, j )
      println( "three:" + k )
    }
  }

以下は、それぞれのコンストラクタを呼び出した結果です。

.. code-block:: scala

  scala> new Foo( 1 )
  one: 1
  res8: Foo = Foo@37c390b8

  scala> new Foo( 1,  2 )
  one: 1
  two:2
  res9: Foo = Foo@2d09b23b

  scala> new Foo( 1,  2,  3 )
  one: 1
  two:2
  three:3
  res10: Foo = Foo@43059849


Scalaでは、このようにコンストラクタを複数用意するより、コンパニオンオブジェクトを用意して、コンパニオンオブジェクトにファクトリメソッドを実装する方法がよく行われます。

コンパニオンオブジェクトにインスタンス生成を任せることで、例えばインスタンスをキャッシュしたりするなどが可能になるからです。

コンストラクタ引数の指定
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

PersonクラスやFooクラスのコンストラクタ引数の定義の方法では、コンストラクタ引数で渡された値はインスタンス生成時にのみ利用され、その後生成されたオブジェクトはその値を覚えていません。

しかし、通常のクラスでは、コンストラクタ引数をクラスのメンバ変数に代入して保持しておくことが多いでしょう。

コンストラクタ引数を指定する際に、以下のようにvalキーワードやvarキーワードを付与することで、引数名と同じプロパティがpublicで追加されます。valの場合は読み取り専用、varの場合は読み込みと書き込みが可能です。

.. code-block:: scala

  scala> class Employee( val name:String, var dept:String )
  defined class Employee

  scala> val e = new Employee( "Ozaki" ,"Development" )
  e: Employee = Employee@2295aedf

  scala> e.name
  res19: String = Ozaki

  scala> e.dept = "Sales"

アクセス修飾子として、protectedなどをつけることもできます。


継承するクラス
___________________________

クラスは、Javaのクラスと同じように継承させることが可能です。通常通り、extendsキーワードを指定することで他のクラスを継承できます。

.. code-block:: scala

  scala> class Guitar( val scale:Int , val name:String )                                           defined class Guitar

  scala> class AcousticGuitar( scale:Int , name:String ) extends Guitar( scale ,  name )
  defined class AcousticGuitar

コンストラクタ引数の指定に注意する必要があります。Guitarクラスはコンストラクタ引数を二つ取るため、継承先のクラスではextendsキーワードのあとにスーパークラスのコンストラクタに値を渡す必要があります。これは、スーパークラスのコンストラクタ呼び出しを行うためです。


抽象クラス、finalクラス
___________________________

abstractキーワードで抽象クラスを宣言することが出来ます。また、finalキーワードでクラスが継承できないことを指定可能です。

抽象クラスは抽象メソッドを持つことができます。抽象メソッドは、=以降の関数の本体を書かないことで宣言します。

先ほどのGuitarクラスを抽象クラスにして、メンバ関数を追加した例が以下です。

.. code-block:: scala

  scala> abstract class Guitar( val scale:Int ,val name:String ){
       |   def openNote( stringNumber:Int )
       | }
  defined class Guitar

  scala> class AcousticGuitar( scale:Int ,name:String ) extends Guitar( scale , name ){            |  override def openNote( stringNumber:Int ) =
       |    List("E","B","G","D","A","E")( stringNumber - 1 )
       | }
  defined class AcousticGuitar

スーパークラスのメソッドをオーバーライドする場合は、サブクラス側ではoverrideキーワードを指定しなければなりません。Javaでいう@overrideと似ていますが、Scalaの場合はoverrideキーワードが指定されていないとコンパイルエラーとなります。

なお、メンバ関数やフィールドに対してもfinalキーワードでサブクラスでのオーバーライドを禁止する指定ができます。

