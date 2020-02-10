package com.gentics.mesh.alexa;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.gentics.mesh.alexa.dagger.AppComponent;
import com.gentics.mesh.alexa.dagger.DaggerAppComponent;
import com.gentics.mesh.alexa.dagger.config.SkillConfig;
import com.gentics.mesh.alexa.server.SkillServerVerticle;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Singleton
public class GenticsSkill {

	public static final String GENTICS_PHONETIC = "<phoneme alphabet=\"ipa\" ph=\"dʒɛˈntɪcs\"></phoneme>";

	private static Logger log;

	public static void main(String[] args) {
		initLogger();

		log = LoggerFactory.getLogger(GenticsSkill.class);
		log.info("Logging system initialized");

		SkillConfig config = new SkillConfig();
		applyEnv(config);
		AppComponent app = DaggerAppComponent.builder().config(config).build();
		app.skill().run();
	}

	private static void initLogger() {
		Configurator.setRootLevel(Level.DEBUG);
		System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, Log4j2LogDelegateFactory.class.getName());
		LoggerFactory.initialise();
	}

	/**
	 * Override configuration with environment variables.
	 * 
	 * @param config
	 */
	private static void applyEnv(SkillConfig config) {
		String host = System.getenv("MESH_HOST");
		if (host != null) {
			config.setMeshHost(host);
		}

		String port = System.getenv("MESH_PORT");
		if (port != null) {
			config.setMeshPort(Integer.parseInt(port));
		}

		String ssl = System.getenv("MESH_SSL");
		if (ssl != null) {
			config.setMeshSsl(Boolean.parseBoolean(ssl));
		}

		String key = System.getenv("MESH_APIKEY");
		if (key != null) {
			config.setMeshApiKey(key);
		}
	}

	private Vertx vertx;
	private SkillServerVerticle serverVerticle;

	@Inject
	public GenticsSkill(Vertx vertx, SkillServerVerticle serverVerticle) {
		this.vertx = vertx;
		this.serverVerticle = serverVerticle;
	}

	public void run() {
		log.info("Deploying Muse Tech Skill");
		vertx.deployVerticle(serverVerticle);
	}

}
