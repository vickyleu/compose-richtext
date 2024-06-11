package com.halilibo.richtext.commonmark

import cmark.*
import cmark.cmark_list_type.CMARK_ORDERED_LIST
import com.halilibo.richtext.markdown.node.AstBlockQuote
import com.halilibo.richtext.markdown.node.AstCode
import com.halilibo.richtext.markdown.node.AstDocument
import com.halilibo.richtext.markdown.node.AstEmphasis
import com.halilibo.richtext.markdown.node.AstFencedCodeBlock
import com.halilibo.richtext.markdown.node.AstHardLineBreak
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstHtmlBlock
import com.halilibo.richtext.markdown.node.AstHtmlInline
import com.halilibo.richtext.markdown.node.AstImage
import com.halilibo.richtext.markdown.node.AstIndentedCodeBlock
import com.halilibo.richtext.markdown.node.AstLink
import com.halilibo.richtext.markdown.node.AstListItem
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstNodeLinks
import com.halilibo.richtext.markdown.node.AstNodeType
import com.halilibo.richtext.markdown.node.AstOrderedList
import com.halilibo.richtext.markdown.node.AstParagraph
import com.halilibo.richtext.markdown.node.AstSoftLineBreak
import com.halilibo.richtext.markdown.node.AstStrongEmphasis
import com.halilibo.richtext.markdown.node.AstText
import com.halilibo.richtext.markdown.node.AstThematicBreak
import com.halilibo.richtext.markdown.node.AstUnorderedList
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toKString

internal fun convert(
  node: CPointer<cmark_node>?,
  parentNode: AstNode? = null,
  previousNode: AstNode? = null,
): AstNode? {
  node ?: return null

  val nodeLinks = AstNodeLinks(
    parent = parentNode,
    previous = previousNode
  )

  val newNodeType: AstNodeType? = when (cmark_node_get_type(node)) {
    CMARK_NODE_BLOCK_QUOTE -> AstBlockQuote
    CMARK_NODE_LIST
    -> {
      when (cmark_node_get_list_type(node)) {
        CMARK_ORDERED_LIST/*CMARK_NODE_ORDERED_LIST*/ -> AstOrderedList( //CMARK_NODE_ORDERED_LIST 不存在
          startNumber = cmark_node_get_list_start(node).toInt(),
          delimiter = '.'
        )

        else -> AstUnorderedList(bulletMarker = '*')
      }
    }

    CMARK_NODE_CODE -> AstCode(literal = cmark_node_get_literal(node)!!.toKString())
    CMARK_NODE_DOCUMENT -> AstDocument
    CMARK_NODE_EMPH -> AstEmphasis(delimiter = "_")
    CMARK_NODE_CODE_BLOCK -> AstFencedCodeBlock(
      literal = cmark_node_get_literal(node)!!.toKString(),
      fenceChar = '`',
      fenceIndent = 0,
      fenceLength = 3,
      info = cmark_node_get_fence_info(node)!!.toKString()
    )

    CMARK_NODE_LINEBREAK/*CMARK_NODE_HARD_BREAK*/ -> AstHardLineBreak //CMARK_NODE_HARD_BREAK 不存在
    CMARK_NODE_HEADING -> AstHeading(level = cmark_node_get_heading_level(node))
    CMARK_NODE_THEMATIC_BREAK -> AstThematicBreak
    CMARK_NODE_HTML_INLINE -> AstHtmlInline(literal = cmark_node_get_literal(node)!!.toKString())
    CMARK_NODE_HTML_BLOCK -> AstHtmlBlock(literal = cmark_node_get_literal(node)!!.toKString())
    CMARK_NODE_IMAGE -> {
      val destination = cmark_node_get_url(node)?.toKString()
      if (destination == null) {
        null
      } else {
        AstImage(
          title = cmark_node_get_title(node)?.toKString() ?: "",
          destination = destination
        )
      }
    }

    CMARK_NODE_CUSTOM_BLOCK/*CMARK_NODE_INDENTED_CODE_BLOCK*/ ->  //CMARK_NODE_INDENTED_CODE_BLOCK 不存在
      AstIndentedCodeBlock(literal = cmark_node_get_literal(node)!!.toKString())

    CMARK_NODE_LINK -> {
      AstLink(
        title = cmark_node_get_title(node)?.toKString() ?: "",
        destination = cmark_node_get_url(node)!!.toKString()
      )
//      when(cmark_node_get_type(node)){
//
//        CMARK_NODE_LINK_REFERENCE -> AstLinkReferenceDefinition( //CMARK_NODE_LINK_REFERENCE 不存在
//          title = cmark_node_get_title(node)?.toKString() ?: "",
//          destination = cmark_node_get_url(node)!!.toKString(),
//          label = cmark_node_get_label(node)!!.toKString()
//        )
//        else -> null
//      }
//      /*CMARK_NODE_LINK_REFERENCE_DEF*/ -> AstLinkReferenceDefinition( //CMARK_NODE_LINK_REFERENCE_DEF 不存在
//        title = cmark_node_get_title(node)?.toKString() ?: "",
//        destination = cmark_node_get_url(node)!!.toKString(),
//        label = cmark_node_get_type_string(node)!!.toKString()//cmark_node_get_label(node)!!.toKString() //cmark_node_get_label cmark_node_get_type_string(node)
//      )
//      AstLink(
//        title = cmark_node_get_title(node)?.toKString() ?: "",
//        destination = cmark_node_get_url(node)!!.toKString()
//      )
    }

    CMARK_NODE_ITEM -> AstListItem

    CMARK_NODE_PARAGRAPH -> AstParagraph
    CMARK_NODE_SOFTBREAK /*CMARK_NODE_SOFT_BREAK*/ -> AstSoftLineBreak //CMARK_NODE_SOFT_BREAK 不存在
    CMARK_NODE_STRONG -> AstStrongEmphasis(delimiter = "**")
    CMARK_NODE_TEXT -> AstText(literal = cmark_node_get_literal(node)!!.toKString())

    else -> null
  }

  val newNode = newNodeType?.let {
    AstNode(newNodeType, nodeLinks)
  }

  newNode?.links?.firstChild =
    convert(cmark_node_first_child(node), parentNode = newNode, previousNode = null)
  newNode?.links?.next =
    convert(cmark_node_next(node), parentNode = parentNode, previousNode = newNode)

  if (cmark_node_next(node) == null) {
    parentNode?.links?.lastChild = newNode
  }

  return newNode
}

/**
 * A helper class that can convert any text content into an ASTNode tree and return its root.
 */
public actual class CommonmarkAstNodeParser actual constructor(private val options: MarkdownParseOptions) {

  /**
   * Parse markdown content and return Abstract Syntax Tree(AST).
   *
   * @param text Markdown text to be parsed.
   * @param options Options for the Commonmark Markdown parser.
   */
  actual fun parse(text: String): AstNode {
    // 参考android 代码实现类似的cmark_parse_document,MarkdownParseOptions只有一个autolink属性
    val parseOptions =
      if (options.autolink) CMARK_OPT_DEFAULT or CMARK_OPT_UNSAFE else CMARK_OPT_DEFAULT
    val commonmarkNode = cmark_parse_document(text, text.length.toULong(), parseOptions)
      ?: throw IllegalArgumentException("Could not parse the given text content into a meaningful Markdown representation!")

    return convert(commonmarkNode)
      ?: throw IllegalArgumentException("Could not convert the generated Commonmark Node into an ASTNode!")
  }
}