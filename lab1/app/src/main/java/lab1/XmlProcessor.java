package lab1;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XmlProcessor {

    public static void process(String input, String output, boolean obfuscate, boolean includeAttributes) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new File(input));
        doc.getDocumentElement().normalize();

        traverse(doc.getDocumentElement(), obfuscate, includeAttributes);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(doc), new StreamResult(new File(output)));
    }

    private static void traverse(Node node, boolean obfuscate, boolean includeAttributes) {

        if (includeAttributes && node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attributes = node.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                attribute.setNodeValue(transform(attribute.getNodeValue(), obfuscate));
            }
        }

        if (node.getNodeType() == Node.TEXT_NODE) {

            String text = node.getTextContent();

            if (text != null && !text.trim().isEmpty()) {
                node.setTextContent(transform(text.trim(), obfuscate));
            }
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            traverse(children.item(i), obfuscate, includeAttributes);
        }
    }

    private static String transform(String value, boolean obfuscate) {
        return obfuscate
                ? Obfuscator.obfuscate(value)
                : Obfuscator.deobfuscate(value);
    }
}
