package org.infinispan.tutorial.simple.remote.opentelemetry;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.StringConfiguration;
import org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.infinispan.tutorial.simple.connect.TutorialsConnectorHelper.TUTORIAL_CACHE_NAME;

/**
 * Shows how to enable tracing programmatically in a cache, sends operations and traces
 * are displayed in Jaeger.
 *
 * Execute after running the docker-compose script in the docker-compose directory.
 */
public class InfinispanRemoteOpenTelemetry {
   public static void main(String[] args) {
      ConfigurationBuilder builder = TutorialsConnectorHelper.connectionConfig();

      try (RemoteCacheManager client = TutorialsConnectorHelper.connect(builder)) {
         RemoteCache<String, String> cache = client.getCache(TUTORIAL_CACHE_NAME);
         RemoteCache<String, String> cache2 = client.administration().getOrCreateCache("test2", new StringConfiguration("<distributed-cache name=\"test\" mode=\"SYNC\" statistics=\"true\">\n" +
                 "\t<encoding media-type=\"application/x-protostream\"/>\n" +
                 "\t<tracing enabled=\"true\"/>\n" +
                 "</distributed-cache>"));

         // Enable tracing at runtime by changing the configuration.
         client.administration()
                 .updateConfigurationAttribute(cache.getName(), "tracing.enabled", "true");

         for (int i = 0; i < 3000000; i++) {

            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            System.out.println("Inserting two  cache in test and one in test2: " + key + " -> " + value);

            cache.put(key+"i",value);
            cache.put(key, value);
            cache2.put(key,value);

/*            try {
               Thread.sleep(ThreadLocalRandom.current().nextInt(2, 120));
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }*/

            cache.get(key);
            cache2.get(key);
/*            try {
               Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }*/

         }

         client.stop();
      }
   }
}