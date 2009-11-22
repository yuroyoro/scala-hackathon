import java.io.{File,FileFilter }
import java.awt.{Image,FlowLayout }
import java.awt.image.BufferedImage
import javax.swing.{ImageIcon, JWindow, JLabel}
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

