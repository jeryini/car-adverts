# car-adverts
A RESTful web service for car adverts


Tested with Play 2.5.9 and Java 1.8.0-openjdk

### Download and run DynamoDB Local

Info here:
http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html

Download from here:
http://dynamodb-local.s3-website-us-west-2.amazonaws.com/dynamodb_local_latest

Extract the archive and run:

    java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar

### Download Play Framework

http://www.playframework.com/download

### Run the app
Run the following in project root:

    sbt testProd
    
Don't run just `sbt run`, as otherwise the startup 