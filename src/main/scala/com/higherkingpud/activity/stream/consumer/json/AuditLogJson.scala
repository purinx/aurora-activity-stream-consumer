package com.higherkingpud.activity.stream.consumer.json

case class AuditLogJson(
    `type`: String,
    clusterId: String,
    instanceId: String,
    databaseActivityEventList: Seq[ActivityEventJson]
)
