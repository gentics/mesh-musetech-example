package com.gentics.mesh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Strings;

import io.vertx.core.json.JsonObject;

public class ContentTransformer {

	public List<Tuple<Exhibition, Exhibition>> transform() throws FileNotFoundException, IOException {
		List<Tuple<Exhibition, Exhibition>> tups = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(new File("data/content_stops.json"))) {
			JsonObject json = new JsonObject(IOUtils.toString(fis));

			JsonObject exhibitionsEn = json.getJsonObject("data").getJsonObject("stops").getJsonObject("1");
			JsonObject exhibitionsDe = json.getJsonObject("data").getJsonObject("stops").getJsonObject("2");

			for (String name : exhibitionsDe.fieldNames()) {
				JsonObject exDeJson = exhibitionsDe.getJsonObject(name);
				JsonObject exEnJson = exhibitionsEn.getJsonObject(name);

				String publicNumber = exDeJson.getString("public_number");
				// Only import exhibits with a number
				if (!Strings.isNullOrEmpty(publicNumber) && !publicNumber.equals("0")) {

					System.out.println(exDeJson);
					Exhibition exDe = new Exhibition(exDeJson);
					Exhibition exEn = new Exhibition(exEnJson);
					tups.add(Tuple.tuple(exDe, exEn));
				}
			}
		}
		return tups;
	}

}
