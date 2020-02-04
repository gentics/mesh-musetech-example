package com.gentics.mesh.alexa.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpClient;

@Module
public class AppModule {

	@Provides
	@Singleton
	public Vertx vertx() {
		VertxOptions options = new VertxOptions();
		options.setBlockedThreadCheckInterval(1000 * 60 * 60);
		return Vertx.vertx(options);
	}

	@Provides
	@Singleton
	public HttpClient client(Vertx vertx) {
		return vertx.createHttpClient();
	}

	@Provides
	@Singleton
	public FileSystem filesystem(Vertx vertx) {
		return vertx.fileSystem();
	}

}