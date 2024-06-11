package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString

/**
 * ios 通过字符串转换为HTML带样式CharSequence字符.
 */
private fun String.fromHtmlStrToAnnotatedStringByIos():CharSequence{
  // TODO 通过字符串转换为HTML带样式CharSequence字符
  return this
}
/**
 * Android and JVM can have different WebView or HTML rendering implementations.
 * We are leaving HTML rendering to platform side.
 */
@Composable
internal actual fun RichTextScope.HtmlBlock(content: String) {
  val richTextString = remember(content) {
    richTextString {
      withAnnotatedString {
        append(content.fromHtmlStrToAnnotatedStringByIos())
      }
    }
  }
  Text(richTextString)
}
