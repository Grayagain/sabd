package lab1;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ObfuscatorTest {

    @Test
    void testRoundTrip() {
        String original = "ABC123XYZ";

        String obf = Obfuscator.obfuscate(original);
        String back = Obfuscator.deobfuscate(obf);

        assertEquals(original, back);
    }

    @Test
    void testXmlProcessingLeavesAttributesUnchangedByDefault() throws Exception {
        Path input = Files.createTempFile("lab1-input", ".xml");
        Path output = Files.createTempFile("lab1-output", ".xml");

        Files.writeString(input, "<employee id=\"A12\"><name>Alex</name></employee>");

        XmlProcessor.process(input.toString(), output.toString(), true, false);

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(output.toFile());

        assertEquals("A12", document.getDocumentElement().getAttribute("id"));
        assertNotEquals("Alex", document.getElementsByTagName("name").item(0).getTextContent().trim());
    }

    @Test
    void testXmlProcessingCanIncludeAttributes() throws Exception {
        Path input = Files.createTempFile("lab1-input", ".xml");
        Path output = Files.createTempFile("lab1-output", ".xml");
        Path restored = Files.createTempFile("lab1-restored", ".xml");

        Files.writeString(input, "<employee id=\"A12\" code=\"ZX9\"><name>Alex</name></employee>");

        XmlProcessor.process(input.toString(), output.toString(), true, true);

        Document obfuscated = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(output.toFile());

        assertNotEquals("A12", obfuscated.getDocumentElement().getAttribute("id"));
        assertNotEquals("ZX9", obfuscated.getDocumentElement().getAttribute("code"));

        XmlProcessor.process(output.toString(), restored.toString(), false, true);

        Document deobfuscated = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(restored.toFile());

        assertEquals("A12", deobfuscated.getDocumentElement().getAttribute("id"));
        assertEquals("ZX9", deobfuscated.getDocumentElement().getAttribute("code"));
        assertEquals("Alex", deobfuscated.getElementsByTagName("name").item(0).getTextContent().trim());
    }
}
