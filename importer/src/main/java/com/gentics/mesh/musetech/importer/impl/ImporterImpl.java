package com.gentics.mesh.musetech.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gentics.mesh.core.rest.micronode.MicronodeResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.impl.NodeFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.MicronodeFieldListImpl;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.schema.impl.BinaryFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.MicroschemaReferenceImpl;
import com.gentics.mesh.core.rest.schema.impl.NodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NumberFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.StringFieldSchemaImpl;
import com.gentics.mesh.musetech.Tuple;
import com.gentics.mesh.musetech.model.exhibit.Exhibit;
import com.gentics.mesh.musetech.model.exhibit.ExhibitContent;
import com.gentics.mesh.musetech.model.exhibit.ExhibitList;
import com.gentics.mesh.musetech.model.image.Image;
import com.gentics.mesh.musetech.model.image.ImageList;
import com.gentics.mesh.musetech.model.video.Video;
import com.gentics.mesh.musetech.model.video.VideoList;
import com.gentics.mesh.parameter.client.NodeParametersImpl;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ImporterImpl extends AbstractImporter {

	private static final Logger log = LoggerFactory.getLogger(ImporterImpl.class);

	private static final String SCREEN_EVENT_MICROSCHEMA_NAME = "ScreenEvent";

	private static final String SCREEN_PROMO_MICROSCHEMA_NAME = "ScreenExhibitionPromo";

	private final String projectName;
	private final ExhibitList exhibitionList;
	private final ImageList imageList;
	private final VideoList videoList;

	public ImporterImpl(String projectName) throws IOException {
		this.projectName = projectName;
		this.exhibitionList = ExhibitList.load();
		this.imageList = ImageList.load();
		this.videoList = VideoList.load();
	}

	private Completable importScreens(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		operations.add(createScreen1(folder));
		operations.add(createScreen2(folder));
		return Completable.merge(operations);
	}

	private Completable importContents(NodeResponse folder) throws FileNotFoundException, IOException {
		Set<Completable> operations = new HashSet<>();
		for (Exhibit ex : exhibitionList.getExhibits()) {
			operations.add(createExhibition(folder.getUuid(), ex, ex.getEnglish()).flatMapCompletable(tupl -> {
				return translateExhibition(tupl.getA(), tupl.getB(), ex, ex.getGerman());
			}));
		}
		return Completable.merge(operations);
	}

	private Completable translateExhibition(NodeResponse node, String audioFolderUuid, Exhibit ex, ExhibitContent content) {
		return Single.just(new NodeUpdateRequest()).flatMapCompletable(request -> {
			request.setLanguage("de");

			List<Completable> contents = new ArrayList<>();
			String audioUrl = content.getAudioName();
			if (audioUrl != null) {
				String filename = Paths.get(audioUrl).getFileName().toString();
				contents.add(
					createAudio(audioFolderUuid, "en", filename).doOnSuccess(audioResp -> {
						log.info("Add audio reference {" + audioResp.getUuid() + "}");
						request.getFields().put("audio", new NodeFieldImpl().setUuid(audioResp.getUuid()));
					}).ignoreElement());
			}

			String description = content.getDescription();
			String name = content.getName();
			// String id = ex.getId();
			String publicNumber = ex.getPublicNumber();
			request.getFields().put("name", new StringFieldImpl().setString(name));
			request.getFields().put("description", new StringFieldImpl().setString(description));
			// request.getFields().put("id", new StringFieldImpl().setString(id));
			request.getFields().put("public_number", new StringFieldImpl().setString(publicNumber));
			request.getFields().put("slug", new StringFieldImpl().setString(publicNumber + ":de"));

			// Copy from first language
			request.getFields().put("images", node.getFields().getNodeFieldList("images"));
			request.getFields().put("title_image", node.getFields().getNodeField("title_image"));

			return Completable.merge(contents).andThen(client.updateNode(projectName, node.getUuid(), request)
				.toCompletable()
				.doOnComplete(() -> {
					log.info("Updated exhibition {" + node.getUuid() + "/en}");
				}).doOnError(err -> {
					log.info("Error while translating exhibition {" + node.getUuid() + "/en}");
				}));
		});
	}

	private Single<Tuple<NodeResponse, String>> createExhibition(String folderUuid, Exhibit exhibit, ExhibitContent content) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setSchemaName("Exhibition");
		request.setParentNodeUuid(folderUuid);

		String description = content.getDescription();
		String name = content.getName();
		// String id = exhibit.getId();
		String publicNumber = exhibit.getPublicNumber();

		request.getFields().put("name", new StringFieldImpl().setString(name));
		request.getFields().put("description", new StringFieldImpl().setString(description));
		// request.getFields().put("id", new StringFieldImpl().setString(id));
		request.getFields().put("public_number", new StringFieldImpl().setString(publicNumber));
		request.getFields().put("slug", new StringFieldImpl().setString(publicNumber + ":en"));

		return client.createNode(projectName, request, new NodeParametersImpl().setLanguages("en"))
			.toSingle()
			.doOnSuccess(node -> {
				log.info("Created exhibition {" + publicNumber + "} with uuid" + node.getUuid());
			})
			.doOnError(err -> {
				log.error("Error while creating exhibition {" + publicNumber + "}", err);
			})
			.flatMap(node -> {

				Single<NodeResponse> imagesFolder = createFolder(node.getUuid(), "image", "Images");
				Single<NodeResponse> audiosFolder = createFolder(node.getUuid(), "audio", "Audios");

				Single<Tuple<NodeResponse, String>> tupS = Single.zip(imagesFolder, audiosFolder, (imgFolder, auFolder) -> {
					Single<Tuple<NodeResponse, String>> td = storeAndUpdateRelatedContents(node.getUuid(), node.getLanguage(), node.getVersion(),
						exhibit, exhibit.getEnglish(), imgFolder, auFolder)
							.flatMap(nodeR -> {
								return Single.just(Tuple.tuple(nodeR, auFolder.getUuid()));
							});
					return td;
				}).flatMap(r -> r);
				return tupS;
			});

	}

	private Single<NodeResponse> storeAndUpdateRelatedContents(String uuid, String lang, String version, Exhibit ex, ExhibitContent content,
		NodeResponse imgFolder,
		NodeResponse auFolder) {
		return Single.just(new NodeUpdateRequest()).flatMap(request -> {

			List<Completable> contents = new ArrayList<>();

			String audio = content.getAudioName();
			if (audio != null) {
				String filename = Paths.get(audio).getFileName().toString();
				contents.add(
					createAudio(auFolder.getUuid(), "de", filename).doOnSuccess(audioResp -> {
						log.info("Add audio reference {" + uuid + "}");
						request.getFields().put("audio", new NodeFieldImpl().setUuid(audioResp.getUuid()));
					}).ignoreElement());
			}

			// for (String image : ex.getImages()) {
			// contents.add(
			// createImage(imgFolder.getUuid(), image)
			// .doOnSuccess(imageResp -> {
			// log.info("Add images reference {" + uuid + "}");
			// NodeFieldListItemImpl item = new NodeFieldListItemImpl().setUuid(imageResp.getUuid());
			// NodeFieldList list = request.getFields().getNodeFieldList("images");
			// if (exhibitionList == null) {
			// exhibitionList = new NodeFieldListImpl();
			// request.getFields().put("images", exhibitionList);
			// }
			// exhibitionList.add(item);
			//
			// String detailImage = ex.getTitleImage();
			// if (detailImage != null) {
			// if (detailImage.equals(image)) {
			// request.getFields().put("title_image", new NodeFieldImpl().setUuid(imageResp.getUuid()));
			// }
			// }
			//
			// }).ignoreElement());
			// }

			return Completable.merge(contents).andThen(Single.just(request));
		}).flatMap(request -> {
			return updateExhibition(uuid, lang, version, request);
		});
	}

	private Single<NodeResponse> updateExhibition(String uuid, String lang, String version, NodeUpdateRequest request) {
		log.info("Update exhibition {" + uuid + "}");
		request.setLanguage(lang);
		request.setVersion(version);
		return client.updateNode(projectName, uuid, request, new NodeParametersImpl().setLanguages(lang)).toSingle()
			.doOnError(err -> {
				log.error("Error while updating exhibition {" + uuid + "}", err);
			})
			.doOnSuccess(node -> {
				log.info("Exhibition {" + uuid + "} updated.");
			});
	}

	private Single<NodeResponse> createImage(String uuid, Image image) {
		String name = image.getName();
		String attr = image.getAttribution();
		String license = image.getLicense();
		String source = image.getSource();
		File imageFile = new File("data/image/" + name);
		if (!imageFile.exists()) {
			return Single.error(new FileNotFoundException("Image " + name + " could not be found." + image.toJson()));
		}
		String filename = imageFile.getName();
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setParentNodeUuid(uuid);
		request.setSchemaName("Image");
		request.getFields().put("filename", new StringFieldImpl().setString(filename));
		request.getFields().put("license", new StringFieldImpl().setString(license));
		request.getFields().put("source", new StringFieldImpl().setString(source));
		request.getFields().put("attribution", new StringFieldImpl().setString(attr));

		return client.createNode(projectName, request).toSingle().flatMap(node -> {
			fileIdMap.put(filename, node.getUuid());
			InputStream ins = new FileInputStream(imageFile);
			long size = imageFile.length();
			return client.updateNodeBinaryField(projectName, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", ins, size, filename,
				"image/jpeg").toSingle();
		}).doOnError(err -> {
			log.error("Error while creating image {" + name + "} for node {" + uuid + "}");
		}).doOnSuccess(node -> {
			log.info("Created image {" + name + "} with id {" + node.getUuid() + "}");
		});
	}

	private Single<NodeResponse> createVideo(String uuid, Video video) {
		String filename = video.getName();
		String description = video.getDescription();

		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setParentNodeUuid(uuid);
		request.setSchemaName("Video");
		request.getFields().put("filename", new StringFieldImpl().setString(filename));
		request.getFields().put("description", new StringFieldImpl().setString(description));

		return client.createNode(projectName, request).toSingle().flatMap(node -> {
			fileIdMap.put(filename, node.getUuid());
			File file = new File("data/video/" + filename);
			InputStream ins = new FileInputStream(file);
			long size = file.length();
			String mimeType = MimeMapping.getMimeTypeForFilename(filename);

			return client.updateNodeBinaryField(projectName, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", ins, size, filename,
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

		return client.createNode(projectName, request, new NodeParametersImpl().setLanguages(lang)).toSingle().flatMap(node -> {
			File file = new File("data/audio/" + filename);
			InputStream ins = new FileInputStream(file);
			long size = file.length();
			return client.updateNodeBinaryField(projectName, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", ins, size, filename,
				mimeType).toSingle();
		}).doOnError(err -> {
			log.error("Error while creating audio {" + filename + "} for node {" + uuid + "}");
		}).doOnSuccess(node -> {
			log.info("Created audio {" + filename + "} with id {" + node.getUuid() + "}");
		});

	}

	@Override
	public Completable createFolders(ProjectResponse project) {
		String uuid = project.getRootNode().getUuid();
		Set<Completable> operations = new HashSet<>();
		operations.add(createFolder(uuid, "image", "Bilder").flatMapCompletable(this::importImages));
		operations.add(createFolder(uuid, "video", "Videos").flatMapCompletable(this::importVideos));
		operations.add(createFolder(uuid, "exhibitions", "Exhibitions").flatMapCompletable(this::importContents)
			.andThen(createFolder(uuid, "screens", "Screens").flatMapCompletable(this::importScreens)));
		return Completable.merge(operations);
	}

	private Completable importImages(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();

		for (Image image : imageList.getImages()) {
			operations.add(createImage(folder.getUuid(), image).ignoreElement());
		}
		return Completable.merge(operations);
	}

	private Completable importVideos(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		for (Video video : videoList.getVideos()) {
			operations.add(createVideo(folder.getUuid(), video).ignoreElement());
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
		return client.createNode(projectName, request).toSingle().doOnError(err -> {
			log.error("Error while creating folder {" + name + "}", err);
		});
	}

	@Override
	public Completable createSchemas() {
		Set<Completable> operations = new HashSet<>();
		operations.add(createExhibitionSchema());
		operations.add(createAudioSchema());
		operations.add(createScreenSchema());
		operations.add(createVideoSchema());
		operations.add(createImageSchema());
		return Completable.merge(operations);
	}

	@Override
	public Completable createMicroschemas() {
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

		return client.createNode(projectName, request).toCompletable().doOnComplete(() -> {
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

		return client.createNode(projectName, request).toCompletable().doOnComplete(() -> {
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
		return linkMicroschema(createOrUpdateMicroschema(request), projectName);
	}

	public MicronodeResponse createScreenPromo(String title, String teaser, String imageName, String videoName) {
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
		request.addField(new NodeFieldSchemaImpl().setAllowedSchemas("Image").setName("image").setLabel("Image"));
		request.addField(new NodeFieldSchemaImpl().setAllowedSchemas("Video").setName("video").setLabel("Video"));
		return linkMicroschema(createOrUpdateMicroschema(request), projectName);
	}

	private Completable createScreenSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Screen");
		request.setContainer(true);
		request.setDisplayField("name");
		request.setSegmentField("id");
		request.setContainer(false);
		request.addField(new StringFieldSchemaImpl().setName("id").setLabel("Id"));
		request.addField(new StringFieldSchemaImpl().setName("name").setLabel("Name"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Description"));
		request.addField(new StringFieldSchemaImpl().setName("location").setLabel("Location"));
		request.addField(new ListFieldSchemaImpl().setAllowedSchemas(SCREEN_EVENT_MICROSCHEMA_NAME, SCREEN_PROMO_MICROSCHEMA_NAME)
			.setListType("micronode").setName("contents").setLabel("Inhalt"));
		return linkSchema(createOrUpdateSchema(request), projectName);
	}

	private Completable createAudioSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Audio");
		request.setContainer(false);
		request.setSegmentField("binary");
		request.setDisplayField("filename");
		request.addField(new StringFieldSchemaImpl().setName("filename").setLabel("Filename"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Description"));
		request.addField(new BinaryFieldSchemaImpl().setName("binary").setLabel("File"));
		return linkSchema(createOrUpdateSchema(request), projectName);
	}

	private Completable createImageSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Image");
		request.setContainer(false);
		request.setSegmentField("binary");
		request.setDisplayField("filename");
		request.addField(new StringFieldSchemaImpl().setName("filename").setLabel("Filename"));
		request.addField(new StringFieldSchemaImpl().setName("license").setLabel("License"));
		request.addField(new StringFieldSchemaImpl().setName("source").setLabel("Source"));
		request.addField(new StringFieldSchemaImpl().setName("attribution").setLabel("Attribution"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Description"));
		request.addField(new BinaryFieldSchemaImpl().setName("binary").setLabel("File"));
		return linkSchema(createOrUpdateSchema(request), projectName);
	}

	private Completable createVideoSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Video");
		request.setContainer(false);
		request.setSegmentField("binary");
		request.setDisplayField("filename");
		request.addField(new StringFieldSchemaImpl().setName("filename").setLabel("Filename"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Description"));
		request.addField(new BinaryFieldSchemaImpl().setName("binary").setLabel("File"));
		return linkSchema(createOrUpdateSchema(request), projectName);
	}

	private Completable createExhibitionSchema() {
		SchemaCreateRequest request = new SchemaCreateRequest();
		request.setName("Exhibition");
		request.setDescription("Information on an exhibition");
		request.setSegmentField("slug");
		request.setDisplayField("name");
		request.setContainer(true);
		request.addField(new StringFieldSchemaImpl().setName("id").setLabel("Id"));
		request.addField(new StringFieldSchemaImpl().setName("slug").setLabel("Slug"));
		request.addField(new StringFieldSchemaImpl().setName("name").setLabel("Name"));
		request.addField(new StringFieldSchemaImpl().setName("description").setLabel("Description"));
		request.addField(new StringFieldSchemaImpl().setName("public_number").setLabel("Public number"));
		request.addField(new NodeFieldSchemaImpl().setName("title_image").setLabel("Titel Image"));
		request.addField(new ListFieldSchemaImpl().setListType("node").setName("images").setLabel("Images"));
		request.addField(new NodeFieldSchemaImpl().setName("audio").setLabel("Audioguide"));
		return linkSchema(createOrUpdateSchema(request), projectName);
	}

	@Override
	public String projectName() {
		return projectName;
	}

	@Override
	public void purge() {
		client.findProjectByName(projectName).toSingle().flatMap(p -> {
			return client.deleteProject(p.getUuid()).toSingle();
		}).ignoreElement().blockingAwait();
	}

}