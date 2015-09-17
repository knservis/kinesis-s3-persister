/*
 * Copyright 2013-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package sample;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.connectors.KinesisConnectorConfiguration;
import com.amazonaws.services.kinesis.connectors.KinesisConnectorExecutorBase;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * This class defines the execution of a Amazon Kinesis Connector.
 * 
 */
public abstract class KinesisConnectorExecutor<T, U> extends KinesisConnectorExecutorBase<T, U> {

    private static final Log LOG = LogFactory.getLog(KinesisConnectorExecutor.class);

    private static final String CREATE_DYNAMODB_DATA_TABLE = "createDynamoDBDataTable";
    private static final boolean DEFAULT_CREATE_RESOURCES = false;

    // Create Amazon DynamoDB Resource constants
    private static final String DYNAMODB_KEY = "dynamoDBKey";
    private static final String DYNAMODB_READ_CAPACITY_UNITS = "readCapacityUnits";
    private static final String DYNAMODB_WRITE_CAPACITY_UNITS = "writeCapacityUnits";
    private static final Long DEFAULT_DYNAMODB_READ_CAPACITY_UNITS = 1l;
    private static final Long DEFAULT_DYNAMODB_WRITE_CAPACITY_UNITS = 1l;

    // Class variables
    protected final KinesisConnectorConfiguration config;
    private final Properties properties;

    /**
     * Create a new KinesisConnectorExecutor based on the provided configuration (*.propertes) file.
     * 
     * @param configFile
     *        The name of the configuration file to look for on the classpath
     */
    public KinesisConnectorExecutor(String configFile) {
        URI s3uri = null;
        InputStream configStream;
        try {
            s3uri = new URI(configFile);
        } catch (URISyntaxException e) {
            //Failed to open s3 config. Continue attempting file.
        }
        if(s3uri != null && s3uri.getScheme().equals("s3")) {
            AmazonS3 s3Client = new AmazonS3Client();
            String path = s3uri.getPath();
            if(path.charAt(0) == '/') {
                path = path.substring(1);
            }
            LOG.info(String.format("Attempting to read configuration from bucket:%s object:%s", s3uri.getHost(), path));
            configStream = s3Client.getObject(new GetObjectRequest(s3uri.getHost(), path)).getObjectContent();
        }
        else {
            configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
        }

        if (configStream == null) {
            String msg = "Could not find resource " + configFile + " in the classpath";
            throw new IllegalStateException(msg);
        }
        properties = new Properties();
        try {
            properties.load(configStream);
            configStream.close();
        } catch (IOException e) {
            String msg = "Could not load properties file " + configFile + " from classpath";
            throw new IllegalStateException(msg, e);
        }
        this.config = new KinesisConnectorConfiguration(properties, getAWSCredentialsProvider());
        setupAWSResources();

        // Initialize executor with configurations
        super.initialize(config);
    }

    /**
     * Returns an {@link AWSCredentialsProvider} with the permissions necessary to accomplish all specified
     * tasks. At the minimum it will require read permissions for Amazon Kinesis. Additional read permissions
     * and write permissions may be required based on the Pipeline used.
     * 
     * @return AWSCredentialsProvider
     */
    public AWSCredentialsProvider getAWSCredentialsProvider() {
        return new DefaultAWSCredentialsProviderChain();
    }

    /**
     * Setup necessary AWS resources for the samples. By default, the Executor does not create any
     * AWS resources. The user must specify true for the specific create properties in the
     * configuration file.
     */
    private void setupAWSResources() {
        if (parseBoolean(CREATE_DYNAMODB_DATA_TABLE, DEFAULT_CREATE_RESOURCES, properties)) {
            String key = properties.getProperty(DYNAMODB_KEY);
            Long readCapacityUnits =
                    parseLong(DYNAMODB_READ_CAPACITY_UNITS, DEFAULT_DYNAMODB_READ_CAPACITY_UNITS, properties);
            Long writeCapacityUnits =
                    parseLong(DYNAMODB_WRITE_CAPACITY_UNITS, DEFAULT_DYNAMODB_WRITE_CAPACITY_UNITS, properties);
            createDynamoDBTable(key, readCapacityUnits, writeCapacityUnits);
        }
    }


    /**
     * Helper method to create the Amazon DynamoDB table.
     * 
     * @param key
     *        The name of the hashkey field in the Amazon DynamoDB table
     * @param readCapacityUnits
     *        Read capacity of the Amazon DynamoDB table
     * @param writeCapacityUnits
     *        Write capacity of the Amazon DynamoDB table
     */
    private void createDynamoDBTable(String key, long readCapacityUnits, long writeCapacityUnits) {
        LOG.info("Creating Amazon DynamoDB table " + config.DYNAMODB_DATA_TABLE_NAME);
        AmazonDynamoDBClient dynamodbClient = new AmazonDynamoDBClient(config.AWS_CREDENTIALS_PROVIDER);
        dynamodbClient.setEndpoint(config.DYNAMODB_ENDPOINT);
        DynamoDBUtils.createTable(dynamodbClient,
                config.DYNAMODB_DATA_TABLE_NAME,
                key,
                readCapacityUnits,
                writeCapacityUnits);
    }

    /**
     * Helper method used to parse boolean properties.
     * 
     * @param property
     *        The String key for the property
     * @param defaultValue
     *        The default value for the boolean property
     * @param properties
     *        The properties file to get property from
     * @return property from property file, or if it is not specified, the default value
     */
    private static boolean parseBoolean(String property, boolean defaultValue, Properties properties) {
        return Boolean.parseBoolean(properties.getProperty(property, Boolean.toString(defaultValue)));
    }

    /**
     * Helper method used to parse long properties.
     * 
     * @param property
     *        The String key for the property
     * @param defaultValue
     *        The default value for the long property
     * @param properties
     *        The properties file to get property from
     * @return property from property file, or if it is not specified, the default value
     */
    private static long parseLong(String property, long defaultValue, Properties properties) {
        return Long.parseLong(properties.getProperty(property, Long.toString(defaultValue)));
    }

}
