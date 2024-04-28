package lang24.common.logger;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import lang24.common.report.*;

/**
 * A logger generating an XML/XSL log of an individual compiler phase.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class Logger implements AutoCloseable {

	/** The name of the XML file to be produced. */
	private final String xmlFileName;

	/** The name of the relating XSL file (to be included in the XML header). */
	private final String xslFileName;

	/** The entire XML document being constructed. */
	private final Document doc;

	/** The stack of the XML documents (used during construction). */
	private final Stack<Element> elements = new Stack<Element>();

	/**
	 * Constructs a new logger.
	 * 
	 * @param phaseName   The name of the phase being logged.
	 * @param xmlFileName The name of the XML file to be produced.
	 * @param xslFileName The name of the relating XSL file.
	 */
	public Logger(final String phaseName, final String xmlFileName, final String xslFileName) {
		this.xmlFileName = xmlFileName;
		this.xslFileName = xslFileName;

		// Prepare a new log document.
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (final ParserConfigurationException ex) {
			throw new Report.InternalError();
		}

		// Create the root element representing the entire phase.
		final Element phase = doc.createElement(phaseName);
		doc.appendChild(phase);
		elements.push(phase);

		// Add XSL declaration.
		final ProcessingInstruction xsl = doc.createProcessingInstruction("xml-stylesheet",
				"type=\"text/xsl\" href=\"" + this.xslFileName + "\"");
		doc.insertBefore(xsl, phase);
	}

	@Override
	public void close() {
		try {
			elements.pop();
		} catch (EmptyStackException ex) {
			throw new Report.InternalError();
		}
		if (!elements.empty())
			throw new Report.InternalError();

		// Dump the log document out.
		try {
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(new File(xmlFileName));
			transformer.transform(source, result);
		} catch (final TransformerException ex) {
			Report.warning("Cannot open log file '" + xmlFileName + "'.");
		}
	}

	/**
	 * Starts a new XML element (within the active XML element) and makes it active.
	 * Only one XML element can be active at each moment.
	 * 
	 * @param tagName The tag name of a new XML element.
	 */
	public void begElement(final String tagName) {
		try {
			final Element element = doc.createElement(tagName);
			elements.peek().appendChild(element);
			elements.push(element);
		} catch (final EmptyStackException ex) {
			throw new Report.InternalError();
		}
	}

	/**
	 * Ends the current XML element and makes its parent element active. Only one
	 * XML element can be active at each moment.
	 */
	public void endElement() {
		try {
			elements.pop();
		} catch (final EmptyStackException ex) {
			throw new Report.InternalError();
		}
	}

	/**
	 * Adds an attribute to the active XML element.
	 * 
	 * @param attrName  The name of an attribute.
	 * @param attrValue The value of an attribute.
	 */
	public void addAttribute(final String attrName, final String attrValue) {
		try {
			elements.peek().setAttribute(attrName, attrValue);
		} catch (final EmptyStackException ex) {
			throw new Report.InternalError();
		}
	}

}
