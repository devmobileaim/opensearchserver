/*
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2017 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaeksoft.searchlib.crawler.rest;

import com.jaeksoft.searchlib.analysis.LanguageEnum;
import com.jaeksoft.searchlib.crawler.common.process.FieldMapCrawlItem;
import com.jaeksoft.searchlib.crawler.web.database.CredentialItem;
import com.jaeksoft.searchlib.crawler.web.spider.HttpDownloader;
import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.Variables;
import com.jaeksoft.searchlib.util.XPathParser;
import com.jaeksoft.searchlib.util.XmlWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.UnsupportedEncodingException;

public class RestCrawlItem extends FieldMapCrawlItem<RestCrawlItem, RestCrawlThread, RestCrawlMaster> {

	private String name;

	private String url;

	private String sequenceParameter;

	private Integer sequenceFromInclusive;

	private Integer sequenceToExclusive;

	private Integer sequenceIncrement;

	private HttpDownloader.Method method;

	private CredentialItem credential;

	private LanguageEnum lang;

	private int bufferSize;

	private String pathDocument;

	private HttpDownloader.Method callbackMethod;

	private String callbackUrl;

	private CallbackMode callbackMode;

	private String callbackQueryParameter;

	private String callbackPayload;

	public static enum CallbackMode {
		NO_CALL, ONE_CALL_PER_DOCUMENT, ONE_CALL_FOR_ALL_DOCUMENTS;

		public final static CallbackMode find(String name) {
			for (CallbackMode mode : values())
				if (mode.name().equalsIgnoreCase(name))
					return mode;
			return NO_CALL;
		}
	}

	public RestCrawlItem(RestCrawlMaster crawlMaster) {
		super(crawlMaster, new RestFieldMap());
		name = null;
		url = null;
		method = HttpDownloader.Method.GET;
		credential = new CredentialItem();
		sequenceParameter = null;
		sequenceFromInclusive = null;
		sequenceToExclusive = null;
		sequenceIncrement = null;
		pathDocument = null;
		lang = LanguageEnum.UNDEFINED;
		bufferSize = 100;
		callbackMethod = HttpDownloader.Method.GET;
		callbackUrl = null;
		callbackMode = CallbackMode.NO_CALL;
		callbackQueryParameter = null;
		callbackPayload = null;
	}

	public RestCrawlItem(RestCrawlMaster crawlMaster, String name) {
		this(crawlMaster);
		this.name = name;
	}

	protected RestCrawlItem(RestCrawlItem crawl) {
		this((RestCrawlMaster) crawl.threadMaster);
		crawl.copyTo(this);
	}

	public RestCrawlItem duplicate() {
		return new RestCrawlItem(this);
	}

	public void apply(Variables variables) {
		if (variables == null)
			return;
		url = variables.replace(url);
		sequenceParameter = variables.replace(sequenceParameter);
		pathDocument = variables.replace(pathDocument);
		credential.apply(variables);
	}

	@Override
	public void copyTo(RestCrawlItem crawl) {
		super.copyTo(crawl);
		crawl.setName(this.getName());
		crawl.url = this.url;
		crawl.method = this.method;
		crawl.pathDocument = this.pathDocument;
		this.credential.copyTo(crawl.credential);
		crawl.lang = this.lang;
		crawl.bufferSize = this.bufferSize;
		crawl.callbackMethod = callbackMethod;
		crawl.callbackUrl = callbackUrl;
		crawl.callbackMode = callbackMode;
		crawl.callbackQueryParameter = callbackQueryParameter;
		crawl.callbackPayload = callbackPayload;
		crawl.sequenceParameter = this.sequenceParameter;
		crawl.sequenceFromInclusive = this.sequenceFromInclusive;
		crawl.sequenceToExclusive = this.sequenceToExclusive;
		crawl.sequenceIncrement = this.sequenceIncrement;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the sequenceParameter
	 */
	public String getSequenceParameter() {
		return sequenceParameter;
	}

	/**
	 * @param sequenceParameter
	 *            the sequenceParameter to set
	 */
	public void setSequenceParameter(String sequenceParameter) {
		this.sequenceParameter = sequenceParameter;
	}

	/**
	 * @return the sequenceFromInclusive
	 */
	public Integer getSequenceFromInclusive() {
		return sequenceFromInclusive;
	}

	/**
	 * @param sequenceFromInclusive
	 *            the sequenceFromInclusive to set
	 */
	public void setSequenceFromInclusive(Integer sequenceFromInclusive) {
		this.sequenceFromInclusive = sequenceFromInclusive;
	}

	/**
	 * @return the sequenceToExclusive
	 */
	public Integer getSequenceToExclusive() {
		return sequenceToExclusive;
	}

	/**
	 * @param sequenceToExclusive
	 *            the sequenceToExclusive to set
	 */
	public void setSequenceToExclusive(Integer sequenceToExclusive) {
		this.sequenceToExclusive = sequenceToExclusive;
	}

	/**
	 * @return the sequenceIncrement
	 */
	public Integer getSequenceIncrement() {
		return sequenceIncrement;
	}

	/**
	 * @param sequenceIncrement
	 *            the sequenceIncrement to set
	 */
	public void setSequenceIncrement(Integer sequenceIncrement) {
		this.sequenceIncrement = sequenceIncrement;
	}

	/**
	 * @return the credential
	 */
	public CredentialItem getCredential() {
		return credential;
	}

	/**
	 * @param lang
	 *            the lang to set
	 */
	public void setLang(LanguageEnum lang) {
		this.lang = lang;
	}

	/**
	 * @return the lang
	 */
	public LanguageEnum getLang() {
		return lang;
	}

	/**
	 * @return the fieldMap
	 */
	@Override
	public RestFieldMap getFieldMap() {
		return (RestFieldMap) super.getFieldMap();
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	protected final static String REST_CRAWL_NODE_NAME = "restCrawl";
	protected final static String REST_CRAWL_ATTR_NAME = "name";
	protected final static String REST_CRAWL_NODE_CREDENTIAL = "credential";
	protected final static String REST_CRAWL_ATTR_URL = "url";
	protected final static String REST_CRAWL_ATTR_SEQUENCE_PARAMETER = "seqParameter";
	protected final static String REST_CRAWL_ATTR_SEQUENCE_FROM = "seqFrom";
	protected final static String REST_CRAWL_ATTR_SEQUENCE_TO = "seqTo";
	protected final static String REST_CRAWL_ATTR_SEQUENCE_INCREMENT = "seqIncrement";
	protected final static String REST_CRAWL_ATTR_LANG = "lang";
	protected final static String REST_CRAWL_ATTR_METHOD = "method";
	protected final static String REST_CRAWL_ATTR_BUFFER_SIZE = "bufferSize";
	protected final static String REST_CRAWL_NODE_NAME_MAP = "map";
	protected final static String REST_CRAWL_NODE_DOC_PATH = "documentPath";
	protected final static String REST_CRAWL_NODE_CALLBACK = "callback";
	protected final static String REST_CRAWL_CALLBACK_ATTR_METHOD = "method";
	protected final static String REST_CRAWL_CALLBACK_ATTR_URL = "url";
	protected final static String REST_CRAWL_CALLBACK_ATTR_MODE = "mode";
	protected final static String REST_CRAWL_CALLBACK_ATTR_QUERY_PARAM = "queryParam";

	public RestCrawlItem(RestCrawlMaster crawlMaster, XPathParser xpp, Node item) throws XPathExpressionException {
		this(crawlMaster);
		setName(XPathParser.getAttributeString(item, REST_CRAWL_ATTR_NAME));
		setUrl(XPathParser.getAttributeString(item, REST_CRAWL_ATTR_URL));
		setSequenceParameter(XPathParser.getAttributeString(item, REST_CRAWL_ATTR_SEQUENCE_PARAMETER));
		setSequenceFromInclusive(DomUtils.getAttributeInteger(item, REST_CRAWL_ATTR_SEQUENCE_FROM, null));
		setSequenceToExclusive(DomUtils.getAttributeInteger(item, REST_CRAWL_ATTR_SEQUENCE_TO, null));
		setMethod(HttpDownloader.Method.find(DomUtils.getAttributeText(item, REST_CRAWL_ATTR_METHOD),
				HttpDownloader.Method.GET));
		setLang(LanguageEnum.findByCode(XPathParser.getAttributeString(item, REST_CRAWL_ATTR_LANG)));
		setBufferSize(XPathParser.getAttributeValue(item, REST_CRAWL_ATTR_BUFFER_SIZE));
		Node mapNode = xpp.getNode(item, REST_CRAWL_NODE_NAME_MAP);
		if (mapNode != null)
			getFieldMap().load(mapNode);
		Node pathNode = xpp.getNode(item, REST_CRAWL_NODE_DOC_PATH);
		if (pathNode != null)
			setPathDocument(StringEscapeUtils.unescapeXml(pathNode.getTextContent()));
		Node credNode = xpp.getNode(item, REST_CRAWL_NODE_CREDENTIAL);
		if (credNode != null)
			credential = CredentialItem.fromXml(credNode);
		Node callBackNode = DomUtils.getFirstNode(item, REST_CRAWL_NODE_CALLBACK);
		if (callBackNode != null) {
			setCallbackMethod(
					HttpDownloader.Method.find(DomUtils.getAttributeText(callBackNode, REST_CRAWL_CALLBACK_ATTR_METHOD),
							HttpDownloader.Method.GET));
			setCallbackUrl(DomUtils.getAttributeText(callBackNode, REST_CRAWL_CALLBACK_ATTR_URL));
			setCallbackMode(CallbackMode.find(DomUtils.getAttributeText(callBackNode, REST_CRAWL_CALLBACK_ATTR_MODE)));
			setCallbackQueryParameter(DomUtils.getAttributeText(callBackNode, REST_CRAWL_CALLBACK_ATTR_QUERY_PARAM));
			setCallbackPayload(callBackNode.getTextContent());
		}
	}

	public void writeXml(XmlWriter xmlWriter) throws SAXException, UnsupportedEncodingException {
		xmlWriter.startElement(REST_CRAWL_NODE_NAME, REST_CRAWL_ATTR_NAME, getName(), REST_CRAWL_ATTR_URL, getUrl(),
				REST_CRAWL_ATTR_SEQUENCE_PARAMETER, getSequenceParameter(), REST_CRAWL_ATTR_SEQUENCE_FROM,
				sequenceFromInclusive == null ? null : sequenceFromInclusive.toString(), REST_CRAWL_ATTR_SEQUENCE_TO,
				sequenceToExclusive == null ? null : sequenceToExclusive.toString(), REST_CRAWL_ATTR_SEQUENCE_INCREMENT,
				sequenceIncrement == null ? null : sequenceIncrement.toString(), REST_CRAWL_ATTR_METHOD, method.name(),
				REST_CRAWL_ATTR_LANG, getLang().getCode(), REST_CRAWL_ATTR_BUFFER_SIZE,
				Integer.toString(getBufferSize()));
		xmlWriter.startElement(REST_CRAWL_NODE_NAME_MAP);
		getFieldMap().store(xmlWriter);
		xmlWriter.endElement();
		xmlWriter.startElement(REST_CRAWL_NODE_CALLBACK, REST_CRAWL_CALLBACK_ATTR_METHOD, callbackMethod.name(),
				REST_CRAWL_CALLBACK_ATTR_MODE, callbackMode.name(), REST_CRAWL_CALLBACK_ATTR_URL, callbackUrl,
				REST_CRAWL_CALLBACK_ATTR_QUERY_PARAM, callbackQueryParameter);
		if (!StringUtils.isEmpty(callbackPayload))
			xmlWriter.textNode(callbackPayload);
		xmlWriter.endElement();
		xmlWriter.writeSubTextNodeIfAny(REST_CRAWL_NODE_DOC_PATH, xmlWriter.escapeXml(getPathDocument()));
		credential.writeXml(xmlWriter);
		xmlWriter.endElement();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(RestCrawlItem o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @return the pathDocument
	 */
	public String getPathDocument() {
		return pathDocument;
	}

	/**
	 * @param pathDocument
	 *            the pathDocument to set
	 */
	public void setPathDocument(String pathDocument) {
		this.pathDocument = pathDocument;
	}

	/**
	 * @return the method
	 */
	public HttpDownloader.Method getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(HttpDownloader.Method method) {
		this.method = method;
	}

	/**
	 * @return the callbackMethod
	 */
	public HttpDownloader.Method getCallbackMethod() {
		return callbackMethod;
	}

	/**
	 * @param callbackMethod
	 *            the callbackMethod to set
	 */
	public void setCallbackMethod(HttpDownloader.Method callbackMethod) {
		this.callbackMethod = callbackMethod;
	}

	/**
	 * @return the callbackUrl
	 */
	public String getCallbackUrl() {
		return callbackUrl;
	}

	/**
	 * @param callbackUrl
	 *            the callbackUrl to set
	 */
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}

	/**
	 * @return the callbackMode
	 */
	public CallbackMode getCallbackMode() {
		return callbackMode;
	}

	/**
	 * @param callbackMode
	 *            the callbackMode to set
	 */
	public void setCallbackMode(CallbackMode callbackMode) {
		this.callbackMode = callbackMode;
	}

	/**
	 * @return the callbackQueryParameter
	 */
	public String getCallbackQueryParameter() {
		return callbackQueryParameter;
	}

	/**
	 * @param callbackQueryParameter
	 *            the callbackQueryParameter to set
	 */
	public void setCallbackQueryParameter(String callbackQueryParameter) {
		this.callbackQueryParameter = callbackQueryParameter;
	}

	/**
	 * @return the callbackPayload
	 */
	public String getCallbackPayload() {
		return callbackPayload;
	}

	/**
	 * @param callbackPayload
	 *            the callbackPayload to set
	 */
	public void setCallbackPayload(String callbackPayload) {
		this.callbackPayload = callbackPayload;
	}

}
