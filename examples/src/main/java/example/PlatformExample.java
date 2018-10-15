package example;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.influxdata.platform.PlatformClient;
import org.influxdata.platform.PlatformClientFactory;
import org.influxdata.platform.WriteClient;
import org.influxdata.platform.annotations.Column;
import org.influxdata.platform.annotations.Measurement;
import org.influxdata.platform.domain.Authorization;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Organization;
import org.influxdata.platform.domain.Permission;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.write.Point;

/*
  InfluxPlatform OSS2.0 onboarding prequisities (create default user, organization and bucket) :

  curl -i -X POST http://localhost:9999/api/v2/setup -H 'accept: application/json' \
      -d '{
              "username": "my-user",
              "password": "my-password",
              "org": "my-org",
              "bucket": "my-bucket"
          }'
 */

@SuppressWarnings("CheckStyle")
public class PlatformExample {

    @Measurement(name = "h2o")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }

    public static void main(final String[] args) {

        PlatformClient platform = PlatformClientFactory.create("http://localhost:9999",
            "my-user", "my-user".toCharArray());

        Organization medicalGMBH = platform.createOrganizationClient().createOrganization("Medical Corp");

        //
        // Create New Bucket with retention 1h
        //
        Bucket temperatureBucket = platform.createBucketClient()
            .createBucket("temperature-sensors", "1h", medicalGMBH.getName());

        //
        // Add Permissions to read and write to the Bucket
        //
        String bucketResource = Permission.bucketResource(temperatureBucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.WRITE_ACTION);

        User loggedUser = platform.createUserClient().me();
        Authorization authorization = platform.createAuthorizationClient()
            .createAuthorization(loggedUser, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();
        System.out.println("The token to write to temperature-sensors bucket " + token);

//        String token = "VeqpLgMq7d-zZ02jcOeetw75qwpi7XbEikxRIOrFXtTHkNl0HspG7SrO5J9O9-_mdy5BbTp56aVqN_Zzf2JfEw==";

        PlatformClient client = PlatformClientFactory.create("http://localhost:9999", token.toCharArray());

        WriteClient writeClient = client.createWriteClient();

        //
        // Write by POJO
        //
        Temperature temperature = new Temperature();
        temperature.location = "south";
        temperature.value = 62D;
        temperature.time = Instant.now();
        writeClient.writeMeasurement("temperature-sensors", "Medical Corp", TimeUnit.NANOSECONDS, temperature);

        //
        // Write by Point
        //
        Point point = Point.name("temperature")
            .addTag("location", "west")
            .addField("value", 55D)
            .time(Instant.now().toEpochMilli(), TimeUnit.MILLISECONDS);
        writeClient.writePoint("temperature-sensors", "Medical Corp", point);

        //
        // Write by LineProtocol
        //
        String record = "temperature,location=north value=60.0";
        writeClient.writeRecord("temperature-sensors", "Medical Corp", TimeUnit.NANOSECONDS, record);
    }


}
