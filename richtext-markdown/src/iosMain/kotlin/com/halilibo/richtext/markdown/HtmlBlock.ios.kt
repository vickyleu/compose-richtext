package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSMutableAttributedString
import platform.Foundation.create
import platform.Foundation.length
import platform.UIKit.NSDocumentTypeDocumentAttribute
import platform.UIKit.NSHTMLTextDocumentType
import platform.UIKit.create

/**
 * ios 通过字符串转换为HTML带样式CharSequence字符.
 */
class AttributedStringWrapper(private val attributedString: NSMutableAttributedString) :
  CharSequence {
  override val length: Int
    get() = attributedString.length.toInt()

  override fun get(index: Int): Char {
    return attributedString.string[index]
  }

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
    return attributedString.string.subSequence(startIndex, endIndex)
  }

  override fun toString(): String {
    return attributedString.string
  }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun String.fromHtmlStrToAnnotatedStringByIos(): CharSequence {
  val data = this.encodeToByteArray().toNSData()
  val options = mapOf<Any?, Any?>(NSDocumentTypeDocumentAttribute to NSHTMLTextDocumentType)

  return memScoped {
    val errorPtr: ObjCObjectVar<NSError?> = alloc<ObjCObjectVar<NSError?>>()
    val attributedString = NSMutableAttributedString.create(
      data = data,
      options = options,
      documentAttributes = null,
      error = errorPtr.ptr
    ) ?: run {
      errorPtr.value?.let {
        println("Error converting HTML to NSAttributedString: ${it.localizedDescription}")
      }
      nativeHeap.free(errorPtr.rawPtr)
      return@memScoped this@fromHtmlStrToAnnotatedStringByIos
    }

    errorPtr.value?.let {
      println("Error converting HTML to NSAttributedString: ${it.localizedDescription}")
    }
    nativeHeap.free(errorPtr.rawPtr)
    AttributedStringWrapper(attributedString)
  }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData = this.usePinned {
  NSData.create(bytes = it.addressOf(0), length = this.size.convert())
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
