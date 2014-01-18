package utils

import scala.util.matching.Regex
import views.html.bbsFieldConstructor

trait InputFormat {
  def format(source: String): String
}

object HtmlEscapeFormatter extends InputFormat {
  def format(source: String) = play.api.templates.HtmlFormat.escape(source).body
}

object LineBreakFormatter extends InputFormat {
  def format(source: String) = source.replaceAll("\r\n|\n|\r", "<br>")
}

object HyperLinkFormatter extends InputFormat {
  def format(source: String) = {
    val urlPtn = """((https?|ftp)://[a-zA-Z0-9_/%#$&?()~_.=+-:]+)""".r
    val imgPtn = """(.*/(.*(\.png|\.jpe?g|\.gif)))""".r
    urlPtn.replaceAllIn(source,
      m => {
        val url = m.group(1);
        url match {
          case imgPtn(_, imgName, _) => "<img src=\"%s\" alt=\"%s\">".format(url, imgName)
          case _ => "<a target=\"__blank\" href=\"%1$s\">%1$s</a>".format(url)
        }
      })

  }
}

/**
 * エスケープ処理後に改行、リンクなどの文字列置換を行う。
 */
object ExFormats {
  val formatters = HtmlEscapeFormatter :: LineBreakFormatter :: HyperLinkFormatter :: Nil
  def format(source: String) = formatters.foldLeft(source)((result, formatter) => formatter.format(result))
}

object BbsFieldHelper {
  import views.html.helper.FieldConstructor
  implicit val bbsFields = FieldConstructor(bbsFieldConstructor.f)
}
