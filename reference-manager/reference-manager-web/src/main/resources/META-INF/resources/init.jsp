<%--
    init.jsp: Common setup code for the reference manager portlet.
    
    Created:    2016-01-10 22:51 by Christian Berndt
    Modified:   2016-12-01 18:10 by Christian Berndt
    Version:    1.1.2
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@taglib uri="http://liferay.com/tld/aui" prefix="aui"%>
<%@taglib uri="http://liferay.com/tld/security" prefix="liferay-security"%>
<%@taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme"%>
<%@taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui"%>
<%@taglib uri="http://liferay.com/tld/util" prefix="liferay-util"%>

<%@page import="ch.inofix.referencemanager.constants.BibliographyActionKeys"%>
<%@page import="ch.inofix.referencemanager.constants.ReferenceActionKeys"%>
<%@page import="ch.inofix.referencemanager.model.Bibliography"%>
<%@page import="ch.inofix.referencemanager.model.Reference"%>
<%@page import="ch.inofix.referencemanager.service.permission.BibliographyPermission"%>
<%@page import="ch.inofix.referencemanager.service.permission.ReferenceManagerPortletPermission"%>
<%@page import="ch.inofix.referencemanager.service.permission.ReferencePermission"%>
<%@page import="ch.inofix.referencemanager.service.BibliographyServiceUtil"%>
<%@page import="ch.inofix.referencemanager.service.ReferenceServiceUtil"%>
<%@page import="ch.inofix.referencemanager.web.internal.constants.BibliographyWebKeys"%>
<%@page import="ch.inofix.referencemanager.web.internal.constants.ReferenceWebKeys"%>
<%@page import="ch.inofix.referencemanager.web.internal.search.BibliographySearchTerms"%>
<%@page import="ch.inofix.referencemanager.web.internal.search.BibliographySearch"%>
<%@page import="ch.inofix.referencemanager.web.internal.search.ReferenceSearch"%>
<%@page import="ch.inofix.referencemanager.web.internal.search.ReferenceSearchTerms"%>

<%@page import="com.liferay.asset.kernel.model.AssetRenderer"%>
<%@page import="com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil"%>
<%@page import="com.liferay.asset.kernel.model.AssetRendererFactory"%>
<%@page import="com.liferay.portal.kernel.dao.search.ResultRow"%>
<%@page import="com.liferay.portal.kernel.dao.search.SearchContainer"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="com.liferay.portal.kernel.search.Document"%>
<%@page import="com.liferay.portal.kernel.search.Hits"%>
<%@page import="com.liferay.portal.kernel.search.Sort"%>
<%@page import="com.liferay.portal.kernel.security.auth.PrincipalException"%>
<%@page import="com.liferay.portal.kernel.upload.UploadException"%>
<%@page import="com.liferay.portal.kernel.util.Constants"%>
<%@page import="com.liferay.portal.kernel.util.GetterUtil"%>
<%@page import="com.liferay.portal.kernel.util.ListUtil"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="com.liferay.portal.kernel.util.StringPool"%>
<%@page import="com.liferay.portal.kernel.util.Validator"%>
<%@page import="com.liferay.portal.kernel.util.WebKeys"%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<%@page import="javax.portlet.PortletURL"%>

<portlet:defineObjects />

<liferay-theme:defineObjects />

<%
	PortletURL portletURL = renderResponse.createRenderURL();

	String currentURL = portletURL.toString();
%>