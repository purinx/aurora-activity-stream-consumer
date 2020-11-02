# aurora-activity-stream-consumer

Aurora(MySQL) Activity Stream Consumer with Akka-stream

## Setup

- Enable *Activity Stream* of your Aurora MySQL.
- Create `application.conf` following `application.conf.dist`. Only todo is setting your AWS IAM access key and secret.
- Boot consumer. `sbt run`

