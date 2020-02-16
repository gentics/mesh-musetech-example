package com.gentics.mesh.alexa.action;

import static com.gentics.mesh.alexa.util.I18NUtil.i18n;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;

import com.gentics.mesh.alexa.dagger.config.SkillConfig;
import com.gentics.mesh.alexa.intent.impl.TourInfoIntentHandler;
import com.gentics.mesh.alexa.model.AlexaResponse;
import com.gentics.mesh.alexa.model.TourInfo;
import com.gentics.mesh.alexa.util.DateUtils;
import com.gentics.mesh.core.rest.graphql.GraphQLRequest;
import com.gentics.mesh.core.rest.node.NodeListResponse;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.MicronodeField;
import com.gentics.mesh.core.rest.node.field.impl.DateFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.core.rest.node.field.list.MicronodeFieldList;
import com.gentics.mesh.parameter.NodeParameters;
import com.gentics.mesh.parameter.client.NodeParametersImpl;
import com.gentics.mesh.rest.client.MeshRestClient;
import com.gentics.mesh.rest.client.MeshRestClientConfig;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Singleton
public class MeshActions {

	private static final Logger log = LoggerFactory.getLogger(TourInfoIntentHandler.class);

	private final String PROJECT = "musetech";

	private final MeshRestClient client;

	private final JsonObject searchTourQuery;

	private final String loadAllToursInfoQuery;

	private final String loadTourByUuidQuery;

	@Inject
	public MeshActions(SkillConfig config) {

		MeshRestClientConfig clientConfig = MeshRestClientConfig.newConfig()
			.setHost(config.getMeshServerHost())
			.setPort(config.getMeshServerPort())
			.setSsl(config.isMeshServerSslFlag())
			.setBasePath("/api/v2")
			.build();

		client = MeshRestClient.create(clientConfig);
		String apiKey = config.getMeshApiKey();
		if (apiKey != null) {
			client.setAPIKey(apiKey);
		} else {
			client.setLogin("admin", "admin");
			client.login().blockingGet();
		}
		try {
			searchTourQuery = loadJson("/queries/searchTour.json");
			loadAllToursInfoQuery = loadString("/queries/loadAllToursInfo.gql");
			loadTourByUuidQuery = loadString("/queries/loadTourByUuid.gql");
		} catch (Exception e) {
			throw new RuntimeException("Could not find query.");
		}
	}

	private String loadString(String path) throws IOException {
		return IOUtils.toString(this.getClass().getResourceAsStream(path), "UTF-8");
	}

	private JsonObject loadJson(String path) throws IOException {
		return new JsonObject(loadString(path));
	}

	public Single<AlexaResponse> loadTourInfos(Locale locale) {

		return client.graphqlQuery(PROJECT, loadAllToursInfoQuery).toMaybe().map(response -> {
			System.out.println(response.toJson());
			JsonObject json = response.getData();
			JsonArray tours = json.getJsonObject("schema").getJsonObject("nodes").getJsonArray("elements");

			if (tours.size() == 0) {
				return AlexaResponse.create(locale, "tours_empty");
			}

			StringBuilder builder = new StringBuilder();
			builder.append(i18n(locale, "tour_info_intro"));
			builder.append(" ");
			for (int i = 0; i < tours.size(); i++) {
				JsonObject tour = tours.getJsonObject(i);
				JsonObject tourFields = tour.getJsonObject("fields");
				String title = tourFields.getString("title");
				int size = tourFields.getInteger("size");
				double price = tourFields.getDouble("price");
				JsonArray dates = tourFields.getJsonArray("dates");
				builder.append(i18n(locale, "tour_info", title, String.valueOf(size)));
				if (tours.size() >= 2 && i == tours.size() - 2) {
					builder.append(" " + i18n(locale, "and") + " ");
				} else {
					builder.append(". ");
				}
			}
			return AlexaResponse.create(builder.toString());
		})
			.onErrorReturnItem(AlexaResponse.create(locale, "tours_empty"))
			.toSingle();
	}

	public Maybe<TourInfo> loadTourByUuid(Locale locale, String tourUuid, String tourDateStr) {
		JsonObject vars = new JsonObject();
		vars.put("lang", locale.getLanguage());
		vars.put("uuid", tourUuid);

		GraphQLRequest request = new GraphQLRequest();
		request.setQuery(loadTourByUuidQuery);
		request.setVariables(vars);

		return client.graphql(PROJECT, request).toMaybe().flatMap(response -> {
			// System.out.println(response.toJson());
			JsonObject json = response.getData();
			JsonObject tour = json.getJsonObject("node");
			if (tour == null) {
				return Maybe.empty();
			}

			JsonObject tourFields = tour.getJsonObject("fields");
			String uuid = tour.getString("uuid");
			String title = tourFields.getString("title");
			String location = tourFields.getString("location");
			int size = tourFields.getInteger("size");
			double price = tourFields.getDouble("price");
			JsonArray dates = tourFields.getJsonArray("dates");
			if (dates.isEmpty()) {
				return Maybe.empty();
			}
			for (int e = 0; e < dates.size(); e++) {
				JsonObject tourDate = dates.getJsonObject(e);
				JsonObject tourDateFields = tourDate.getJsonObject("fields");
				int seats = tourDateFields.getInteger("seats");
				String dateStr = tourDateFields.getString("date");
				OffsetDateTime date = null;
				try {
					date = DateUtils.parse(dateStr);
				} catch (Exception e2) {
					log.error("Could not parse date {" + dateStr + "}");
				}

				if (dateStr.equals(tourDateStr)) {
					System.out.println("Comparing: " + dateStr + " with " + tourDateStr);
					return Maybe.just(new TourInfo(uuid, title, location, date, price, seats, size, tourDateStr));
				}
			}

			return Maybe.empty();

		});
	}

	public Maybe<TourInfo> loadNextTour(Locale locale) {
		JsonObject vars = new JsonObject();
		vars.put("lang", locale.getLanguage());

		GraphQLRequest request = new GraphQLRequest();
		request.setQuery(loadAllToursInfoQuery);
		request.setVariables(vars);

		return client.graphql(PROJECT, request).toMaybe().map(response -> {
			// System.out.println(response.toJson());
			JsonObject json = response.getData();
			JsonArray tours = json.getJsonObject("schema").getJsonObject("nodes").getJsonArray("elements");
			return findNextTour(tours);
		});
	}

	public Single<AlexaResponse> loadNextTourInfo(Locale locale) {

		return loadNextTour(locale).map(tour -> {
			if (tour == null) {
				return AlexaResponse.create(locale, "tours_empty");
			} else {
				OffsetDateTime dateTime = tour.getDate();
				LocalDate date = dateTime.toLocalDate();
				LocalDate today = DateUtils.now().toLocalDate();
				boolean isToday = date.isEqual(today);
				boolean isTomorrow = date.isEqual(today.plusDays(1));
				StringBuilder builder = new StringBuilder();
				String timeStr = DateUtils.toTime(dateTime);
				String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				if (isToday) {
					builder.append(i18n(locale, "tour_next_info_today", tour.getTitle(), timeStr, tour.getLocation()));
					builder.append(" ");
					builder.append(seatsInfo(locale, tour.getSeats()));
				} else if (isTomorrow) {
					builder.append(i18n(locale, "tour_next_info_tomorrow", tour.getTitle(), timeStr, tour.getLocation()));
					builder.append(" ");
					builder.append(seatsInfo(locale, tour.getSeats()));
				} else {
					// builder.append(i18n(locale, "tour_next_info_tomorrow"));
					builder.append(i18n(locale, "tour_next_info_on", tour.getTitle(), dateStr, timeStr, tour.getLocation()));
				}
				builder.append(" ");
				AlexaResponse response = new AlexaResponse(builder.toString());
				response.addAttribute(Attributes.TOUR_UUID, tour.getUuid());
				response.addAttribute(Attributes.TOUR_DATE, tour.getDateStr());
				return response;

			}

		})
			.onErrorReturnItem(AlexaResponse.create(locale, "tours_empty"))
			.toSingle();
	}

	private String seatsInfo(Locale locale, int seats) {
		if (seats == 1) {
			return i18n(locale, "tour_next_info_seat");
		} else {
			return i18n(locale, "tour_next_info_seats", String.valueOf(seats));
		}
	}

	private TourInfo findNextTour(JsonArray tours) {
		TourInfo earliestInfo = null;
		for (int i = 0; i < tours.size(); i++) {
			JsonObject tour = tours.getJsonObject(i);
			JsonObject tourFields = tour.getJsonObject("fields");
			String uuid = tour.getString("uuid");
			String title = tourFields.getString("title");
			String location = tourFields.getString("location");
			int size = tourFields.getInteger("size");
			double price = tourFields.getDouble("price");
			JsonArray dates = tourFields.getJsonArray("dates");
			if (dates.isEmpty()) {
				break;
			}
			for (int e = 0; e < dates.size(); e++) {
				JsonObject tourDate = dates.getJsonObject(e);
				JsonObject tourDateFields = tourDate.getJsonObject("fields");
				int seats = tourDateFields.getInteger("seats");
				String dateStr = tourDateFields.getString("date");
				OffsetDateTime date = null;
				try {
					System.out.println("Checking tour {" + dateStr + "} " + title);
					date = DateUtils.parse(dateStr);
					boolean isInPast = date.isBefore(DateUtils.now());
					// Skip full and past tours
					if (isInPast || seats == 0) {
						System.out.println("Tour date was in past");
						continue;
					}
					// Check whether this is the first tour in the future or whether the tour is earlier compared to the last found tour.
					if (earliestInfo == null || date.isBefore(earliestInfo.getDate())) {
						earliestInfo = new TourInfo(uuid, title, location, date, price, seats, size, dateStr);
					}
				} catch (Exception e2) {
					log.error("Could not parse date {" + dateStr + "}", e2);
				}
			}
		}
		return earliestInfo;

	}

	public Single<AlexaResponse> loadStockLevel(Locale locale, String tourName) {
		return locateTour(tourName).map(node -> {
			Long level = getStockLevel(node);
			if (level == null || level == 0) {
				return AlexaResponse.create(locale, "tour_out_of_stock", getName(node));
			} else if (level == 1) {
				return AlexaResponse.create(locale, "tour_stock_level_one", getName(node));
			} else {
				return AlexaResponse.create(locale, "tour_stock_level", String.valueOf(level));
			}
		})
			.onErrorReturnItem(AlexaResponse.create(locale, "tour_stock_level_error"))
			.defaultIfEmpty(AlexaResponse.create(locale, "tour_not_found"))
			.toSingle();
	}

	public Single<AlexaResponse> reserveTourByUuid(Locale locale, String uuid, String dateStr) {
		return loadTourByUuid(locale, uuid, dateStr).flatMapSingle(tour -> {

			if (tour.getSeats() == 0) {
				return Single.just(AlexaResponse.create(locale, "tour_out_of_stock", tour.getTitle()));
			}

			// Load and update the tour node in both languages
			return Observable.fromArray("de", "en").flatMapCompletable(lang -> {
				NodeParameters langParams = new NodeParametersImpl().setLanguages(lang);
				Maybe<NodeResponse> locatedNode = client
					.findNodeByUuid(PROJECT, tour.getUuid(), langParams).toMaybe();

				return locatedNode.flatMapCompletable(node -> {
					NodeUpdateRequest nodeUpdateRequest = node.toRequest();
					updateRequest(nodeUpdateRequest, tour);
					System.out.println(nodeUpdateRequest.toJson());
					// Update and publish the node
					Completable update = client.updateNode(PROJECT, node.getUuid(), nodeUpdateRequest).toCompletable()
						.andThen(client.publishNode(PROJECT, node.getUuid(), langParams).toCompletable());
					return update;
				});
			}).andThen(Single.just(AlexaResponse.create(locale, "tour_reserved", tour.getTitle())));
		}).onErrorReturnItem(AlexaResponse.create(locale, "tour_reserve_error"));
	}

	private void updateRequest(NodeUpdateRequest nodeUpdateRequest, TourInfo tour) {
		MicronodeFieldList list = nodeUpdateRequest.getFields().getMicronodeFieldList("dates");

		for (MicronodeField date : list.getItems()) {
			DateFieldImpl currentDateField = date.getFields().getDateField("date");
			if (currentDateField != null) {
				OffsetDateTime odt = DateUtils.parse(currentDateField.getDate());

				// Found the next tour. Lets update the count
				if (tour.getDate().isEqual(odt)) {
					int currentSeats = date.getFields().getNumberField("seats").getNumber().intValue();
					int newSeats = currentSeats - 1;
					date.getFields().put("seats", new NumberFieldImpl().setNumber(newSeats));
				}
			}
		}

		nodeUpdateRequest.getFields().put("dates", list);
	}

	public Single<AlexaResponse> loadTourPriceByUuid(Locale locale, String tourUuid) {
		return locateTourByUuid(locale, tourUuid).map(node -> {
			NumberFieldImpl price = node.getFields().getNumberField("price");
			double value = price.getNumber().doubleValue();
			String priceStr = String.format("%.2f Euro", value);
			String name = getName(node);
			log.info("Located tour for " + tourUuid + " => " + name);
			return AlexaResponse.create(locale, "tour_price", name, priceStr);
		})
			.defaultIfEmpty(AlexaResponse.create(locale, "tour_not_found"))
			.onErrorReturnItem(AlexaResponse.create(locale, "tour_not_found"))
			.toSingle();
	}

	public Single<AlexaResponse> loadTourPrice(Locale locale, String tourName) {
		return locateTour(tourName).map(node -> {
			NumberFieldImpl price = node.getFields().getNumberField("price");
			double value = price.getNumber().doubleValue();
			String priceStr = String.format("%.2f Euro", value);
			String name = getName(node);
			log.info("Located tour for " + tourName + " => " + name);
			return AlexaResponse.create(locale, "tour_price", name, priceStr);
		})
			.defaultIfEmpty(AlexaResponse.create(locale, "tour_not_found"))
			.onErrorReturnItem(AlexaResponse.create(locale, "tour_price_not_found", tourName))
			.toSingle();
	}

	private Maybe<NodeResponse> locateTourByUuid(Locale locale, String tourUuid) {
		return client.findNodeByUuid(PROJECT, tourUuid, new NodeParametersImpl().setLanguages(locale.getLanguage())).toMaybe();
	}

	private Maybe<NodeResponse> locateTour(String tourName) {
		if (tourName == null) {
			return Maybe.empty();
		}
		JsonObject query = new JsonObject(searchTourQuery.encode());
		query.getJsonObject("query").getJsonObject("bool").getJsonArray("must").getJsonObject(1).getJsonObject("match").put("fields.title",
			tourName.toLowerCase());
		log.info("Sending search request:\n\n" + query.encodePrettily());
		return client.searchNodes(PROJECT, query.encode()).toMaybe()
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
		MicronodeFieldList dates = response.getFields().getMicronodeFieldList("dates");
		MicronodeField first = dates.getItems().get(0);
		Number number = first.getFields().getNumberField("seats").getNumber();
		if (number == null) {
			return null;
		}
		return number.longValue();
	}

	private String getName(NodeResponse node) {
		return node.getFields().getStringField("title").getString();
	}

}
