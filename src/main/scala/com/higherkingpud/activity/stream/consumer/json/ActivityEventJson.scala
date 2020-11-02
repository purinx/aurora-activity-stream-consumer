package com.higherkingpud.activity.stream.consumer.json

case class ActivityEventJson(
                              `class`: String,
                              command: String,
                              commandText: String, // command が MAIN の場合 SQL が入ってる
                              databaseName: String,
                              dbProtocol: String,
                              dbUserName: String,
                              errorMessage: Option[String] // エラーの場合のみ
                            )
