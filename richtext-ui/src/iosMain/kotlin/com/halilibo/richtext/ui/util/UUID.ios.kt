package com.halilibo.richtext.ui.util

import platform.Foundation.NSUUID

internal actual fun randomUUID(): String {
  //   return UUID.randomUUID().toString()
  // ios 中 类似 UUID 类的生成方式
  return NSUUID().UUIDString
}