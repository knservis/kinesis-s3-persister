Kinesis S3 persister
====================

This is esentially a cut down and meddled version of the amazon-kinesis-connector S3 example here: https://github.com/awslabs/amazon-kinesis-connectors/tree/master/src/main/samples

There is three slight modifications:
1. It creates a fat jar using maven. 
2. You can pass an s3 URI (s3://my-bucket/config.properties) for the config.
3. You can build it into a docker container.