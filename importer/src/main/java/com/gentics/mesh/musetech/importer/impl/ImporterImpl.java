package com.gentics.mesh.musetech.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.gentics.mesh.core.rest.micronode.MicronodeResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.node.FieldMap;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.node.NodeUpdateRequest;
import com.gentics.mesh.core.rest.node.field.BinaryField;
import com.gentics.mesh.core.rest.node.field.impl.NodeFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.NumberFieldImpl;
import com.gentics.mesh.core.rest.node.field.impl.StringFieldImpl;
import com.gentics.mesh.core.rest.node.field.list.NodeFieldList;
import com.gentics.mesh.core.rest.node.field.list.impl.MicronodeFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NodeFieldListImpl;
import com.gentics.mesh.core.rest.node.field.list.impl.NodeFieldListItemImpl;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.schema.impl.MicroschemaReferenceImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaCreateRequest;
import com.gentics.mesh.core.rest.user.NodeReference;
import com.gentics.mesh.importer.helper.ImportUtils;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.musetech.model.exhibit.Exhibit;
import com.gentics.mesh.musetech.model.exhibit.ExhibitContent;
import com.gentics.mesh.musetech.model.exhibit.ExhibitList;
import com.gentics.mesh.musetech.model.exhibit.ExhibitLocation;
import com.gentics.mesh.musetech.model.image.Image;
import com.gentics.mesh.musetech.model.image.ImageList;
import com.gentics.mesh.musetech.model.screen.Screen;
import com.gentics.mesh.musetech.model.screen.ScreenContent;
import com.gentics.mesh.musetech.model.screen.ScreenList;
import com.gentics.mesh.musetech.model.video.Video;
import com.gentics.mesh.musetech.model.video.VideoList;
import com.gentics.mesh.parameter.client.NodeParametersImpl;
import com.gentics.mesh.rest.client.MeshRestClient;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ImporterImpl extends AbstractImporter {

	private static final Logger log = LoggerFactory.getLogger(ImporterImpl.class);

	private static final String EXHIBIT_LOCATION_NAME = "Location";

	private final String projectName;
	private final ExhibitList exhibitList;
	private final ImageList imageList;
	private final VideoList videoList;
	private final ScreenList screenList;
	private final List<SchemaCreateRequest> schemas;
	private final List<MicroschemaCreateRequest> microschemas;
	private final List<NodeResponse> nodes;
	private final List<NodeResponse> curators;
	private final List<NodeResponse> tours;

	public ImporterImpl(String hostname, int port, boolean ssl, String projectName) throws IOException {
		super(hostname, port, ssl);
		this.projectName = projectName;
		this.exhibitList = ExhibitList.load();
		this.imageList = ImageList.load();
		this.videoList = VideoList.load();
		this.screenList = ScreenList.load();
		this.schemas = ImportUtils.loadSchemas("data/schemas");
		this.microschemas = ImportUtils.loadMicroschemas("data/microschemas");
		this.nodes = ImportUtils.loadNodes("data/nodes");
		this.curators = ImportUtils.loadNodes("data/curators");
		this.tours = ImportUtils.loadNodes("data/tours");
	}

	private Completable importScreens(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		for (Screen screen : screenList.getScreens()) {
			operations.add(createScreen(folder, screen));
		}
		return Completable.merge(operations);
	}

	private Completable importContents(NodeResponse folder) throws FileNotFoundException, IOException {
		Set<Completable> operations = new HashSet<>();
		for (Exhibit ex : exhibitList.getExhibits()) {
			operations.add(createExhibit(folder.getUuid(), ex, ex.getEnglish()).flatMapCompletable(tupl -> {
				return translateExhibit(tupl.getA(), tupl.getB(), ex, ex.getGerman());
			}));
		}
		return Completable.merge(operations);
	}

	private Completable translateExhibit(NodeResponse node, String audioFolderUuid, Exhibit ex, ExhibitContent content) {
		return Single.just(new NodeUpdateRequest()).flatMapCompletable(request -> {
			request.setLanguage("de");

			List<Completable> contents = new ArrayList<>();
			String audioName = content.getAudioName();
			contents.add(
				createAudio(audioFolderUuid, "de", ex.getPublicNumber(), audioName).doOnSuccess(audioResp -> {
					log.info("Add audio reference {" + audioResp.getUuid() + "}");
					request.getFields().put("audio", new NodeFieldImpl().setUuid(audioResp.getUuid()));
				}).ignoreElement());

			String description = content.getDescription();
			String name = content.getName();
			// String id = ex.getId();
			String publicNumber = ex.getPublicNumber();
			request.getFields().put("name", new StringFieldImpl().setString(name));
			request.getFields().put("description", new StringFieldImpl().setString(description));
			setCommonExhibitInfo(request.getFields(), ex);
			request.getFields().put("slug", new StringFieldImpl().setString(publicNumber + ":de"));

			// Copy from first language
			request.getFields().put("images", node.getFields().getNodeFieldList("images"));
			request.getFields().put("title_image", node.getFields().getNodeField("title_image"));

			return Completable.merge(contents).andThen(Completable.defer(() -> {
				return client.updateNode(projectName, node.getUuid(), request)
					.toCompletable();
			})
				.doOnComplete(() -> {
					log.info("Updated exhibit {" + node.getUuid() + "/en}");
				}).doOnError(err -> {
					log.info("Error while translating exhibit {" + node.getUuid() + "/en}");
				}));
		});
	}

	private Single<Tuple<NodeResponse, String>> createExhibit(String folderUuid, Exhibit exhibit, ExhibitContent content) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setSchemaName("Exhibit");
		request.setParentNodeUuid(folderUuid);

		String description = content.getDescription();
		String name = content.getName();

		// String id = exhibit.getId();
		String publicNumber = exhibit.getPublicNumber();
		request.getFields().put("name", new StringFieldImpl().setString(name));
		request.getFields().put("description", new StringFieldImpl().setString(description));
		request.getFields().put("slug", new StringFieldImpl().setString(publicNumber + ":en"));

		setCommonExhibitInfo(request.getFields(), exhibit);

		return client.createNode(projectName, request, new NodeParametersImpl().setLanguages("en"))
			.toSingle()
			.doOnSuccess(node -> {
				log.info("Created exhibit {" + publicNumber + "} with uuid" + node.getUuid());
			})
			.doOnError(err -> {
				log.error("Error while creating exhibit {" + publicNumber + "}", err);
			})
			.flatMap(node -> {

				Single<NodeResponse> imagesFolder = createFolder(node.getUuid(), "image", "Image");
				Single<NodeResponse> audiosFolder = createFolder(node.getUuid(), "audio", "Audio");

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

	private void setCommonExhibitInfo(FieldMap fields, Exhibit exhibit) {
		String publicNumber = exhibit.getPublicNumber();
		fields.put("public_number", new StringFieldImpl().setString(publicNumber));

		ExhibitLocation loc = exhibit.getLocation();
		if (loc == null) {
			throw new RuntimeException("Exhibit {" + exhibit.getPublicNumber() + "} has no location");
		}
		MicronodeResponse location = new MicronodeResponse();
		location.getFields().put("building", new StringFieldImpl().setString(loc.getBuilding()));
		location.getFields().put("level", new NumberFieldImpl().setNumber(loc.getLevel()));
		location.getFields().put("section", new StringFieldImpl().setString(loc.getSection()));
		location.setMicroschema(new MicroschemaReferenceImpl().setName(EXHIBIT_LOCATION_NAME));
		fields.put("location", location);

	}

	private Single<NodeResponse> storeAndUpdateRelatedContents(String uuid, String lang, String version, Exhibit ex, ExhibitContent content,
		NodeResponse imgFolder,
		NodeResponse auFolder) {
		return Single.just(new NodeUpdateRequest()).flatMap(request -> {

			List<Completable> contents = new ArrayList<>();

			String audio = content.getAudioName();
			contents.add(
				createAudio(auFolder.getUuid(), "en", ex.getPublicNumber(), audio).doOnSuccess(audioResp -> {
					log.info("Add audio reference {" + uuid + "}");
					request.getFields().put("audio", new NodeFieldImpl().setUuid(audioResp.getUuid()));
				}).ignoreElement());

			// Iterate over all images and create them within the exhibit image folder
			List<String> exhibitImageList = ex.getImages();
			if (exhibitImageList == null) {
				return Single.error(new RuntimeException("The exhibit {" + ex.getPublicNumber() + "} has no image list"));
			}
			for (String imageName : exhibitImageList) {
				Image image = findImage(ex, imageName);
				if (image == null) {
					return Single.error(new RuntimeException("Could not find image with name {" + imageName + "}"));
				}
				contents.add(
					createImage(imgFolder.getUuid(), image)
						.doOnSuccess(imageResp -> {
							// Now add node references for images to the exhibit fields

							log.info("Adding images reference {" + uuid + "}");
							NodeFieldListItemImpl item = new NodeFieldListItemImpl().setUuid(imageResp.getUuid());
							NodeFieldList list = request.getFields().getNodeFieldList("images");
							if (list == null) {
								list = new NodeFieldListImpl();
								request.getFields().put("images", list);
							}
							list.add(item);

							String detailImage = ex.getTitleImage();
							if (detailImage != null) {
								if (detailImage.equals(image.getName())) {
									log.info("Adding detail image reference {" + detailImage + "}");
									request.getFields().put("title_image", new NodeFieldImpl().setUuid(imageResp.getUuid()));
								}
							} else {
								log.error("Detail image {" + detailImage + "} could not be found for exhibit {" + ex.getPublicNumber() + "}");
							}

						}).ignoreElement());

			}
			return Completable.merge(contents).andThen(Single.just(request));
		}).flatMap(request -> {
			return updateExhibit(uuid, lang, version, request);
		});
	}

	private Image findImage(Exhibit ex, String imageName) {
		Optional<Image> op = imageList.getImages().stream().filter(i -> i.getName().equals(imageName)).findFirst();
		if (op.isPresent()) {
			return op.get();
		} else {
			log.error("Could not find image for exhibit {" + ex.getPublicNumber() + "} {" + imageName + "}");
		}
		return null;
	}

	private Single<NodeResponse> updateExhibit(String uuid, String lang, String version, NodeUpdateRequest request) {
		log.info("Update exhibit {" + uuid + "}");
		request.setLanguage(lang);
		request.setVersion(version);
		return client.updateNode(projectName, uuid, request, new NodeParametersImpl().setLanguages(lang)).toSingle()
			.doOnError(err -> {
				log.error("Error while updating exhibit {" + uuid + "}", err);
			})
			.doOnSuccess(node -> {
				log.info("Exhibit {" + uuid + "} updated.");
			});
	}

	private Single<NodeResponse> createImage(String parentNodeUuid, Image image) {
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
		request.setParentNodeUuid(parentNodeUuid);
		request.setSchemaName("Image");
		request.getFields().put("filename", new StringFieldImpl().setString(filename));
		request.getFields().put("license", new StringFieldImpl().setString(license));
		request.getFields().put("source", new StringFieldImpl().setString(source));
		request.getFields().put("attribution", new StringFieldImpl().setString(attr));
		return client.createNode(image.getUuid(), projectName, request).toSingle().flatMap(node -> {
			fileIdMap.put(filename, node.getUuid());
			InputStream ins = new FileInputStream(imageFile);
			long size = imageFile.length();
			String version = node.getVersion();
			// TODO: Somehow some nodes get version 0.2 after creation - wtf?
			version = "draft";
			Single<NodeResponse> upload = client
				.updateNodeBinaryField(projectName, node.getUuid(), node.getLanguage(), version, "binary", ins, size, filename,
					"image/jpeg")
				.toSingle().doOnError(err -> {
					log.error("Got error on upload for node {" + node.getUuid() + "} on version {" + node.getVersion() + "}");
				});
			return upload.flatMap(updatedNode -> {
				// Check whether the image contains focal point info
				if (image.getFpx() != null && image.getFpy() != null) {
					BinaryField binField = updatedNode.getFields().getBinaryField("binary");
					binField.setFocalPoint(image.getFpx(), image.getFpy());
					NodeUpdateRequest nodeUpdateRequest = new NodeUpdateRequest();
					String version2 = updatedNode.getVersion();
					version2 = "draft";
					nodeUpdateRequest.setVersion(version2);
					nodeUpdateRequest.setLanguage("en");
					nodeUpdateRequest.getFields().put("binary", binField);
					return client.updateNode(projectName, node.getUuid(), nodeUpdateRequest).toSingle();
				} else {
					return Single.just(updatedNode);
				}
			});
		}).doOnError(err -> {
			log.error("Error while creating image {" + name + "/" + image.getUuid() + "} for node {" + parentNodeUuid + "}");
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

	private Maybe<NodeResponse> createAudio(String uuid, String lang, String publicNumber, String audioName) {
		String audioLang = lang.equals("en") ? "gb" : lang;
		// The audioName field can be used to override the default version.
		if (audioName == null) {
			audioName = publicNumber + "_" + audioLang + ".mp3";
		}
		File audioFile = new File("data/audio/" + audioName);

		if (audioFile.exists()) {
			NodeCreateRequest request = new NodeCreateRequest();
			request.setLanguage(lang);
			request.setParentNodeUuid(uuid);
			request.setSchemaName("Audio");
			request.getFields().put("filename", new StringFieldImpl().setString(audioName));
			String mimeType = MimeMapping.getMimeTypeForFilename(audioName);

			return client.createNode(projectName, request, new NodeParametersImpl().setLanguages(lang)).toSingle().flatMap(node -> {
				InputStream ins = new FileInputStream(audioFile);
				long size = audioFile.length();
				return client
					.updateNodeBinaryField(projectName, node.getUuid(), node.getLanguage(), node.getVersion(), "binary", ins, size,
						audioFile.getName(),
						mimeType, new NodeParametersImpl().setLanguages(lang))
					.toSingle();
			}).doOnError(err -> {
				log.error("Error while creating audio {" + audioFile + "} for node {" + uuid + "}");
			}).doOnSuccess(node -> {
				log.info("Created audio {" + audioFile + "} with id {" + node.getUuid() + "}");
			}).toMaybe();
		} else {
			return Maybe.empty();
		}

	}

	@Override
	public Completable createFolders(ProjectResponse project) {
		String uuid = project.getRootNode().getUuid();
		Set<Completable> operations = new HashSet<>();
		Completable importImages = createFolder(uuid, "image", "Images").flatMapCompletable(this::importImages);
		Completable importCurators = createFolder(uuid, "curators", "Curators").flatMapCompletable(this::importCurators);
		Completable importTours = createFolder(uuid, "tours", "Tours").flatMapCompletable(this::importTours);
		operations.add(importImages.andThen(importCurators).andThen(importTours));
		operations.add(createFolder(uuid, "video", "Videos").flatMapCompletable(this::importVideos));
		operations.add(createFolder(uuid, "exhibits", "Exhibits").flatMapCompletable(this::importContents)
			.andThen(createFolder(uuid, "screens", "Screens").flatMapCompletable(this::importScreens)));
		return Completable.merge(operations);
	}

	/**
	 * Import images - Create a new node for each image in the given folder node.
	 * 
	 * @param folder
	 * @return
	 */
	private Completable importImages(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		for (Image image : imageList.getImages()) {
			operations.add(createImage(folder.getUuid(), image).ignoreElement());
		}
		return Completable.merge(operations);
	}

	private Completable importCurators(NodeResponse folder) {
		return importNodes(client, folder.getUuid(), curators, projectName);
	}

	private Completable importTours(NodeResponse folder) {
		return importNodes(client, folder.getUuid(), tours, projectName);
	}

	/**
	 * Import videos - Create a new node for each video in the given folder node.
	 * 
	 * @param folder
	 * @return
	 */
	private Completable importVideos(NodeResponse folder) {
		Set<Completable> operations = new HashSet<>();
		for (Video video : videoList.getVideos()) {
			if (video.getImportFlag()) {
				operations.add(createVideo(folder.getUuid(), video).ignoreElement());
			}
		}
		return Completable.merge(operations);
	}

	private Single<NodeResponse> createFolder(String parentUuid, String slug, String name) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setParentNodeUuid(parentUuid);
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
		return ImportUtils.createSchemas(client, schemas, projectName);
	}

	@Override
	public Completable createMicroschemas() {
		return ImportUtils.createMicroschemas(client, microschemas, projectName);
	}

	private Completable createScreen(NodeResponse folder, Screen screen) {
		NodeCreateRequest request = new NodeCreateRequest();
		request.setLanguage("en");
		request.setParentNodeUuid(folder.getUuid());
		request.setSchemaName("Screen");
		request.getFields().put("id", new StringFieldImpl().setString(screen.getId()));
		request.getFields().put("name", new StringFieldImpl().setString(screen.getName()));
		request.getFields().put("description", new StringFieldImpl().setString(screen.getDescription()));
		request.getFields().put("location", new StringFieldImpl().setString(screen.getLocation()));

		MicronodeFieldListImpl contents = new MicronodeFieldListImpl();
		for (ScreenContent content : screen.getContents()) {
			switch (content.getType()) {
			case EVENT:
				contents.getItems().add(createScreenEvent(content));
				break;
			case PROMO:
				contents.getItems().add(createScreenPromo(content));
				break;
			default:
				throw new RuntimeException("Unknown type {" + content.getType() + "}");
			}
		}
		request.getFields().put("contents", contents);

		return client.createNode(projectName, request).toCompletable().doOnComplete(() -> {
			log.info("Created screen " + screen.getId());
		}).doOnError(err -> {
			log.error("Error while creating screen " + screen.getId(), err);
		});
	}

	private MicronodeResponse createScreenEvent(ScreenContent content) {
		MicronodeResponse micronode = new MicronodeResponse().setMicroschema(new MicroschemaReferenceImpl().setName("ScreenEvent"));
		micronode.getFields().put("title", new StringFieldImpl().setString(content.getTitle()));
		micronode.getFields().put("teaser", new StringFieldImpl().setString(content.getTeaser()));
		micronode.getFields().put("start", new StringFieldImpl().setString(content.getStart()));
		micronode.getFields().put("duration", new NumberFieldImpl().setNumber(content.getDuration()));
		micronode.getFields().put("location", new StringFieldImpl().setString(content.getLocation()));

		addMedia(micronode, content);
		return micronode;
	}

	private void addMedia(MicronodeResponse micronode, ScreenContent content) {
		String image = content.getImage();
		if (image != null) {
			String mappedImage = fileIdMap.get(content.getImage());
			if (mappedImage != null) {
				micronode.getFields().put("image", new NodeFieldImpl().setUuid(mappedImage));
			} else {
				log.error("Could not find image {" + image + "} Omitting image..");
			}
		}

		String video = content.getVideo();
		if (video != null) {
			String mappedVideo = fileIdMap.get(content.getVideo());
			if (mappedVideo != null) {
				micronode.getFields().put("video", new NodeFieldImpl().setUuid(mappedVideo));
			} else {
				log.error("Could not find video {" + video + "} Omitting video..");
			}
		}
	}

	public MicronodeResponse createScreenPromo(ScreenContent content) {
		MicronodeResponse micronode = new MicronodeResponse().setMicroschema(new MicroschemaReferenceImpl().setName("ScreenExhibitPromo"));
		micronode.getFields().put("title", new StringFieldImpl().setString(content.getTitle()));
		if (content.getTeaser() != null) {
			micronode.getFields().put("teaser", new StringFieldImpl().setString(content.getTeaser()));
		}
		addMedia(micronode, content);
		return micronode;
	}

	@Override
	public String projectName() {
		return projectName;
	}

	@Override
	public void purge() {
		ImportUtils.purge(client, projectName);
	}

	@Override
	public Completable importNodes(ProjectResponse project) {
		return ImportUtils.importNodes(client, nodes, project);
	}

	public static Completable importNodes(MeshRestClient client, String parentNodeUuid, List<NodeResponse> nodes, String projectName) {
		return Observable.fromIterable(nodes)
			.map(node -> {
				NodeReference nodeRef = new NodeReference();
				nodeRef.setUuid(parentNodeUuid);
				node.setParentNode(nodeRef);
				return node;
			})
			.flatMapCompletable(node -> {
				String uuid = node.getUuid();
				NodeCreateRequest request = JsonUtil.readValue(node.toJson(), NodeCreateRequest.class);
				return client.createNode(uuid, projectName, request).toCompletable();
			});
	}

}
