package iac;

import app.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = app.Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "server.port=8080"})
@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)

public class ApplicationTest {

    private final TestRestTemplate restTemplate= new TestRestTemplate();

    private static final String STACK_NAME = "dev";

    static File WORK_DIR;

    @Container
    public static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5.0"))
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
            .withEnv("PULUMI_CONFIG_PASSPHRASE","lsdevtest")
            .withEnv("PULUMI_BACKEND_URL","file://`pwd`/myproj");


    static {
        localStack.start();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.endpoint", () -> localStack.getEndpoint());
        registry.add("spring.cloud.aws.s3.endpoint", () -> localStack.getEndpoint());
    }
    @Before
    public void setup() throws IOException, InterruptedException {
        WORK_DIR = new File(".");
        PulumiLocalStackAdapter.configure(localStack, STACK_NAME, WORK_DIR);

        PulumiLocalStackAdapter.init(WORK_DIR);

        PulumiLocalStackAdapter.up(WORK_DIR);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        PulumiLocalStackAdapter.clean(WORK_DIR);
    }

    @Test
    public void testPostAndGetMessage() {
        // Create a unique UUID and the message content
        UUID uuid = UUID.randomUUID();
        String content = "Hello, World!";

        // Define the message object
        Message message = new Message(uuid, content);

        // Define the URL for the POST request
        String postUrl = "http://localhost:8080/api/messages";

        // Send the POST request to save the message
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Message> request = new HttpEntity<>(message, headers);
        ResponseEntity<String> postResponse = restTemplate.exchange(postUrl, HttpMethod.POST, request, String.class);

        // Assert that the POST request was successful (HTTP 201 Created)
        assertThat(postResponse.getStatusCode().is2xxSuccessful()).isTrue();

        // Define the URL for the GET request
        String getUrl = "http://localhost:8080/api/messages/" + uuid;

        // Send the GET request to retrieve the message
        ResponseEntity<java.util.Map> getResponse = restTemplate.exchange(getUrl, HttpMethod.GET, null, java.util.Map.class);

        // Assert that the GET request was successful and the content matches
        assertThat(getResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().get("content")).isEqualTo(content);

    }

}