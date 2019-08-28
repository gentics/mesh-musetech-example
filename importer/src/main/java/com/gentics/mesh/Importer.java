package com.gentics.mesh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gentics.mesh.core.rest.micronode.MicronodeResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaResponse;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.impl.NodeFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.node.field.list.NodeFieldList;
import com.gentics.mesh.core.rest.node.field.list.impl.MicronodeFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NodeFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NodeFieldListItemImpl;
import com.gentics.mesh.core.rest.project.ProjectCreateRequest;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.role.RolePermissionRequest;
import com.gentics.mesh.core.rest.schema.impl.BinaryFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.MicroschemaReferenceImpl;
import com.gentics.mesh.core.rest.schema.impl.NodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NumberFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.core.rest.schema.impl.StringFieldSchemaImpl;
import com.gentics.mesh.parameter.client.NodeParametersImpl;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Importer {

	private static final Logger log = LoggerFactory.getLogger(Importer.class);

	private static final String PROJECT_NAME = "demo";

	private static final String SCREEN_EVENT_MICROSCHEMA_NAME = "ScreenEvent";

	private static final String SCREEN_PROMO_MICROSCHEMA_NAME = "ScreenExhibitionPromo";

	private Vertx vertx = Vertx.vertx();
	private MeshRestClient client = MeshRestClient.create("localhost", 8080, false, vertx);

	private Map<String, String> fileIdMap = new HashMap<>();

	public Importer() {
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new Importer().run();
	}

	private void run() throws FileNotFoundException, IOException {
		client.setLogin("admin", "admin");
		client.login().blockingGet();

		long start = System.currentTimeMillis();
		loadProject()
			.flatMapCompletable(project -> {
				return createMicroschemas().andThen(createSchemas())
					.andThen(
						createFolders(project))
					.andThen(
						grantPermissions(project));
			})
			.subscribe(() -> {
				long dur = System.currentTimeMillis() - start;
				System.out.println("Import done. Took: " + dur + "[ms]");
			}, err -> {
				err.printStackTrace();
			});
	}

	private Completable grantPermissions(ProjectResponse project) {
		return client.findRoles().toSingle().flatMapCompletable(list -> {
			String roleUuid = list.getData().stream().filter(u -> u.getName().equals("anonymous")).map(u -> u.getUuid()).findFirst().get();
			RolePermissionRequest request = new RolePermissionRequest();
			request.setRecursive(true);
			request.getPermissions().setRead(true);
			request.getPermissions().setReadPublished(true);
			return client.updateRolePermissions(roleUuid, "projects/" + project.getUuid(), request).toCompletable()
				.andThen(client.updateRolePermissions(roleUuid, "schemas", request).toCompletable());
		});
	}

	private Single<ProjectResponse> loadProject() {
		return client.findProjectByName(PROJECT_NAME).toSingle().onErrorResumeNext(err -> {
			ProjectCreateRequest request = new ProjectCreateRequest();
			request.setName(PROJECT_NAME);
			request.setSchemaRef("folder");
			return client.createProject(request).toSingle();
		});
	}

	private Completable importScreens(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		operations.add(createScreen1(folder));
		operations.add(createScreen2(folder));
		return Completable.merge(operations);
	}

	private Completable importContents(NodeResponse folder) throws FileNotFoundException, IOException {
		Set<Completable> operations = new HashSet<>();
		ContentTransformer t = new ContentTransformer();
		List<Tuple<Exhibition, Exhibition>> tu = t.transform();
		for (Tuple<Exhibition, Exhibition> tup : tu) {
			Exhibition de = tup.getA();
			Exhibition en = tup.getB();
			operations.add(createExhibition(folder.getUuid(), de, en).flatMapCompletable(tupl -> {
				return translateExhibition(tupl.getA(), tupl.getB(), en);
			}));
		}
		return Completable.merge(operations);
	}

	private Completable translateExhibition(NodeResponse node, String audioFolderUuid, Exhibition ex) {
		return Single.just(new NodeUpdateRequest()).flatMapCompletable(request -> {
			request.setLanguage("en");

			List<Completable> contents = new ArrayList<>();
			String audioUrl = ex.getAudio();
			if (audioUrl != null) {
				String filename = Paths.get(audioUrl).getFileName().toString();
				contents.add(
					createAudio(audioFolderUuid, "en", filename).doOnSuccess(audioResp -> {
						log.info("Add audio reference {" + audioResp.getUuid() + "}");
						request.getFields().put("audio", new NodeFieldImpl().setUuid(audioResp.getUuid()));
					}).toCompletable());
			}

			String description = ex.getDescription();
			String title = ex.getTitle();
			String id = ex.getId();
			String publicNumber = ex.getPublicNumber();
			request.getFields().put("title", new StringFieldImpl().setString(title));
			request.getFields().put("description", new StringFieldImpl().setString(description));
			request.getFields().put("id", new StringFieldImpl().setString(id));
			request.getFields().put("public_number", new StringFieldImpl().setString(publicNumber));
			request.getFields().put("slug", new StringFieldImpl().setString(publicNumber + ":en"));

			// Copy from first language
			request.getFields().put("images", node.getFields().getNodeFieldList("images"));
			request.getFields().put("title_image", node.getFields().getNodeField("title_image"));

			return Completable.merge(contents).andThen(client.updateNode(PROJECT_NAME, node.getUuid(), request)
				.toCompletable()
				.doOnComplete(() -> {
					log.info("Updated exhibition {" + node.getUuid() + "/en}");
				}).doOnError(err -> {
					log.info("Error while translating exhibition {" + node.getUuid() + "/en}");
				}));
		});
	}

	private Single<Tuple<NodeResponse, String>> createExhibition(String folderUuid, Exhibition exDe, Exhibition exEn) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("de");
		request.setSchemaName("Exhibition");
		request.setParentNodeUuid(folderUuid);

		String description = exDe.getDescription();
		String title = exDe.getTitle();
		String id = exDe.getId();
		String publicNumber = exDe.getPublicNumber();

		request.getFields().put("title", new StringFieldImpl().setString(title));
		request.getFields().put("description", new StringFieldImpl().setString(description));
		request.getFields().put("id", new StringFieldImpl().setString(id));
		request.getFields().put("public_number", new StringFieldImpl().setString(publicNumber));
		request.getFields().put("slug", new StringFieldImpl().setString(publicNumber + ":de"));

		return client.createNode(PROJECT_NAME, request, new NodeParametersImpl().setLanguages("de"))
			.toSingle()
			.doOnSuccess(node -> {
				log.info("Created exhibition {" + id + "} with uuid" + node.getUuid());
			})
			.doOnError(err -> {
				log.error("Error while creating exhibition {" + id + "}", err);
			})
			.flatMap(node -> {

				Single<NodeResponse> imagesFolder = createFolder(node.getUuid(), "images", "Images");
				Single<NodeResponse> audiosFolder = createFolder(node.getUuid(), "audios", "Audios");

				Single<Tuple<NodeResponse, String>> tupS = Single.zip(imagesFolder, audiosFolder, (imgFolder, auFolder) -> {
					Single<Tuple<NodeResponse, String>> td = storeAndUpdateRelatedContents(node.getUuid(), node.getLanguage(), node.getVersion(),
						exDe, imgFolder, auFolder)
							.flatMap(nodeR -> {
								return Single.just(Tuple.tuple(nodeR, auFolder.getUuid()));
							});
					return td;
				}).flatMap(r -> r);
				return tupS;
			});

	}

	/**
	 * 
	 * @param uuid
	 *            Uuid of the exhibition node
	 * @param lang
	 *            Language of the node
	 * @param version
	 *            Version of the exhibition node
	 * @param ex
	 *            Current exhibition
	 * @param request
	 * @param imgFolder
	 * @param auFolder
	 * @return
	 */
	private Single<NodeResponse> storeAndUpdateRelatedContents(String uuid, String lang, String version, Exhibition ex, NodeResponse imgFolder,
		NodeResponse auFolder) {
		return Single.just(new NodeUpdateRequest()).flatMap(request -> {

			List<Completable> contents = new ArrayList<>();

			String audio = ex.getAudio();
			if (audio != null) {
				String filename = Paths.get(audio).getFileName().toString();
				contents.add(
					createAudio(auFolder.getUuid(), "de", filename).doOnSuccess(audioResp -> {
						log.info("Add audio reference {" + uuid + "}");
						request.getFields().put("audio", new NodeFieldImpl().setUuid(audioResp.getUuid()));
					}).toCompletable());
			}

			for (String image : ex.getImages()) {
				contents.add(
					createImage(imgFolder.getUuid(), image)
						.doOnSuccess(imageResp -> {
							log.info("Add images reference {" + uuid + "}");
							NodeFieldListItemImpl item = new NodeFieldListItemImpl().setUuid(imageResp.getUuid());
							NodeFieldList list = request.getFields().getNodeFieldList("images");
							if (list == null) {
								list = new NodeFieldListImpl();
								request.getFields().put("images", list);
							}
							list.add(item);

							String detailImage = ex.getTitleImage();
							if (detailImage != null) {
								if (detailImage.equals(image)) {
									request.getFields().put("title_image", new NodeFieldImpl().setUuid(imageResp.getUuid()));
								}
							}

						}).toCompletable());
			}

			return Completable.merge(contents).andThen(Single.just(request));
		}).flatMap(request -> {
			return updateExhibition(uuid, lang, version, request);
		});
	}

	private Single<NodeResponse> updateExhibition(String uuid, String lang, String version, NodeUpdateRequest request) {
		log.info("Update exhibition {" + uuid + "}");
		request.setLanguage(lang);
		request.setVersion(version);
		return client.updateNode(PROJECT_NAME, uuid, request, new NodeParametersImpl().setLanguages(lang)).toSingle()
			.doOnError(err -> {
				log.error("Error while updating exhibition {" + uuid + "}", err);
			})
			.doOnSuccess(node -> {
				log.info("Exhibition {" + uuid + "} updated.");
			});
	}

	private Single<NodeResponse> createImage(String uuid, String url) {
		String curl = url.replace("public/", "/");
		String filename = Paths.get(url).getFileName().toString();
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setParentNodeUuid(uuid);
		request.setSchemaName("Image");
		request.getFields().put("filename", new StringFieldImpl().setString(filename));
		return client.createNode(PROJECT_NAME, request).toSingle().flatMap(node -> {
			fileIdMap.put(filename, node.getUuid());
			Buffer buffer = vertx.fileSystem().readFileBlocking("data/images/" + filename);
			return client.updateNodeBinaryField(PROJECT_NAME, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", buffer, filename,
				"image/jpeg").toSingle();
		}).doOnError(err -> {
			log.error("Error while creating image {" + curl + "} for node {" + uuid + "}");
		}).doOnSuccess(node -> {
			log.info("Created image {" + curl + "} with id {" + node.getUuid() + "}");
		});
	}

	private Single<NodeResponse> createVideo(String uuid, String filename) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setParentNodeUuid(uuid);
		request.setSchemaName("Video");
		request.getFields().put("filename", new StringFieldImpl().setString(filename));
		return client.createNode(PROJECT_NAME, request).toSingle().flatMap(node -> {
			fileIdMap.put(filename, node.getUuid());
			Buffer buffer = vertx.fileSystem().readFileBlocking("data/videos/" + filename);
			String mimeType = MimeMapping.getMimeTypeForFilename(filename);
			return client.updateNodeBinaryField(PROJECT_NAME, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", buffer, filename,
				mimeType).toSingle();
		}).doOnError(err -> {
			log.error("Error while creating video {" + filename + "} for node {" + uuid + "}");
		}).doOnSuccess(node -> {
			log.info("Created video {" + filename + "} with id {" + node.getUuid() + "}");
		});
	}

	private Single<NodeResponse> createAudio(String uuid, String lang, String filename) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage(lang);
		request.setParentNodeUuid(uuid);
		request.setSchemaName("Audio");
		request.getFields().put("filename", new StringFieldImpl().setString(filename));
		String mimeType = MimeMapping.getMimeTypeForFilename(filename);
		return client.createNode(PROJECT_NAME, request, new NodeParametersImpl().setLanguages(lang)).toSingle().flatMap(node -> {
			Buffer buffer = vertx.fileSystem().readFileBlocking("data/audios/" + filename);
			return client.updateNodeBinaryField(PROJECT_NAME, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", buffer, filename,
				mimeType).toSingle();
		}).doOnError(err -> {
			log.error("Error while creating audio {" + filename + "} for node {" + uuid + "}");
		}).doOnSuccess(node -> {
			log.info("Created audio {" + filename + "} with id {" + node.getUuid() + "}");
		});

	}

	private Completable createFolders(ProjectResponse project) {
		String uuid = project.getRootNode().getUuid();
		Set<Completable> operations = new HashSet<>();
		operations.add(createFolder(uuid, "images", "Bilder").flatMapCompletable(this::importImages));
		operations.add(createFolder(uuid, "videos", "Videos").flatMapCompletable(this::importVideos));
		operations.add(createFolder(uuid, "exhibitions", "Exhibitions").flatMapCompletable(this::importContents)
			.andThen(createFolder(uuid, "screens", "Screens").flatMapCompletable(this::importScreens)));
		return Completable.merge(operations);
	}

	private Completable importImages(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		File imageFolder = new File("data/images");
		for (File file : imageFolder.listFiles()) {
			String filename = file.getName();
			if (filename.startsWith("ausstellung_") || filename.startsWith("vrlab")) {
				operations.add(createImage(folder.getUuid(), filename).toCompletable());
			}
		}
		return Completable.merge(operations);
	}

	private Completable importVideos(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		File imageFolder = new File("data/videos");
		for (File file : imageFolder.listFiles()) {
			String filename = file.getName();
			operations.add(createVideo(folder.getUuid(), filename).toCompletable());
		}
		return Completable.merge(operations);
	}

	private Single<NodeResponse> createFolder(String uuid, String slug, String name) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setParentNodeUuid(uuid);
		request.setLanguage("en");
		request.setSchemaName("folder");
		request.getFields().put("name", new StringFieldImpl().setString(name));
		request.getFields().put("slug", new StringFieldImpl().setString(slug));
		return client.createNode(PROJECT_NAME, request).toSingle().doOnError(err -> {
			log.error("Error while creating folder {" + name + "}", err);
		});
	}

	private Completable createSchemas() {
		Set<Completable> operations = new HashSet<>();
		operations.add(createExhibitionSchema());
		operations.add(createAudioSchema());
		operations.add(createScreenSchema());
		operations.add(createVideoSchema());
		operations.add(createImageSchema());
		return Completable.merge(operations);
	}

	private Completable createMicroschemas() {
		Set<Completable> operations = new HashSet<>();
		operations.add(createScreenEventMicroschema());
		operations.add(createScreenExhibitionPromoMicroschema());
		return Completable.merge(operations);
	}

	private Completable createScreen1(NodeResponse folder) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("de");
		request.setParentNodeUuid(folder.getUuid());
		request.setSchemaName("Screen");
		request.getFields().put("id", new StringFieldImpl().setString("screen1"));
		request.getFields().put("name", new StringFieldImpl().setString("Haupteingang"));
		request.getFields().put("description", new StringFieldImpl().setString("Screen für Events"));
		request.getFields().put("location", new StringFieldImpl().setString("Haupteingang"));

		MicronodeFieldListImpl contents = new MicronodeFieldListImpl();
		contents.getItems()
			.add(createScreenPromo("ABC", "ABC: ", "ausstellung_test.jpg", null));

		contents.getItems()
			.add(createScreenPromo("Wissen Erleben", null, null, "Wissen Erleben.mp4"));

		contents.getItems().add(createScreenEvent("Die VR Zone im Museum", "Abtauchen in den virtuellen Welten. "
			+ "Im VRlab präsentieren wir computergenerierte Inhalte, die auf Objekten und inhaltlichen Schwerpunkten des Museums basieren. "
			+ "Sie nutzen dazu eine VR-Brille. Mit Hilfe von zwei Controllern können Sie sich innerhalb der virtuellen Welt "
			+ "über weite Entfernungen „beamen“ ein Informationsmenü bedienen oder mit Objekten arbeiten.",
			"10:00", 45L, "Eingangshalle", "vrzone_001.jpg", "Die VR Zone im Museum.mp4"));
		request.getFields().put("contents", contents);

		return client.createNode(PROJECT_NAME, request).toCompletable().doOnComplete(() -> {
			log.info("Created screen 1");
		}).doOnError(err -> {
			log.error("Error while creating screen 1", err);
		});
	}

	private Completable createScreen2(NodeResponse folder) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("de");
		request.setParentNodeUuid(folder.getUuid());
		request.setSchemaName("Screen");
		request.getFields().put("id", new StringFieldImpl().setString("screen2"));
		request.getFields().put("name", new StringFieldImpl().setString("Promo Screen"));
		request.getFields().put("location", new StringFieldImpl().setString("Nebeneingang"));
		request.getFields().put("description", new StringFieldImpl().setString("Screen für Exhibitions"));

		MicronodeFieldListImpl contents = new MicronodeFieldListImpl();
		contents.getItems()
			.add(createScreenPromo("Test", "Test: Aus Ideen Erfolge machen", "ausstellung_Test.jpg", null));
		contents.getItems().add(createScreenPromo("Astronomie", "Wissenschaft von den Sternen", "ausstellung_Astronomie.jpg", null));
		contents.getItems().add(createScreenPromo("Historische Luftfahrt",
			"Blick in die Frühgeschichte der Luftfahrt mit vielen Original-Flugapparaten.", "ausstellung_Historische_Luftfahrt.jpg", null));
		request.getFields().put("contents", contents);

		return client.createNode(PROJECT_NAME, request).toCompletable().doOnComplete(() -> {
			log.info("Created screen 2");
		}).doOnError(err -> {
			log.error("Error while creating screen 2", err);
		});
	}

	private MicronodeResponse createScreenEvent(String title, String teaser, String start, Long duration, String location, String imageName,
		String videoName) {
		MicronodeResponse micronode = new MicronodeResponse().setMicroschema(new MicroschemaReferenceImpl().setName("ScreenEvent"));
		micronode.getFields().put("title", new StringFieldImpl().setString(title));
		micronode.getFields().put("teaser", new StringFieldImpl().setString(teaser));
		micronode.getFields().put("start", new StringFieldImpl().setString(start));
		micronode.getFields().put("duration", new NumberFieldImpl().setNumber(duration));
		micronode.getFields().put("location", new StringFieldImpl().setString(location));
		micronode.getFields().put("image", new NodeFieldImpl().setUuid(fileIdMap.get(imageName)));
		micronode.getFields().put("video", new NodeFieldImpl().setUuid(fileIdMap.get(videoName)));
		return micronode;
	}

	private Completable createScreenEventMicroschema() {
		MicroschemaCreateRequest request = new MicroschemaCreateRequest();
		request.setName(SCREEN_EVENT_MICROSCHEMA_NAME);
		request.addField(new StringFieldSchemaImpl().setName("title").setLabel("Event Titel"));
		request.addField(new StringFieldSchemaImpl().setName("teaser").setLabel("Teaser"));
		request.addField(new StringFieldSchemaImpl().setName("start").setLabel("Start"));
		request.addField(new NumberFieldSchemaImpl().setName("duration").setLabel("Dauer (min)"));
		request.addField(new StringFieldSchemaImpl().setName("location").setLabel("Ort"));
		request.addField(new NodeFieldSchemaImpl().setAllowedSchemas("Image").setName("image").setLabel("Bild"));
		request.addField(new NodeFieldSchemaImpl().setAllowedSchemas("Video").setName("video").setLabel("Video"));
		return linkMicroschema(client.createMicroschema(request).toSingle());
	}

	private MicronodeResponse createScreenPromo(String title, String teaser, String imageName, String videoName) {
		MicronodeResponse micronode = new MicronodeResponse().setMicroschema(new MicroschemaReferenceImpl().setName("ScreenExhibitionPromo"));
		micronode.getFields().put("title", new StringFieldImpl().setString(title));
		if (teaser != null) {
			micronode.getFields().put("teaser", new StringFieldImpl().setString(teaser));
		}
		if (imageName != null) {
			micronode.getFields().put("image", new NodeFieldImpl().setUuid(fileIdMap.get(imageName)));
		}
		if (videoName != null) {
			micronode.getFields().put("video", new NodeFieldImpl().setUuid(fileIdMap.get(videoName)));
		}
		return micronode;
	}

	private Completable createScreenExhibitionPromoMicroschema() {
		MicroschemaCreateRequest request = new MicroschemaCreateRequest();
		request.setName(SCREEN_PROMO_MICROSCHEMA_NAME);
		request.addField(new StringFieldSchemaImpl().setName("title").setLabel("Exhibition Titel"));
		request.addField(new StringFieldSchemaImpl().setName("teaser").setLabel("Teaser"));
		request.addField(new NodeFieldSchemaImpl().setAllowedSchemas("Image").setName("image").setLabel("Bild"));
		request.addField(new NodeFieldSchemaImpl().setAllowedSchemas("Video").setName("video").setLabel("Video"));
		return linkMicroschema(client.createMicroschema(request).toSingle());
	}

	public Completable createScreenSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Screen");
		request.setContainer(true);
		request.setDisplayField("name");
		request.setSegmentField("id");
		request.setContainer(false);
		request.addField(new StringFieldSchemaImpl().setName("id").setLabel("Id"));
		request.addField(new StringFieldSchemaImpl().setName("name").setLabel("Name"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Beschreibung"));
		request.addField(new StringFieldSchemaImpl().setName("location").setLabel("Position"));
		request.addField(new ListFieldSchemaImpl().setAllowedSchemas(SCREEN_EVENT_MICROSCHEMA_NAME, SCREEN_PROMO_MICROSCHEMA_NAME)
			.setListType("micronode").setName("contents").setLabel("Inhalt"));
		return linkSchema(client.createSchema(request).toSingle());
	}

	public Completable linkSchema(Single<SchemaResponse> schema) {
		return schema.flatMapCompletable(response -> {
			return client.assignSchemaToProject(PROJECT_NAME, response.getUuid()).toCompletable();
		});
	}

	public Completable linkMicroschema(Single<MicroschemaResponse> schema) {
		return schema.flatMapCompletable(response -> {
			return client.assignMicroschemaToProject(PROJECT_NAME, response.getUuid()).toCompletable();
		});
	}

	private Completable createAudioSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Audio");
		request.setContainer(false);
		request.setSegmentField("binary");
		request.setDisplayField("filename");
		request.addField(new StringFieldSchemaImpl().setName("filename").setLabel("Dateiname"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Beschreibung"));
		request.addField(new BinaryFieldSchemaImpl().setName("binary").setLabel("Datei"));

		return linkSchema(client.createSchema(request).toSingle());
	}

	private Completable createImageSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Image");
		request.setContainer(false);
		request.setSegmentField("binary");
		request.setDisplayField("filename");
		request.addField(new StringFieldSchemaImpl().setName("filename").setLabel("Dateiname"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Beschreibung"));
		request.addField(new BinaryFieldSchemaImpl().setName("binary").setLabel("Datei"));

		return linkSchema(client.createSchema(request).toSingle());
	}

	private Completable createVideoSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Video");
		request.setContainer(false);
		request.setSegmentField("binary");
		request.setDisplayField("filename");
		request.addField(new StringFieldSchemaImpl().setName("filename").setLabel("Dateiname"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Beschreibung"));
		request.addField(new BinaryFieldSchemaImpl().setName("binary").setLabel("Datei"));

		return linkSchema(client.createSchema(request).toSingle());
	}

	private Completable createExhibitionSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Exhibition");
		request.setDescription("Information on an exhibition");
		request.setSegmentField("slug");
		request.setDisplayField("title");
		request.setContainer(true);
		request.addField(new StringFieldSchemaImpl().setName("id").setLabel("Id"));
		request.addField(new StringFieldSchemaImpl().setName("slug").setLabel("Slug"));
		request.addField(new StringFieldSchemaImpl().setName("title").setLabel("Titel"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Bescreibung"));
		request.addField(new StringFieldSchemaImpl().setName("public_number").setLabel("Öffentliche Nummer"));
		request.addField(new NodeFieldSchemaImpl().setName("title_image").setLabel("Titel Bild"));
		request.addField(new ListFieldSchemaImpl().setListType("node").setName("images").setLabel("Bilder"));
		request.addField(new NodeFieldSchemaImpl().setName("audio").setLabel("Audioguide"));
		return linkSchema(client.createSchema(request).toSingle());
	}

}
