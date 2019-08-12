package io.vertx.guides.wiki.http;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SampleHttpServerTest {
  private Vertx vertx;

  @Before
  public void prepare() {
    vertx = Vertx.vertx();
  }

  @After
  public void finish(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void startHttpServer(TestContext context) {
    Async async = context.async();
    vertx
      .createHttpServer()
      .requestHandler(req ->
        req.response().putHeader("Content-Type", "text/html").end("OK"))
      .listen(8080, context.asyncAssertSuccess(server -> {
        WebClient webClient = WebClient.create(vertx);
        webClient.get(8080, "localhost", "/").send(ar -> {
          if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            context.assertTrue(response.headers().contains("Content-Type"));
            context.assertEquals("text/html", response.getHeader("Content-Type"));
            context.assertEquals("OK", response.body().toString());
            async.complete();
          } else {
            async.resolve(Promise.failedPromise(ar.cause()));
          }
        });
      }));
  }
}
