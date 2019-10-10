package com.gentics.mesh.musetech.text2speech;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AudioGenerator {

	private OkHttpClient client = new OkHttpClient.Builder().build();

	private final String token;

	public AudioGenerator(String token) {
		this.token = token;
	}

	public void text2speech(String text, String lang, File output) throws IOException {
		String languageCode = "en-US";
		String name = "en-US-Wavenet-D";
		double pitch = 0f;
		double speakingRate = 1f;
		switch (lang) {
		case "de":
			languageCode = "de-DE";
			name = "de-DE-Wavenet-B";
			pitch = -0.4f;
			speakingRate = 1.07f;
			break;
		case "gb":
			languageCode = "en-GB";
			name = "en-GB-Wavenet-B";
			pitch = 0f;
			speakingRate = 0.90f;
			break;
		}

		JsonObject result = dispatch(text, languageCode, name, pitch, speakingRate);
		if (result.containsKey("audioContent")) {
			byte[] decoded = Base64.getDecoder().decode(result.getString("audioContent"));
			if (output.exists()) {
				output.delete();
			}
			FileUtils.writeByteArrayToFile(output, decoded);
			System.out.println("File written");
		} else {
			System.out.println("Request seemed to have failed");
			System.out.println(result.encodePrettily());
		}
	}

	private JsonObject dispatch(String text, String languageCode, String name, double pitch, double speakingRate) throws IOException {
		JsonObject body = new JsonObject();
		body.put("input", new JsonObject().put("text", text));
		body.put("voice", new JsonObject().put("languageCode", languageCode).put("name", name));
		JsonObject audioConfig = new JsonObject();
		audioConfig.put("audioEncoding", "LINEAR16");
		audioConfig.put("pitch", pitch);
		audioConfig.put("speakingRate", speakingRate);
		audioConfig.put("effectsProfileId", new JsonArray().add("headphone-class-device"));
		body.put("audioConfig", audioConfig);

		Request request = new Request.Builder()
			.header("Accept", "application/json")
			.url("https://texttospeech.googleapis.com/v1beta1/text:synthesize&token=" + token)
			.post(RequestBody.create(MediaType.get("application/json"), body.encode()))
			.build();

		Response response = client().newCall(request).execute();
		if (!response.isSuccessful()) {
			throw new RuntimeException("Request failed {" + response.code() + "} {" + response.message() + "}");
		}
		return new JsonObject(response.body().string());
	}

	public OkHttpClient client() {
		return client;
	}

}
