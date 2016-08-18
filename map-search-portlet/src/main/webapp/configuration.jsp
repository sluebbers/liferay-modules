<%--
    configuration.jsp: Configure the map-search-portlet's preferences.
    
    Created:    2016-07-21 22:26 by Christian Berndt
    Modified:   2016-08-18 00:42 by Christian Berndt
    Version:    1.0.6
--%>

<%@ include file="/html/init.jsp"%>

<%@page import="com.liferay.portlet.asset.model.AssetVocabulary"%>
<%@page import="com.liferay.portlet.asset.service.AssetVocabularyServiceUtil"%>

<%
    PortletURL portletURL = renderResponse.createRenderURL();

    List<AssetVocabulary> vocabularies = AssetVocabularyServiceUtil
            .getGroupVocabularies(themeDisplay.getScopeGroupId());
%>

<liferay-portlet:actionURL portletConfiguration="true"
    var="configurationActionURL" />

<liferay-portlet:renderURL portletConfiguration="true"
    var="configurationRenderURL" />

<aui:form action="<%=configurationActionURL%>" enctype="multipart/form-data" method="post" name="fm">
    
    <aui:input name="<%=Constants.CMD%>" type="hidden"
        value="<%=Constants.UPDATE%>" />
    <aui:input name="redirect" type="hidden"
        value="<%=configurationRenderURL%>" />
        
    <liferay-ui:panel-container id="mapsearchportletSettingsPanelContainer" persistState="<%= true %>">
    
        <liferay-ui:panel id="mapsearchportletSearchPanel" title="search" extended="true">
            
            <aui:input name="classNames" value="<%= classNames %>" type="textarea" 
                helpMessage="class-names-help"/> 
                
            <aui:select name="vocabularyId" helpMessage="vocabulary-id-help">
                <aui:option label="select-vocabulary" value="0"/>
                <% for (AssetVocabulary vocabulary: vocabularies) { %>
                    <aui:option label="<%= vocabulary.getTitle(locale) %>" value="<%= vocabulary.getVocabularyId() %>"
                        selected="<%= vocabularyId == vocabulary.getVocabularyId() %>"/>
                <% } %>
            </aui:select>                           
                       
        </liferay-ui:panel> 

        <liferay-ui:panel id="mapsearchportletMapSettingsPanel" title="map-settings" extended="true">
        
            <aui:row>
            
                <aui:col span="6">
            
                    <aui:input name="mapCenter" value="<%= mapCenter %>" 
                        helpMessage="map-center-help"/>
                        
                    <aui:input name="mapHeight" value="<%= mapHeight %>"
                        helpMessage="map-height-help"/>
                         
                    <aui:select name="mapZoom" helpMessage="map-zoom-help">
                    <% for (int i=0; i<18; i++) { %>
                        <aui:option value="<%= i %>" selected="<%= i == GetterUtil.getInteger(mapZoom) %>"><%= i %></aui:option>
                    <% } %>
                    </aui:select>
                    
                </aui:col>
                
                <aui:col span="6">
            
                    <aui:input name="tilesURL" cssClass="full-width"
                        helpMessage="tiles-url-help" label="tiles-url"
                        value="<%= tilesURL %>"/>
        
                    <aui:input name="tilesCopyright" cssClass="full-width" 
                        helpMessage="tiles-copyright-help" value="<%= tilesCopyright %>"/>
                        
                    <aui:input name="markerIconConfig" type="textarea" value="<%= markerIconConfig %>"
                        helpMessage="marker-icon-config-help" />                
                        
                    <aui:input name="useDivIcon" type="checkbox" checked="<%= useDivIcon %>" 
                        helpMessage="use-div-icon-help"/>   
                
                </aui:col>

            </aui:row> 
                                   
        </liferay-ui:panel>
        
        <liferay-ui:panel id="mapsearchportletDataTablePanel" title="data-table" extended="true">                
                
                                      
            <aui:input name="dataTableColumnDefs" type="textarea" 
                helpMessage="data-table-column-defs-help" 
                value="<%= dataTableColumnDefs %>"/>   
                
            <aui:input name="dataTableDom" 
                helpMessage="data-table-dom-help" 
                value="<%= dataTableDom %>"/>                               
                                      
        </liferay-ui:panel>               
        
        <liferay-ui:panel id="mapsearchportletMiscellaneousPanel" title="miscellaneous" extended="true">
                
            <aui:input name="useGlobalJQuery" type="checkbox" checked="<%= useGlobalJQuery %>" 
                label="use-global-jquery"
                helpMessage="use-global-jquery-help"/>  
                
            <aui:field-wrapper label="view-by-default" helpMessage="view-by-default-help">
                <aui:input name="viewByDefault" type="radio" value="true"
                    checked="<%= viewByDefault%>" label="yes" inlineField="true" />
                <aui:input name="viewByDefault" type="radio" value="false"
                    checked="<%=!viewByDefault%>" label="no" inlineField="true" />
            </aui:field-wrapper> 
            
            <aui:field-wrapper label="view-in-context" helpMessage="view-in-context-help">
                <aui:input name="viewInContext" type="radio" value="true"
                    checked="<%= viewInContext%>" label="yes" inlineField="true" />
                <aui:input name="viewInContext" type="radio" value="false"
                    checked="<%=!viewInContext%>" label="no" inlineField="true" />
            </aui:field-wrapper>                                          
                       
        </liferay-ui:panel>

    </liferay-ui:panel-container>

    <aui:button-row>
        <aui:button type="submit" />
    </aui:button-row>

</aui:form>
