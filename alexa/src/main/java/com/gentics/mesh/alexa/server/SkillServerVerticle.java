package com.gentics.mesh.alexa.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gentics.mesh.alexa.GenticsSkill;
import com.gentics.mesh.alexa.dagger.config.SkillConfig;
import com.gentics.mesh.alexa.intent.SkillIntentHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;

@Singleton
public class SkillServerVerticle extends AbstractVerticle {

	public static final Logger log = LoggerFactory.getLogger(GenticsSkill.class);

	private HttpServer server;
	private SkillIntentHandler intentHandler;

	private SkillConfig config;

	@Inject
	public SkillServerVerticle(SkillConfig config, SkillIntentHandler intentHandler) {
		this.config = config;
		this.intentHandler = intentHandler;
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Router router = Router.router(vertx);

		addRoutes(router);

		int port = config.getServerPort();
		server = vertx.createHttpServer()
			.requestHandler(router::handle)
			.listen(port, lh -> {
				if (lh.failed()) {
					startFuture.fail(lh.cause());
				} else {
					log.info("Server started on port {" + port + "}");
					startFuture.complete();
				}
			});
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		server.close(c -> {
			if (c.failed()) {
				stopFuture.fail(c.cause());
			} else {
				stopFuture.complete();
			}
		});
	}

	private void addRoutes(Router router) {
		router.route().handler(LoggerHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().failureHandler(f -> {
			f.failure().printStackTrace();
			f.next();
		});

		router.route("/alexa").handler(rh -> {
			JsonObject json = rh.getBodyAsJson();
			try {
				intentHandler.handleRequest(json, sr -> {
					Buffer buffer = Buffer.buffer(sr.getRawResponse());
					rh.response().end(buffer);
				});
			} catch (IOException e) {
				rh.fail(e);
			}
		});

	}
}
