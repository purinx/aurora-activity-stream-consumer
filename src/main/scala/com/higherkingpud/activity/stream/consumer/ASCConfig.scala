package com.higherkingpud.activity.stream.consumer

case class ASCConfig(
    streamName: String,
    appName: String,
    dbcResourceId: String,
    aws: AWS,
)

case class AWS(
    accessKey: String,
    secretKey: String
)
