package org.infinispan.tutorial.simple.connect;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.server.test.core.InfinispanContainer;

/**
 * Utility class for the simple tutorials in client server mode.
 *
 * @author Katia Aresti, karesti@redhat.com
 */
public class TutorialsConnectorHelper {

   public static final String USER = "admin";
   public static final String PASSWORD = "password";
   public static final String HOST = "127.0.0.1";
   public static final int SINGLE_PORT = ConfigurationProperties.DEFAULT_HOTROD_PORT;

   public static final String TUTORIAL_CACHE_NAME = "test";
   public static final String TUTORIAL_CACHE_CONFIG =
         "<distributed-cache name=\"CACHE_NAME\" statistics=\"true\">\n"
         + "    <encoding media-type=\"application/x-protostream\"/>\n"
         + "</distributed-cache>";

   /**
    * Returns the configuration builder with the connection information
    *
    * @return a Configuration Builder with the connection config
    */
   public static final ConfigurationBuilder connectionConfig() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.security()
            .authentication()
            //Add user credentials.
            .username(USER)
            .password(PASSWORD);

      // Make sure the remote cache is available.
      // If the cache does not exist, the cache will be created
      builder.remoteCache(TUTORIAL_CACHE_NAME)
            .configuration(TUTORIAL_CACHE_CONFIG.replace("CACHE_NAME", TUTORIAL_CACHE_NAME));
      return builder;
   }

   /**
    * Connect to the running Infinispan Server in localhost:11222.
    *
    * This method illustrates how to connect to a running Infinispan Server with a downloaded
    * distribution or a container.
    *
    * @return a connected RemoteCacheManager
    */
   public static final RemoteCacheManager connect() {
      // Return the connected cache manager
      return connect(connectionConfig());
   }

   public static InfinispanContainer INFINISPAN_CONTAINER;

   public static final RemoteCacheManager connect(ConfigurationBuilder builder) {
      RemoteCacheManager cacheManager = null;
      try {
         cacheManager = new RemoteCacheManager(builder.build());
         //ping
         System.out.println("Get cache names: " + cacheManager.getCacheNames());
      } catch (Exception ex) {
         System.out.println("Unable to connect to a running server in localhost:11222. Try test containers");
         if (cacheManager != null) {
            cacheManager.stop();
         }
         cacheManager = null;
      }

      if (cacheManager == null) {
         try {
            startInfinispanContainer();
            builder.addServer().host(HOST).port(INFINISPAN_CONTAINER.getMappedPort(SINGLE_PORT));
            cacheManager = new RemoteCacheManager(builder.build());
            //ping
            System.out.println("Get cache names: " + cacheManager.getCacheNames());
         } catch (Exception ex) {
            System.out.println("Infinispan Server start with Testcontainers failed. Exit");
            System.exit(0);
         }
      }
      if (cacheManager != null) {
         // Clear the cache in case it already exists from a previous running tutorial
         RemoteCache<Object, Object> testCache = cacheManager.getCache(TUTORIAL_CACHE_NAME);
         if (testCache != null) {
            testCache.clear();
         } else {
            System.out.println("Test cache does not exist");
         }
      }
      // Return the connected cache manager
      return cacheManager;
   }

   public static InfinispanContainer startInfinispanContainer() {
      return startInfinispanContainer(1000);
   }

   public static InfinispanContainer startInfinispanContainer(long millis) {
      try {
         INFINISPAN_CONTAINER = new InfinispanContainer();
         INFINISPAN_CONTAINER.withUser(USER);
         INFINISPAN_CONTAINER.withPassword(PASSWORD);
         INFINISPAN_CONTAINER.start();
         Thread.sleep(millis);
      } catch (Exception ex) {
         System.out.println("Unable to start Infinispan container");
         return null;
      }
      return INFINISPAN_CONTAINER;
   }

   public static boolean isContainerStarted() {
      return INFINISPAN_CONTAINER != null && INFINISPAN_CONTAINER.isRunning();
   }

   public static void stopInfinispanContainer() {
      try {
         if (INFINISPAN_CONTAINER != null) {
            INFINISPAN_CONTAINER.stop();
         }
      } catch (Exception ex) {
         System.out.println("Error stopping the container");
      }
   }

   public static void stop(RemoteCacheManager cacheManager) {
      if (cacheManager != null){
         cacheManager.stop();
         stopInfinispanContainer();
      }
   }

}
