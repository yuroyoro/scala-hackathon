import java.io.{File,FileFilter }
import java.awt.event._
import java.awt.{Toolkit, Image,FlowLayout }
import java.awt.image.BufferedImage
import javax.swing.{ImageIcon, JWindow, JLabel, JFrame}
import javax.imageio.ImageIO

import scala.collection.jcl.Conversions._

object ImageViewer extends Application{
  private val w = new ThumbWindow
}

class ThumbWindow extends JFrame{
  val filePattern  = """.*\.(png|jpg|gif)$""".r

  def imageFiles( dir:File ) = dir.listFiles{
    new FileFilter{
      def accept( f:File ) = f.getName match {
        case filePattern( _ ) => true
        case _ => false
      }
    }
  }

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

    label.addMouseListener( new MouseAdapter(){
      override def mouseClicked(e:MouseEvent){
        new ViewWindow( f )
      }
    })

    pane.add( label )

  }

  addMouseListener(new MouseAdapter(){
    override def mouseClicked(e:MouseEvent){ System.exit(0) }
  })

  setTitle( "Image Viewer")
  setSize( 640, 420)
  setVisible( true )
}

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

