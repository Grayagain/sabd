package lab1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void processLeavesAttributesUntouchedByDefault() throws Exception {
        Path input = tempDir.resolve("input.xml");
        Path output = tempDir.resolve("output.xml");

        Files.write(input,
                "<employee id=\"ABC123\"><name>John Doe</name></employee>".getBytes(StandardCharsets.UTF_8));

        XmlProcessor.process(input.toString(), output.toString(), true);

        Document document = parse(output);

        assertEquals("ABC123", document.getDocumentElement().getAttribute("id"));
        assertEquals(Obfuscator.obfuscate("John Doe"),
                document.getElementsByTagName("name").item(0).getTextContent());
    }

    @Test
    void processCanObfuscateAndRestoreAttributes() throws Exception {
        Path input = tempDir.resolve("input.xml");
        Path obfuscated = tempDir.resolve("obfuscated.xml");
        Path restored = tempDir.resolve("restored.xml");

        Files.write(input,
                "<employee id=\"ABC123\"><name>John Doe</name></employee>".getBytes(StandardCharsets.UTF_8));

        XmlProcessor.process(input.toString(), obfuscated.toString(), true, true);
        XmlProcessor.process(obfuscated.toString(), restored.toString(), false, true);

        Document obfuscatedDocument = parse(obfuscated);
        Document restoredDocument = parse(restored);

        assertEquals(Obfuscator.obfuscate("ABC123"), obfuscatedDocument.getDocumentElement().getAttribute("id"));
        assertEquals("ABC123", restoredDocument.getDocumentElement().getAttribute("id"));
        assertEquals("John Doe", restoredDocument.getElementsByTagName("name").item(0).getTextContent());
    }

    private static Document parse(Path path) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
    }
}
