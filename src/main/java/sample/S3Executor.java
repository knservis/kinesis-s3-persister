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

import com.amazonaws.services.kinesis.connectors.KinesisConnectorRecordProcessorFactory;

/**
 * Executor to emit records to Amazon S3 files. The number of records per Amazon S3 file can be set in the buffer
 * properties.
 */
public class S3Executor extends KinesisConnectorExecutor<String, byte[]> {
    private static final String CONFIG_FILE = "S3Sample.properties";

    /**
     * Creates a new S3Executor.
     * 
     * @param configFile
     *        The name of the configuration file to look for on the classpath
     */
    public S3Executor(String configFile) {
        super(configFile);
    }

    @Override
    public KinesisConnectorRecordProcessorFactory<String, byte[]>
            getKinesisConnectorRecordProcessorFactory() {
        return new KinesisConnectorRecordProcessorFactory<>(new S3Pipeline(), this.config);
    }

    /**
     * Main method to run the S3Executor.
     * 
     * @param args
     */
    public static void main(String[] args) {
        String configFile = CONFIG_FILE;
        if(args.length > 0) {
            configFile = args[0];
        }
        KinesisConnectorExecutor<String, byte[]> s3Executor = new S3Executor(configFile);
        s3Executor.run();
    }
}
