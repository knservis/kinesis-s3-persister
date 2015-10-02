Kinesis S3 persister
====================

This is esentially a cut down and meddled version of the amazon-kinesis-connector S3 example here: https://github.com/awslabs/amazon-kinesis-connectors/tree/master/src/main/samples

There are some slight modifications:
1. It creates a fat jar using maven. 
2. You can pass an s3 URI (s3://my-bucket/config.properties) for the config.
3. You can build it into a docker container.
4. It will add a newline after every event. 
5. It will add an expiration of 14 days to the s3 object and AES SSE encryption. 

As it stands now, the persister will read every message as a single string and wirte it out to S3.