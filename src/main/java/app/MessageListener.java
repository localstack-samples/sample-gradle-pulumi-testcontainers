package app;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class MessageListener {

  private final StorageService storageService;
  private final ApplicationProperties properties;

  public MessageListener(
    StorageService storageService,
    ApplicationProperties properties
  ) {
    this.storageService = storageService;
    this.properties = properties;
  }

  @SqsListener(queueNames = { "${queue}" })
  public void handle(Message message) {
    String bucketName = this.properties.bucket();
    String key = message.uuid().toString();
    ByteArrayInputStream is = new ByteArrayInputStream(
      message.content().getBytes(StandardCharsets.UTF_8)
    );
    this.storageService.upload(bucketName, key, is);
  }
}
