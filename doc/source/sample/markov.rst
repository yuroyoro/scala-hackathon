
Twitterマルコフ連鎖
------------------------------

Yahoo形態素解析APIを利用して、Twitterの発言内容をRSSで取得してマルコフ連鎖するサンプル。

.. code-block:: scala

  import scala.xml._
  import scala.io.Source

  /**
   * Feedを取得するObject
   */
  object FeedCrawler {

      /**
       * URLからFeedを取得して、scala.xml.Nodeオブジェクトで返します。
       */
      def get( url:String ) = XML.loadString(
                Source.fromURL( url,  "utf-8").getLines.mkString)

      /**
       * URLからFeedを取得して、<content:encoded>タグの内容を文字列で返します。
       */
      def getContent( url:String ) = get( url ) \\ "encoded" map {
        node =>
          """<("[^"]*"|'[^']*'|[^'">])*>""".r.replaceAllIn( node.text , "")
      }

      def getSentence( url:String ) = getContent( url ).flatMap{
        _.lines.toList.filter{ _.trim.length != 0 }.map{ _.trim}}
  }

  case class Morpheme( word:String, pos:String )

  /**
   * Yahoo形態素解析APIを利用して形態素解析を行うObject
   */
  object MorphologicalAnalyser {
    import java.net.URLEncoder

    val apikey = "E5JE6uuxg64mzybiFvlbeXLmDyw3K1f.Kpj0D.W5JMQdXdMB98muWuy9PUqGOLiFIRmuplc-"
    val url = "http://jlp.yahooapis.jp/DAService/V1/parse?appid=%s&sentence=%s"

    /**
     * 形態素解析を行い、品詞分割されたListを返します。
     */
    def apply( s:String ) = XML.loadString(
                Source.fromURL(
                  url.format( apikey, URLEncoder.encode(s , "utf-8")), "utf-8").
                getLines.mkString) \\ "Morphem" map{ m =>
                  Morpheme( m \\ "Surface"  text, m \\ "POS" text) }

  }

  /**
   * 与えられた文章からマルコフ連鎖します。
   */
  class Markov( sentence:Seq[String] ) {
    import scala.collection.mutable.{Map, ListBuffer }
    import scala.util.Random

    val rnd = new Random
    val maxKeyCnt = 3
    val dict:Map[(Morpheme, Morpheme), List[Morpheme]] = Map.empty
    val terminater = new ListBuffer[(Morpheme, Morpheme)]

    print("解析中")
    sentence.foreach{ s =>
      print(".")
      MorphologicalAnalyser( s ) match {
        case ms if ms.length < 2 =>
        case ms =>
          terminater += ((ms(0), ms(1)) /: ms.drop(2) ){
            case (key, m ) => dict.get(key) match{
              case None => dict += key -> (m :: Nil)
              case Some( xs ) => dict += key -> (m :: xs)
            }
            if( m.pos == "特殊" ) terminater +=  (key._2, m)
            ( key._2, m )
          }
      }
    }
    println("done.")

    def generate( n:Int ):String = {
      if( n == 0 ) ""
      else {
        val (m1, m2) = terminater( rnd.nextInt( terminater.length ) )
        val selected = Map[(Morpheme,  Morpheme),Int]( (m1, m2) -> 1 )

        def gen( key:(Morpheme, Morpheme) ):String = {
          val selectedCnt = selected.get( key ).getOrElse(0) + 1
          if( selectedCnt >= maxKeyCnt ) ""
          else{
            selected += key -> selectedCnt
            dict.get( key ) match {
              case None => ""
              case Some( Nil ) => ""
              case Some( xs ) => {
                val m = xs( rnd.nextInt( xs.length ) )
                m.word + gen(( key._2, m))
              }
            }
          }
        }

        m1.word + m2.word + gen( (m1, m2)) + generate( n - 1 )
      }
    }
  }

  object Main{
    def main( args:Array[String] ) = {
      println("Feed(RSS1.0/RSS2.0)のURLを入力してください。")
      print(" > ")
      val url = Console.readLine
      val sentence = FeedCrawler.getSentence( url )
      val markov = new Markov( sentence )

      val num = """(\d+)""".r
      def cmd:Unit = {
        println("生成する文章の長さを入力してください。")
        print(" > ")
        Console.readLine match {
          case "quit" => System.exit(0)
          case num(cnt) => {
            println( markov.generate( cnt.toInt ) );
            cmd
          }
          case _ => cmd
        }
      }
      cmd
    }
  }

