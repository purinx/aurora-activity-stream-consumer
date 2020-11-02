package com.higherkingpud.activity.stream.consumer.json

case class MonitoringRecordJson(
                                 `type`: String,
                                 version: String,
                                 databaseActivityEvents: String, // Base64エンコード & 暗号化された json 文字列
                                 key: String
                               )
