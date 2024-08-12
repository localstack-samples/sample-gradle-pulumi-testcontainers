package iac;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.aws.s3.Bucket;
import com.pulumi.aws.s3.BucketArgs;
import com.pulumi.aws.sqs.Queue;
import com.pulumi.aws.sqs.QueueArgs;

public class ApplicationStack {
    public static void main(String[] args) {

        Pulumi.run(ApplicationStack::createResources);
    }
    private static void createResources(Context ctx) {
        AwsConfig config = YamlConfigLoader.loadConfig("application.yml");

        var bucket = new Bucket(config.getBucket(),
                BucketArgs.builder()
                        .bucket(config.getBucket())
                        .forceDestroy(true)
                        .build());
        ctx.export("bucketArn", bucket.arn());

        var queue = new Queue(config.getQueue() + ".fifo", QueueArgs.builder()
                .name(config.getQueue() + ".fifo")
                .fifoQueue(true)
                .contentBasedDeduplication(true)
                .build());

        ctx.export("queueUrl", queue.url());

    }
}