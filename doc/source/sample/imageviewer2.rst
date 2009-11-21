クリックすると画像表示ウィンドウを表示させる
____________________________________________

さて次は、画像がクリックされたら表示するウィンドウを出すようにしてみます。

クラスに分割する
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
サムネイル用ウィンドウは、匿名オブジェクトとしてmainの処理内で実装してました。これから表示用ウィンドウも生成するので、この際クラスを分割することにします。

.. code-block:: scala

  object ImageViewer extends Application{
    private val w = new ThumbWindow
  }

  class ThumbWindow extends JFrame{
    val filePattern  = """.*\.(png|jpg|gif)$""".r

    // 中略

    setTitle( "Image Viewer")
    setSize( 640, 420)
    setVisible( true )
  }

main関数をもつImageViewerオブジェクトと、サムネイルを表示するThumbWindowクラスに分割しました。ThumbWindowクラスの内容は、先ほど匿名クラス内に記述した内容を移動させただけです。

では、画像を表示するViewWindowクラスを作成します。このクラスは、コンストラクタに表示する画像ファイルを受け取り、ImageIconとJLabelを利用して画像を表示します。ウィンドウをクリックするとこのウィンドウを閉じるようにしています。

.. code-block:: scala

  class ViewWindow( file:File ) extends JFrame{
    val icon = new ImageIcon
    val image = ImageIO.read( file )
    icon.setImage( image )

    getContentPane.add(new JLabel(icon))
    setSize( image.getWidth, image.getHeight )

    setTitle( file.getName )
    setVisible(true)

    addMouseListener(new MouseAdapter(){
      override def mouseClicked(e:MouseEvent){ dispose }
    })
  }

最後に、サムネイル画像をクリックすると、このViewWindowクラスを生成する処理を実装すれば完成です。

ThumbWindow内で、サムネイルを表示しているJLabelクラスに、addMouseListenerを利用して処理を追加します。

.. code-block:: scala

  label.addMouseListener( new MouseAdapter(){
    override def mouseClicked(e:MouseEvent){
      new ViewWindow( f )
    }
  })

ここで追加しているMouseAdapterの匿名クラスは、foreachで受けてる変数fを利用しています。これは、クロージャの形になっているのです。

