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
