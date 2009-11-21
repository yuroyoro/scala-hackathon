Scalaっぽくリファクタリングする
____________________________________________

ここまで、画像ビューワーとしては完成しましたが、プログラム自体はどちらかというとJavaのコードをScalaに置き換えただけのように見えます。もう少しだけ、Scalaの特徴を生かしたプログラムになるようにリファクタリングしてみます。

traitの導入 ImageIconの生成やWindow周りを共通化
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

ThumbWindowとViewWindowクラスには、ImageIconの生成やウィンドウサイズの設定など、共通化出来そうな処理があります。ここを、traitを導入して共通化させてみましょう。

.. code-block:: scala

  trait ImageWindow extends JFrame{
    val w:Int
    val h:Int
    val t:String

    val pane = getContentPane

    def open = {
      setSize( w, h )
      setTitle( t )
      setVisible(true)
    }

    def createIcon( file:File )( f:(BufferedImage) => Image) = {
      val icon = new ImageIcon
      val image = ImageIO.read( file )

      icon.setImage( f(image) )
      (icon , image )
    }
  }

ImageWindowというtraitを定義しました。このtraitは、JFrameを継承しています。

抽象変数として、w, h, tを持っています。これは、ウィンドウの幅と高さとタイトルを表しており
このtraitを実装するクラスで値を設定する必要がある変数です。

そして、これらの変数を利用してウィンドウのタイトルや大きさを設定するopen関数があります。

ImageIconを生成する処理として、createIcon関数を作成しました。この関数はカリー化されており、第一引数は画像ファイル、第二引数はBufferedImageを変換する関数を受け取ります。

結果型は、ImageIconオブジェクトとImageオブジェクトのタプルです。

具体的な利用方法を見てみましょう。以下のコードは、ThumbWindowの中で、foreachを利用してサムネイル画像を作成している処理です。

.. code-block:: scala

  imageFiles( new File( "./images" ) ).foreach{ f =>
    val (icon, image ) = createIcon( f ){ image =>
      image.getScaledInstance(100,100, Image.SCALE_AREA_AVERAGING)
    }

以前に比べてずいぶんすっきりしましたね。{}を使って、createIcon関数の第二引数に渡す関数オブジェクトを関数リテラルの形で定義していることに注意してください。


MouseEvent処理をtraitに
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
このアプリケーションでは、マウスイベントを処理する箇所が3箇所あります。ThumbWindowでクリックされるとアプリケーションを終了し、JLabelのサムネイル画像をクリックするとViewWindowを生成し、ViewWindowをクリックするとウィンドウを閉じる、以上の箇所です。

これらのイベント処理は、コンポーネントに対してaddMouseListenerでMouseAdapterの匿名サブクラスを追加する形になっています。

クリックされた時の処理を切り出すことが出来れば、MouseAdapterの登録は共通化できそうです。幸い、Scalaでは処理を関数リテラルを利用してオブジェクトとして渡すことができるようになっています。

.. code-block:: scala

  trait ClickEventHandler extends Component{

    val handle: ()=> Unit
    addMouseListener(new MouseAdapter(){
      override def mouseClicked(e:MouseEvent) = { handle() }
    })
  }

これが、マウスイベントを処理したいコンポーネントが継承するClickEventHandlerです。addMouseListenerはComponentクラスが持っているので、このtraitもComponentクラスを継承しています。

このtraitには抽象変数handleが定義されています。このhandle変数は、マウスクリックイベントを処理する関数オブジェクトを、traitを実装するクラス側で定義しておくことで、addMouseListenerに渡すMouseAdapterの匿名クラスから利用しています。

このtraitを実装するクラスはどのようになるのでしょうか? サムネイル画像をクリックしたときのJLabelの処理を見てみましょう。

.. code-block:: scala

  val label = new JLabel( icon ) with ClickEventHandler{
    val handle = () => {new ViewWindow( f ); () }
  }

JLabelの匿名サブクラスを生成するときに、withキーワードでClickEventHandlerを継承させています
。
この匿名サブクラスに、クロージャとして抽象変数handleにViewWindowを生成する処理を実装することで、明示的にaddMouseListenerでイベントハンドラーを登録せずともクリック時の処理が行われるようになりました。

完成版のソースコード
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
2つのtraitを導入してリファクタリングした結果が、以下のコードです。

.. code-block:: scala

  import java.io.{File,FileFilter }
  import java.awt.event._
  import java.awt.{Component, Toolkit, Image,FlowLayout }
  import java.awt.image.BufferedImage
  import javax.swing.{ImageIcon, JWindow, JLabel, JFrame}
  import javax.imageio.ImageIO

  import scala.collection.jcl.Conversions._

  object ImageViewer extends Application{
    private val w = new ThumbWindow
  }

  trait ImageWindow extends JFrame{
    val w:Int
    val h:Int
    val t:String

    val pane = getContentPane

    def open = {
      setSize( w, h )
      setTitle( t )
      setVisible(true)
    }

    def createIcon( file:File )( f:(BufferedImage) => Image) = {
      val icon = new ImageIcon
      val image = ImageIO.read( file )

      icon.setImage( f(image) )
      (icon , image )
    }
  }

  trait ClickEventHandler extends Component{

    val handle: ()=> Unit
    addMouseListener(new MouseAdapter(){
      override def mouseClicked(e:MouseEvent) = { handle() }
    })
  }

  class ThumbWindow extends ImageWindow with ClickEventHandler {
    val handle = () => {System.exit( 0 )}
    val (t, w, h) = ( "Image Viewer", 640, 420 )

    val filePattern  = """.*\.(png|jpg|gif)$""".r

    def imageFiles( dir:File ) = dir.listFiles{
      new FileFilter{
        def accept( f:File ) = f.getName match {
          case filePattern( _ ) => true
          case _ => false
        }
      }
    }

    pane.setLayout( new FlowLayout() )

    imageFiles( new File( "./images" ) ).foreach{ f =>
      val (icon, image ) = createIcon( f ){ image =>
        image.getScaledInstance(100,100, Image.SCALE_AREA_AVERAGING)
      }

      val label = new JLabel( icon ) with ClickEventHandler{
        val handle = () => {new ViewWindow( f ); () }
      }
      label.setSize(100, 100)
      pane.add( label )

    }
    open
  }

  class ViewWindow( file:File ) extends ImageWindow
    with ClickEventHandler{
      val (icon, image ) = createIcon( file ){ image => image }

    val (t, w, h )= ( file.getName, image.getWidth, image.getHeight )
    val handle =  dispose _

    pane.add(new JLabel(icon))
    open
  }

これでこの画像ビューワーアプリケーションは完成ですが、余裕があれば皆さんで機能追加をしてみてください。例えば、こんなことが考えられます。

* サムネイルと一緒に画像ファイル名も表示する
* FileChooserなどで、任意のディレクトリを表示できるようにする
* 表示した画像を回転や反転できるようにする

