package com.gentics.mesh.musetech;

import java.io.IOException;

import org.junit.Test;

import com.gentics.mesh.musetech.model.exhibit.Exhibit;
import com.gentics.mesh.musetech.model.exhibit.ExhibitList;

public class AudioGeneratorTest {

	private static final String TOKEN = "03AOLTBLQt4iooqSNw0e68_kSLGs_0okz4EF48gxl74BPlYVyf0a2F9373ZLgAxdjaPM7-NqClUcmVwAzjAWO2S5QTKXubAScQmvLBUB--tuFDvsCuhyFf8Y0YHDqx5ePugAIe9waMdz_IlpogkUeOo0CZg4a3O1U4_wDcRmHj8AW68Tqwk6TvhiRvmV3ITgO-WSsHpdbGX7WXrxYF8K7fIHu9Nw1UorE-uFzXcvE83iTTtjOsbPph6MRpkUvzVLRecMDyavPyuXWpzz2ZODmYEhFoqdet-tg99ew5dPGEtFSJbwY67Zckre3Rvx-QgcTI9h6jHkLOo2AN4_0YmoGJRJ48--fQpffeEY-Pr6CChmJrBzpoCKXlTsBE1Kc0VpVY_ZLG_HSB0msZN4uRvqgj7k6o0qbD-Vu_TOCM65QoVJLFMrTpmBkwOBw";

	@Test
	public void testGenerator() throws IOException {
		AudioGenerator gen = new AudioGenerator(TOKEN);
		// gen.text2speech("hello world 123", "en", new File("target/audio_en.wav"));
		ExhibitList list = ExhibitList.load();
		for (Exhibit ex : list.getExhibits()) {
		}
	}
}
