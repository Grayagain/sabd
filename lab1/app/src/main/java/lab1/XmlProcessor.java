package lab1;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XmlProcessor {

    public static void process(String input, String output, boolean obfuscate) throws Exception {
        process(input, output, obfuscate, false);
    }

    public static void process(String input, String output, boolean obfuscate, boolean processAttributes) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new File(input));
        doc.getDocumentElement().normalize();

        traverse(doc.getDocumentElement(), obfuscate, processAttributes);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(doc), new StreamResult(new File(output)));
    }

    private static void traverse(Node node, boolean obfuscate, boolean processAttributes) {

        if (processAttributes && node.getNodeType() == Node.ELEMENT_NODE) {
            transformAttributes(node.getAttributes(), obfuscate);
        }

        if (node.getNodeType() == Node.TEXT_NODE) {

            String text = node.getTextContent().trim();

            if (!text.isEmpty()) {
                node.setTextContent(
                        obfuscate
                                ? Obfuscator.obfuscate(text)
                                : Obfuscator.deobfuscate(text)
                );
            }
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            traverse(children.item(i), obfuscate, processAttributes);
        }
    }

    private static void transformAttributes(NamedNodeMap attributes, boolean obfuscate) {

        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String value = attribute.getNodeValue();

            if (value != null && !value.trim().isEmpty()) {
                attribute.setNodeValue(
                        obfuscate
                                ? Obfuscator.obfuscate(value)
                                : Obfuscator.deobfuscate(value)
                );
            }
        }
    }
}
