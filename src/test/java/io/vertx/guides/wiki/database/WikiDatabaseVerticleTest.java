package io.vertx.guides.wiki.database;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class WikiDatabaseVerticleTest {
  private Vertx vertx;
  private WikiDatabaseService service;

  @Before
  public void prepare(TestContext context) {
    vertx = Vertx.vertx();
    JsonObject conf = new JsonObject()
      .put(WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:mem:testdb;shutdown=true")
      .put(WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 4);

    vertx.deployVerticle(new WikiDatabaseVerticle(), new DeploymentOptions().setConfig(conf),
      context.asyncAssertSuccess(id ->
        service = WikiDatabaseService.createProxy(vertx, WikiDatabaseVerticle.CONFIG_WIKIDB_QUEUE)
      )
    );
  }

  @Test
  public void crudOperations(TestContext context) {
    Async async = context.async();

    service.createPage("Test", "Dummy content", context.asyncAssertSuccess(v1 -> {
      service.fetchPage("Test", context.asyncAssertSuccess(json1 -> {
        context.assertTrue(json1.getBoolean("found"));
        context.assertTrue(json1.containsKey("id"));
        context.assertEquals("Dummy content", json1.getString("rawContent"));

        service.savePage(json1.getInteger("id"), "Changed", context.asyncAssertSuccess(v2 -> {
          service.fetchAllPages(context.asyncAssertSuccess(array1 -> {
            context.assertEquals(1, array1.size());

            service.fetchPage("Test", context.asyncAssertSuccess(json2 -> {
              context.assertEquals("Changed", json2.getString("rawContent"));

              service.deletePage(json1.getInteger("id"), v3 -> {
                service.fetchAllPages(context.asyncAssertSuccess(array2 -> {
                  context.assertTrue(array2.isEmpty());
                  async.complete();
                }));
              });
            }));
          }));
        }));
      }));
    }));
  }

  @After
  public void finish(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
}
