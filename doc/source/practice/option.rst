Option型の利用
----------------------------

ScalaのOptionの使い方について解説しますよ。OptionはScala使いにとってはなくてはならないものです。これを覚えてしまうと他の言語でもOptionを作りたくなる、かもしれない?

Optionってなに?
_______________________________

Option型は、値があるかないか分からない状態を表すものです。「Maybeモナド」とか言ってしまうとアレですがまぁそんなようなものらしいです。

Optionの定義
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
まずは、Optionの定義から見てみましょう。Option型は、抽象クラスOption[+A]と、Option[A]を継承して値がある場合のSome[+A]型とNoneオブジェクトがあります。


[http://www.scala-lang.org/archives/downloads/distrib/files/nightly/docs/library/scala/Option.html:title]


.. code-block:: scala

  // Optionの抽象クラス。持つかもしれない値の型をパラメータでもつ
  sealed abstract class Option[+A] extends Product

  // Someは、Optionを継承して値がある場合を表現する。
  // コンストラクタに実際の値をとる
  final case class Some[+A](x: A) extends Option[A]

  // Noneは、値がない場合を表す。Optionの型パラメータは共変なので
  // Option[Nothing]は全てのOption[A]型のサブタイプになる。なのでシングルトン。
  case object None extends Option[Nothing]



基本的な使い方
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
それでは、実際の使用例を見てみましょう。以下のfindFile関数は、引数のファイル名のファイルが存在した場合はSome[java.io.File]を返し、存在しないならばNoneを返します。

.. code-block:: scala

  import java.io.File

  def findFile( filename:String):Option[File] = {
    val file = new File( filename )
    if( file.exists ) Some( file )  // Fileが存在したらSomeに入れて返す
    else None                       // ないならNoneオブジェクトを返す
  }



これでfindFile関数を呼び出すとSome[java.io.File]かNoneかのどちらかが返されるようになりました。使ってみましょう。

.. code-block:: scala

  scala> val zshrc = findFile( "/Users/ozaki/.zshrc")
  zshrc: Option[java.io.File] = Some(/Users/ozaki/.zshrc)

  scala> val test = findFile( "hoge")
  test: Option[java.io.File] = None



値の取り出し
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
しかし、このままではOption型の中に実際に取り出したいFileオブジェクトが入ったままなので、値を取り出す必要があります。


もっともシンプルな方法は、Option型に用意されているgetメソッドを呼び出す方法です。


.. code-block:: scala

  scala> file.get
  res3: java.io.File = /Users/ozaki/.zshrc

  scala> test.get
  java.util.NoSuchElementException: None.get
    at scala.None$.get(Option.scala:185)
    at scala.None$.get(Option.scala:183)
      ...



Some[java.io.File]型であれば、getメソッドを呼び出すことで実際の値であるFileオブジェクトを取り出すことができましたが、値がない場合のNoneオブジェクトにgetメソッドを呼び出すとNoSuchElementExceptionがthrowされてしまいました。


Noneの場合は例外がthrowされるので、Option#getメソッドによる値の取り出しは利用すべきではありません。いっそAPIから削除してもらいたいくらいです。せめて@deprecatedつけて欲しい。


じゃあどうすんの? って話ですが、基本はパターンマッチを使います。Option型はsealed指定されており、Some[A]はケースクラスですので、Option型に対するパターンマッチを書くことで値がある場合とない場合の処理を自然に分離できますし、値がなかった場合の処理が漏れていた、なんてバグも無くなります。

.. code-block:: scala

  scala> file match {
       |   case Some( f ) => "exist! [%s]" format f.getName
       |   case None  => "nothing!"
       | }
  res5: String = exist! [.zshrc]

  scala> test match {
       |   case Some( f ) => "exist! [%s]" format f.getName
       |   case None  => "nothing!"
       | }
  res6: String = nothing!



Some[A]はケースクラスなので、"case Some(f) => ..."のようにパターンを書くと、Someが保持している値がパターン変数fに束縛されて自然に取り出せます。


以下のように、Noneの場合のパターンを書かなかったら、このようにコンパイラが警告を出してくれるので、処理の記述漏れも無くなります。便利。親切。

.. code-block:: scala

  scala> file match {
       |   case Some( f ) => "exist! [%s]" format f.getName
       | }
  <console>:10: warning: match is not exhaustive!
  missing combination           None

         file match {
         ^
  res7: String = exist! [.zshrc]


nullじゃあかんの?
_______________________________

さて、JavaなどのAPIでは、値が存在しない場合にはnullを返すような設計のものがあります。このようなアプローチではいけないのでしょうか?

nullを返すAPIの問題点
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
以下に、実際にjavaでのMapオブジェクトを利用する際の具体例で問題を示します。

.. code-block:: scala

  scala> import java.util.{ Map => JMap, HashMap }
  import java.util.{Map=>JMap, HashMap}

  scala> val map:JMap[String,String] = new HashMap[String,String]
  map: java.util.Map[String,String] = {}

  scala> map.put( "foo","bar")
  res9: String = null

  scala> map.put("hoge",null)
  res10: String = null


HashMapオブジェクトmapは、キー"foo"に対して値"bar"を持ち、キー"hoge"に対して値nullを持ちます。このmapオブジェクトから値を取り出して、値の文字数を出力する処理を考えてみます。

.. code-block:: scala

  scala> val foo = map.get("foo")
  foo: String = bar

  scala> foo.length
  res11: Int = 3



キーがある場合は問題ありません。キーがない場合は?

.. code-block:: scala

  scala> val fuga = map.get("fuga")
  fuga: String = null

  scala> fuga.length
  java.lang.NullPointerException
    at .<init>(<console>:10)
    at .<clinit>(<console>)


"fuga"というキーを持っていないため、HashMap#getはnullを返します。その後は、lengthの呼び出しに対して当然ながらぬるぽ!ガッ! ですorz。


そのため、mapから値を取り出した後にはnullチェックをしなければなりません。そして、nullチェックが行われているかはコンパイラは知ることができません(これ重要)。

.. code-block:: scala

  scala> if( fuga != null ) fuga.length else -1
  res14: Int = -1



面倒ですね。汁でそうですね?


もう一つ問題があります。HashMapは値としてnullを持つことができます。そしてHashMap#getはキーが存在しない場合はnullを返します。

.. code-block:: scala

  scala> map.get("hoge")
  res15: String = null

  scala> map.get("fuga")
  res16: String = null


これだと、キーがあって値がnullなのか、キーがないからnullが返っているのか判断できません。判断するには、Map#containsKeyメソッドを呼び出して事前にキーを持つか判断しなければなりません。面倒ですね?汁(ry


つまり、nullを返すAPI設計はふたつの問題点をはらんでいます。


- 利用する側にnullチェックを強要する。nullチェックが行われているかはコンパイラでチェックできないので実行時エラーが出る。
- nullという値にふたつの意味を持たせてしまう。APIの結果値がないという意味のnullと、値としてのnull。

Optionを使うと解決します!(ｷﾘｯ
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

では、上記の問題点がOptionの導入によりどのように解決されているか、ScalaのコレクションのMapを例に解説します。

.. code-block:: scala

  scala> val map = Map( "foo" -> "bar","hoge" -> null )
  map: scala.collection.immutable.Map[java.lang.String,java.lang.String] = Map((foo,bar), (hoge,null))

  scala> def valueLength( key:String ) = map.get( key ) match {
       |    // 値があってnullの場合
       |    case Some( v ) if v == null => "key %s value is null." format( key )
       |    // 値がある場合
       |    case Some( v ) => "key %s value's length is %d" format( key,v.length )
       |    // 値がない場合
       |    case None => "key %s is not contains." format( key )
       | }
  valueLength: (key: String)String


mapのキーに対して値の文字数を出力するvalueLength関数を用意しました。ScalaのMap#get( key:A):Bは、キーに対する値をOption型に包んで返します。


valueLength関数では、引数のキーに対してMap#getを呼び出して、返された結果のOptionに対してパターンマッチを行っています。


さきほど述べた問題点は、Optionを返すので必ずSomeまたはNoneの場合の処理を書かねばならず、nullを返す場合に比較して値がある場合とない場合の処理を書くことを強制しています。パターンが漏れていた場合はコンパイラが警告を出すので、実行時エラーは起こりえません。


また、値が無い場合はNoneなので、値があってnullの場合とない場合は区別できます(値自体がnullだった場合はnullチェックが必要ですが…)。


.. code-block:: scala

  scala> valueLength( "foo")
  res20: String = key foo value's length is 3

  scala> valueLength( "hoge")
  res21: String = key hoge value is null.

  scala> valueLength( "fuga")
  res22: String = key fuga is not contains.



先ほどの問題点は、Optionを導入することによりコンパイラにチェックを任せることができるようになりました。もう実行時エラーで汁が出ることもなくなります。


JavaのAPIをScalaから利用する場合でも、nullをOptionに変換するようなwrapperを用意すると幸せになります。


あなたが書いたScalaのコードでnullが登場する箇所は、Optionに置き換えるべきところなのです!


高階関数を利用したカッコイイOptionの使い方
_______________________________________________

Optionの取り扱いの基本はパターンマッチですが、慣れてくるとmatch式を使わずにOption型で用意されているmapやforeachなどの高階関数やgetOrElseやorElseなどを使うようになります。この方がカッコイイし。


では、例としてコマンドラインオプションの解析をもとにOption型で用意されているAPIの利用方法を説明します。


scalacコマンドのオプションは、以下のように"-verbose"のようなスイッチのものと、"-classpath <Path>"のように値をとるものがあります。

>||
 scalac -verbose -unchecked -deprecation -classpath ./:./lib -d ./bin hoge.scala


このオプションを解析して、Map[String,String]型のオブジェクトを生成するユーティリティを以下のように用意します。上記のオプションの解析結果は" Map((unchecked,), (verbose,), (deprecation,), (classpath,./:./lib), (d,./bin))"のようになります。

オプションに引数が無い場合は、キーに対しての値が""、classpathのように引数をとるオプションは"./:./lib"が値に入っているMapなわけです。

.. code-block:: scala

  val OptPattern = """-(\S+)\s?([^-]\S+)?""".r
  def parse( args:String ):Map[String,String] =
    OptPattern.findAllIn(args).matchData.map{ m =>
      m.group(1) -> (if( m.group(2) == null ) "" else m.group(2)) }.toMap



このparse関数の詳細は本筋じゃないので割愛します。あ、Scala2.8でないと動きませんので。


じゃあ、解析した結果がMap[String,String]型の変数optsに入っている前提で進めます。

.. code-block:: scala

  scala> val rawargs = "-verbose -unchecked -deprecation -classpath ./:./lib -d ./bin hoge.scala"
  rawargs: java.lang.String = -verbose -unchecked -deprecation -classpath ./:./lib -d ./bin hoge.scala

  scala> val opts = parse( rawargs )
  opts: Map[String,String] = Map((unchecked,), (verbose,), (deprecation,), (classpath,./:./lib), (d,./bin))



isDefined,isEmptyによるチェック
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Option#isDefinedでSomeならばtrueを、Option#isEmptyでNoneならばtrueを得ることができます。Optionが値を持つかをBoolean型でチェックできるという訳です。

.. code-block:: scala

  scala> opts get("verbose") isDefined
  res69: Boolean = true

  scala> opts get("explaintypes") isDefined
  res70: Boolean = false

  scala> opts get("verbose") isEmpty
  res71: Boolean = false



これは、まぁそんなには使いませんが。

mapによる値の変換
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
"Option#map[B](f: (A) ⇒ B): Option[B]"は、Someの場合は引数の"f:(A) => B"型の関数オブジェクトに保持しいてる値を渡して変換した結果をSome[B]で返し、NoneだったらNoneのまま、というメソッドです。


"-unchecked"などのスイッチが設定されている場合はtrueを返したい場合は、以下のようにします。

.. code-block:: scala

  scala> opts.get("verbose").map{ v => true }
  res57: Option[Boolean] = Some(true)

  scala> opts.get("explaintypes").map{ v => true }
  res58: Option[Boolean] = None


opts.get("verbose")はMap#getによりキーが存在する場合はSome[String]を返すのでMap#getで返されたOption[String]に対してmapメソッドでtrueを設定しています。この場合は、引数は関係ないので値がなんだろうがtrueを返しています。


このようにして呼び出すことで、Some[String]型をSome[Boolean]型に変換できました。mapによる加工は、このあと説明するorElseやgetOrElseによる値の取り出しの前段階の処理としてよく利用します。

getOrElseによるデフォルト値
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

"Option#getOrElse[B >: A](default: ⇒ B): B"は、Someだったら持っている値を返し、Noneだったら引数に与えた値を返す関数です。


先ほどのmapによってOption[Boolean]型になったコマンドラインオプションから、デフォルト値を指定して値を取り出すのにgetOrElseメソッドを利用します。具体的には以下のように使います。

.. code-block:: scala

  scala> val verboseOn = opts.get("verbose").map{ v => true }.getOrElse( false )
  verboseOn: Boolean = true

  scala> val explaintypesOn = opts.get("explaintypes").map{ v => true }.getOrElse( false )
  explaintypesOn: Boolean = false



Option型からデフォルト値を指定して取り出すことができました。これは、パターンマッチを用いて以下のように書いたのと同じです。

.. code-block:: scala

  scala> val verboseOn = opts.get("verbose").map{ v => true } match {
       |   case Some(b) => true
       |   case None =>  false
       | }
  verboseOn: Boolean = true



このようにして、-verboseオプションが設定されているかをBoolean型の変数に設定できました。getOrElseの方が短く書けてステキですね。


foreachによる処理
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

"Option#foreach[U](f: (A) ⇒ U): Unit"は、Someだったら持っている値を引数の関数オブジェクトに渡して処理を呼び出し、Noneだったら何もしない、という高階関数です。


これは、Listなどのforeachと同じです。Listなどコレクションのforeachは持っている要素を順番に引数の関数オブジェクトに渡して処理させました。Listが空だったらなにもしません。Optionは、要素数1のListと考えてみると、foreachが同じ考えであることに気がつくでしょう。


"-classpath"オプションの引数を分解して表示する処理はこんな風になります。

.. code-block:: scala

  scala> opts get("classpath") foreach{ s => println( s.split(":").toSeq) }
  WrappedArray(./, ./lib)


orElseによる合成
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

コマンドラインオプションの内、"-verbose"か"-deprecation"のどちらかが指定されていたらエラーにしたいとします。その場合に例外をなげるようにしたいとして、ストレートに考えると、以下のようにif文を書くでしょう。

.. code-block:: scala

  val verbose = opts get("verbose")
  val deprecation = opts get("deprecation")

  if( verbose.isEmpty || deprecation.isEmpty ) throw new IllegalArgumentException



"Option#orElse[B >: A](alternative: ⇒ Option[B]): Option[B]"を利用すると、もっと簡単に書けます。Someに対してorElseを呼び出すと自身を返し、Noneに対して呼び出すと引数のOptionを返します。

.. code-block:: scala

  scala> verbose orElse deprecation foreach{ v => throw new IllegalArgumentException }
  java.lang.IllegalArgumentException
    at $anonfun$2.apply(<console>:15)
    ...



verboseがNoneだったらdeprecationが返されます。deprecationもNoneだったら、続くforeachは実行されません。どちらかがSomeだった場合はforeachが実行されて例外が発生します。


他の例として、クラスパスを、"-classpath"が指定されていない場合は"-d"の引数を、両方が指定されていない場合は"./classes"に設定したいとします。orElseとgetOrElseでこんな風に書きます。

.. code-block:: scala

  scala> opts.get("classpath") orElse( opts.get("d") ) getOrElse("./classes")
  res79: String = ./:./lib



複数のOptionをネストしたmatch式で書いているときには、orElseの利用を検討してみるとよいでしょう。

他にも
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

collect[B](pf: PartialFunction[A, B]): Option[B]
引数にPartialFunctionを渡して、PFが適用できる場合のみmapします。適用できない場合はNoneです。

.. code-block:: scala

  scala> opts.get("verbose").collect{
       |   case "" => false
       | }
  res88: Option[Boolean] = Some(false)



exists(p: (A) ⇒ Boolean): Boolean
Optionの値に対して、引数のpがtrueを返すかチェックします。Noneに対して呼び出すと常にfalseです。

.. code-block:: scala

  scala> opts.get("d").exists { s => s == "./bin" }
  res89: Boolean = true



filter(p: (A) ⇒ Boolean): Option[A]
引数のpがtrueを返す場合のみSome[A]を返します。falseを返す場合はNoneになります。

.. code-block:: scala

  scala> opts.get("verbose").filter{ v => v.nonEmpty }
  res90: Option[String] = None

  scala> opts.get("classpath").filter{ v => v.nonEmpty }
  res91: Option[String] = Some(./:./lib)



flatMap[B](f: (A) ⇒ Option[B]): Option[B]
"(A) => Option[B]"の結果がSome[B]だったらSome[B]を、NoneだったらNoneを返します。

.. code-block:: scala

  scala> opts.get("verbose").flatMap{ v => Some(v) }
  res92: Option[String] = Some()

  scala> opts.get("verbose").flatMap{ v => None }
  res93: Option[Nothing] = None



OptionのflatMapは、Optionを含むflatMapを呼び出すときに利用できます。
optsの中で引数が指定されているもののみ取り出したい場合は、optsのflatMapでOptionを返すような関数を渡せばおけーです。

.. code-block:: scala

  scala> opts.flatMap{
       |   case (k,"") => None
       |   case (k,v) => Some(v)
       | }
  res95: scala.collection.immutable.Iterable[String] = List(./:./lib, ./bin)




Optionとfor
_______________________________________________

Optionは、for式のgeneratorに指定できます。複数のOptionがSomeの場合のみなんらか処理を行わせたい場合に便利です。


以下の例は、"classpath","d"の2つが指定されている場合にのみ処理を行う例です。

.. code-block:: scala

  scala> for( cp <- opts.get("classpath"); d <- opts.get("d") ) {
       |   println(" classpath: %s" format cp )
       |   println(" dest     : %s" format d )
       | }
   classpath: ./:./lib
   dest     : ./bin


