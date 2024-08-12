package iac;

import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertTrue;

@Testcontainers
public class IaCStackTest {

    private static final String STACK_NAME = "dev";

    @Container
    public static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5.0"))
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
            .withEnv("PULUMI_CONFIG_PASSPHRASE","lsdevtest")
            .withEnv("PULUMI_BACKEND_URL","file://`pwd`/myproj");

    static File WORK_DIR;

    private S3Client s3Client;

    static {
        localStack.start();
    }

    @Before
    public void setup() throws IOException {
        URI lsEndpoint = localStack.getEndpoint();
        WORK_DIR = new File(".");
        PulumiLocalStackAdapter.configure(localStack, STACK_NAME, WORK_DIR);

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("test", "test");

        s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(lsEndpoint)
                .region(Region.of("us-east-1"))
                .build();
    }


    @Test
    public void testBucketCreation() throws IOException, InterruptedException {

        PulumiLocalStackAdapter.init(WORK_DIR);

        PulumiLocalStackAdapter.up(WORK_DIR);


        var buckets = s3Client.listBuckets().buckets();
        assertTrue(buckets.stream().anyMatch(b -> b.name().equals("6578-demo-message-bucket")));

        PulumiLocalStackAdapter.clean(WORK_DIR);

    }
}