package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.aghajari.compose.text.fromHtml
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString

@Composable
internal actual fun RichTextScope.HtmlBlock(content: String) {
  val richTextString = remember(content) {
    richTextString {
      withAnnotatedString {
        append(content.fromHtml().annotatedString)
      }
    }
  }
  Text(richTextString)
}
