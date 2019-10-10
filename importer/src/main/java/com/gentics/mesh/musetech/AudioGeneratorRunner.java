package com.gentics.mesh.musetech;

import java.io.File;
import java.io.IOException;

import com.gentics.mesh.musetech.model.exhibit.Exhibit;
import com.gentics.mesh.musetech.model.exhibit.ExhibitList;
import com.gentics.mesh.musetech.text2speech.AudioGenerator;

public class AudioGeneratorRunner {

	private static final String TOKEN = "TOKEN";

	public static void main(String[] args) throws IOException {
		AudioGenerator gen = new AudioGenerator(TOKEN);
		ExhibitList list = ExhibitList.load();
		for (Exhibit ex : list.getExhibits()) {
			File gbFile = new File("data/audio/" + ex.getPublicNumber() + "_gb.wav");
			if (!gbFile.exists()) {
				gen.text2speech(ex.getEnglish().getDescription(), "gb", gbFile);
			}

			File deFile = new File("data/audio/" + ex.getPublicNumber() + "_de.wav");
			if (!deFile.exists()) {
				gen.text2speech(ex.getGerman().getDescription(), "de", deFile);
			}
		}
	}
}
