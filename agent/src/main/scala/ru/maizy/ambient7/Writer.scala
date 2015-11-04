package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
trait Writer {
  def onInit(): Unit
  def write(event: Event): Unit
}
