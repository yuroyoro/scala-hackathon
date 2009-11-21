サムネイル表示までを作成する
____________________________

では、まずはディレクトリ内の画像ファイルを検索して、サムネイルが表示されるまでを実装してみます。

Applicationトレイトを利用する
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
アプリケーションにはmain関数が必要ですが、毎回定義するのは面倒ですよね。Scalaには、Applicationトレイトというものが用意されており、これを利用するとmain関数を書かなくてもクラス内に記述した処理をmainとして実行してくれます。

ただ、Applicationトレイトには、起動時の引数を受け取ることは出来ないため、引数をとるアプリケーションは通常通りmain関数を定義する必要があります。

では、早速ソースファイルの作成に入りましょう。ファイル名は、まぁなんでもいんですけどとりあえずImageViewer.scalaにしておきます。

.. code-blokc:: scala

object ImageViewer extends Application{
  println( "Image Viewer " )

}

ここまで、コンパイルして実行すると、メッセージが表示されるはずです。

ディレクトリ内の画像ファイルを検索する
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

では、ディレクトリ内の画像ファイルを検索する処理を実装しましょう。
このアプリケーションでは、起動したディレクトリのなかの"images"ディレクトリ内から画像を検索することにします。

Javaでは、ディレクトリ内のファイル一覧を取得するには、java.io.File$listFilesを利用します。
インタプリタで動作を確認してみましょう。

.. code-block:: scala

  scala> import java.io.File
  import java.io.File

  scala> val dir = new File( "./images")
  dir: java.io.File = ./images

  scala> dir.listFiles
  res0: Array[java.io.File] = Array(./images/.DS_Store,  ./images/010-7.jpg,  ./images/011.jpg,  ./images/012-4.jpg,  ./images/017.jpg,  ./images/022-6.jpg,  ./images/20070823miku.jpg,  ./images/2007112815.jpg,  ./images/20091010600009.jpg,  ./images/200910121500006.jpg,  ./images/2a2d5252.jpg,  ./images/317307.png,  ./images/318077.png,  ./images/611fd5ae.jpg,  ./images/6a00d4142a45c73c7f00cd973dd59...

ファイルの一覧が取れましたね。ただ、このままでは画像ファイル以外も取得できてしまうため、画像ファイルだけ抽出する必要があります。

ここでは、拡張子が"jpg", "png", "gif"のいずれかであれば、画像ファイルと見なすことにします。

単純にString#endsWith関数で判断してもよいのですが、3回or判定をしなければ行けないのでメンドイですね。正規表現でマッチさせることにしましょう。

Scalaでは、java.util.regexをラップしたscala.util.matching.Regexという便利なクラスがあります。
Scalaでの具体的な性器表現の使い方を見ましょう。

.. code-block:: scala

  scala> val filePattern  = """.*\.(png|jpg|gif)$""".r
  filePattern: scala.util.matching.Regex = .*\.(png|jpg|gif)$

  scala> "test.png" match {
       |   case filePattern( _ ) => "ok"
       |   case _ => "ng"
       | }
  res1: java.lang.String = ok

シングルクォートを3つつづけて"""Fooo"""のようにかくと、ヒアドキュメントとして文字列を定義できます。"""内では改行も自由自在ですし、\(バックスラッシュ)をエスケープする必要もないので、正規表現を記述するにはもってこいです。

で、正規表現を書いた文字列に.r関数を呼び出すことで、scala.util.matching.Regexオブジェクトを取得できます。

Regexオブジェクトはパターンマッチで利用できますので、case filePattern(_) => "ok" はマッチした時に実行されます。filePattern( _ )の(_)は、正規表現内のグループに対応しています。ここでは、グループ(例では拡張子が入る)の内容は使わないので"_"で受けていますが、filePattern( ext ) =>print(ext)のように書くと、正規表現内のグループの内容が変数extに入ります。

では、ImageViewに画像ファイルだけ検索する関数を実装してみます。File#listFilesには、java.io.FileFilterインターフェースを実装したインスタンスを渡すことで、一覧にフィルターを書けることが出来ます。

.. code-block:: scala

  import java.io.{File,FileFilter }

  object ImageViewer extends Application{
      val filePattern  = """.*\.(png|jpg|gif)$""".r

      def imageFiles( dir:File ) = dir.listFiles{
        new FileFilter{
          def accept( f:File ) = f.getName match {
            case filePattern( _ ) => true
            case _ => false
          }
        }
      }
   }

これで画像ファイルのみ取り出すことができるようになりました。

ウィンドウを表示させる
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

では、GUIを使ってウィンドウを表示しましょう。ここでは、javax.swing.JWindowを使います。

.. code-block:: scala

  import java.io.{File, FileFilter }
  import javax.swing.{ JWindow }

  object ImageViewer extends Application{

    val filePattern  = """.*\.(png|jpg|gif)$""".r

    def imageFiles( dir:File ) = dir.listFiles{
      new FileFilter{
        def accept( f:File ) = f.getName match {
          case filePattern( _ ) => true
          case _ => false
        }
      }
    }

    private val w = new JWindow {
      import java.awt.Toolkit
      import java.awt.event._

      addMouseListener(new MouseAdapter(){
        override def mouseClicked(e:MouseEvent){ System.exit(0) }
      })

      setSize( 640, 420)
      setVisible( true )
    }
  }

ブランクのウィンドウが表示され、クリックすると終了しましたね。では、先ほど実装した画像ファイルの一覧から、サムネイルを作る処理を実装します。

listFiles関数で取得できるのは、JavaのFile型の配列オブジェクトで、ScalaのArray型ではありません。生のJavaの配列は扱いにくいので、ScalaのArray型に変換することにします。

やり方は簡単で、"import scala.collection.jcl.Conversions._"とimportを書くだけです。これだけで、Javaの配列やListやMapは、Scalaのコレクションに変換されます。

これは、Conversionsオブジェクトがimplict conversionを用いてコレクション型への変換を行うためです。

さて、imageFilesで取得したファイル一覧に対して、サムネイル画像を作ってウィンドウに追加する処理にはいります。imageFilesの結果を、foreachで処理すると良さそうです。

.. code-block:: scala

  imageFiles( new File( "./images" ) ).foreach{ f =>

    def createThumb( image:BufferedImage ) =
      image.getScaledInstance(100,100, Image.SCALE_AREA_AVERAGING)

    val icon = new ImageIcon
    val image = ImageIO.read( f )
    icon.setImage( createThumb( image) )
    val label = new JLabel( icon )
    label.setSize(100, 100)
    pane.add( label )

  }

foreachの中で、ファイルからBufferedImageとして画像を読み込んで、ImageIconを作ってJLabelに追加しています。サムネイルを作る処理は、BufferedImage#getScaledInstanceで作成できますので、これを利用しています。

以下が、完成したソースコードです。これをコンパイルして実行すると、サムネイルが表示されたウィンドウが出るはずです。

.. code-block:: scala

  import java.io.{File, FileFilter }
  import java.awt.{Image, FlowLayout }
  import java.awt.image.BufferedImage
  import javax.swing.{ImageIcon,  JWindow,  JLabel}
  import javax.imageio.ImageIO

  import scala.collection.jcl.Conversions._

  object ImageViewer extends Application{

    val filePattern  = """.*\.(png|jpg|gif)$""".r

    def imageFiles( dir:File ) = dir.listFiles{
      new FileFilter{
        def accept( f:File ) = f.getName match {
          case filePattern( _ ) => true
          case _ => false
        }
      }
    }

    private val w = new JWindow {
      import java.awt.Toolkit
      import java.awt.event._

      val pane = getContentPane
      pane.setLayout( new FlowLayout() )

      imageFiles( new File( "./images" ) ).foreach{ f =>

        def createThumb( image:BufferedImage ) =
          image.getScaledInstance(100,100, Image.SCALE_AREA_AVERAGING)

        val icon = new ImageIcon
        val image = ImageIO.read( f )
        icon.setImage( createThumb( image) )
        val label = new JLabel( icon )
        label.setSize(100, 100)
        pane.add( label )

      }

      addMouseListener(new MouseAdapter(){
        override def mouseClicked(e:MouseEvent){ System.exit(0) }
      })

      setSize( 640, 420)
      setVisible( true )
    }
  }

