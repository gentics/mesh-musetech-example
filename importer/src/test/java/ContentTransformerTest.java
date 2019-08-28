import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.gentics.mesh.ContentTransformer;
import com.gentics.mesh.Exhibition;
import com.gentics.mesh.Tuple;

public class ContentTransformerTest {

	@Test
	public void testTransform() throws FileNotFoundException, IOException {
		ContentTransformer t = new ContentTransformer();
		List<Tuple<Exhibition, Exhibition>> tu = t.transform();
		for (Tuple<Exhibition, Exhibition> tup : tu) {
			// System.out.println(tup.getA().toString());
			// System.out.println("---------------");
			// System.out.println(tup.getB().toString());

			String a1 = tup.getA().getAudio();
			if (a1 != null) {
				String filename = Paths.get(a1).getFileName().toString();
				System.out.println(filename);
			}

			String a2 = tup.getB().getAudio();
			if (a2 != null) {
				String filename2 = Paths.get(a2).getFileName().toString();
				System.out.println(filename2);
			}

		}
	}
}
