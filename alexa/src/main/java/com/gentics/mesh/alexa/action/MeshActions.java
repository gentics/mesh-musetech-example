package com.gentics.mesh.alexa.action;

import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;

import com.gentics.mesh.alexa.dagger.config.SkillConfig;
import com.gentics.mesh.alexa.intent.impl.StockLevelIntentHandler;
import com.gentics.mesh.core.rest.node.NodeListResponse;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Singleton
public class MeshActions {

	private static final Logger log = LoggerFactory.getLogger(StockLevelIntentHandler.class);

	private final String PROJECT = "demo";

	private final MeshRestClient client;

	private final JsonObject searchVehicleQuery;

	@Inject
	public MeshActions(SkillConfig config) {
		client = MeshRestClient.create(config.getMeshServerHost(), config.getMeshServerPort(), config.isMeshServerSslFlag());
		String apiKey = config.getMeshApiKey();
		if (apiKey != null) {
			client.setAPIKey(apiKey);
		} else {
			client.setLogin("admin", "admin");
			client.login().blockingGet();
		}
		try {
			searchVehicleQuery = new JsonObject(IOUtils.toString(this.getClass().getResourceAsStream("/queries/searchVehicle.json"),
				"UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("Could not find query.");
		}
	}

	public Single<String> loadStockLevel(Locale locale, String vehicleName) {
		return locateVehicle(vehicleName).map(node -> {
			Long level = getStockLevel(node);
			if (level == null || level == 0) {
				return i18n(locale, "vehicle_out_of_stock", getName(node));
			} else if (level == 1) {
				return i18n(locale, "vehicle_stock_level_one", getName(node));
			} else {
				return i18n(locale, "vehicle_stock_level", String.valueOf(level));
			}
		})
			.onErrorReturnItem(i18n(locale, "vehicle_stock_level_error"))
			.defaultIfEmpty(i18n(locale, "vehicle_not_found"))
			.toSingle();
	}

	public Single<String> reserveVehicle(Locale locale, String vehicleName) {
		return locateVehicle(vehicleName).flatMap(node -> {
			Long level = getStockLevel(node);
			String name = node.getFields().getStringField("name").getString();
			if (level == null || level <= 0) {
				return Maybe.just(i18n(locale, "vehicle_out_of_stock", name));
			}
			long newLevel = level - 1;
			NodeUpdateRequest request = node.toRequest();
			request.getFields().put("stocklevel", new NumberFieldImpl().setNumber(newLevel));
			return client.updateNode(PROJECT, node.getUuid(), request).toSingle().map(n -> {
				return i18n(locale, "vehicle_reserved", name);
			}).toMaybe();

		})
			.defaultIfEmpty(i18n(locale, "vehicle_not_found"))
			.onErrorReturnItem(i18n(locale, "vehicle_reserve_error"))
			.toSingle();
	}

	public Single<String> loadVehiclePrice(Locale locale, String vehicleName) {
		return locateVehicle(vehicleName).map(node -> {
			NumberFieldImpl price = node.getFields().getNumberField("price");
			double value = price.getNumber().doubleValue();
			String priceStr = String.format("%.2f Euro", value);
			String name = getName(node);
			log.info("Located vehicle for " + vehicleName + " => " + name);
			return i18n(locale, "vehicle_price", name, priceStr);
		})
			.defaultIfEmpty(i18n(locale, "vehicle_not_found"))
			.onErrorReturnItem(i18n(locale, "vehicle_price_not_found", vehicleName))
			.toSingle();
	}

	private Maybe<NodeResponse> locateVehicle(String vehicleName) {
		if (vehicleName == null) {
			return Maybe.empty();
		}
		JsonObject query = new JsonObject(searchVehicleQuery.encode());
		query.getJsonObject("query").getJsonObject("bool").getJsonArray("must").getJsonObject(1).getJsonObject("match").put("fields.name",
			vehicleName.toLowerCase());
		log.info("Sending search request:\n\n" + query.encodePrettily());
		return client.searchNodes("demo", query.encode()).toMaybe()
			.onErrorComplete()
			.defaultIfEmpty(new NodeListResponse())
			.flatMap(list -> {
				if (list.getData().isEmpty()) {
					log.info("No result found");
					return Maybe.empty();
				} else {
					log.info("Found {" + list.getData().size() + "} matches. Using the first.");
					return Maybe.just(list.getData().get(0));
				}
			});
	}

	private Long getStockLevel(NodeResponse response) {
		Number number = response.getFields().getNumberField("stocklevel").getNumber();
		if (number == null) {
			return null;
		}
		return number.longValue();
	}

	private String getName(NodeResponse node) {
		return node.getFields().getStringField("name").getString();
	}
}
