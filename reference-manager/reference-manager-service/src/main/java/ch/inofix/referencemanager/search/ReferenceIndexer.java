package ch.inofix.referencemanager.search;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.IndexWriterHelperUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import ch.inofix.referencemanager.model.Reference;
import ch.inofix.referencemanager.service.ReferenceLocalService;
import ch.inofix.referencemanager.service.permission.ReferencePermission;

/**
 * 
 * @author Christian Berndt
 * @created 2016-11-18 01:15
 * @modified 2016-11-26 11:58
 * @version 1.0.1
 *
 */
@Component(immediate = true, service = Indexer.class)
public class ReferenceIndexer extends BaseIndexer<Reference> {

    public static final String CLASS_NAME = Reference.class.getName();

    public ReferenceIndexer() {
        setDefaultSelectedFieldNames(Field.ASSET_TAG_NAMES, "author", Field.COMPANY_ID, Field.ENTRY_CLASS_NAME,
                Field.ENTRY_CLASS_PK, Field.GROUP_ID, Field.MODIFIED_DATE, Field.SCOPE_GROUP_ID, Field.TITLE, Field.UID,
                Field.URL, "year");
        setFilterSearch(true);
        setPermissionAware(true);
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    @Override
    public boolean hasPermission(PermissionChecker permissionChecker, String entryClassName, long entryClassPK,
            String actionId) throws Exception {
        return ReferencePermission.contains(permissionChecker, entryClassPK, ActionKeys.VIEW);
    }

    @Override
    protected void doDelete(Reference reference) throws Exception {
        
        deleteDocument(reference.getCompanyId(), reference.getReferenceId());
    }

    @Override
    protected Document doGetDocument(Reference reference) throws Exception {

        Document document = getBaseModelDocument(CLASS_NAME, reference);
        document.addText(Field.CONTENT, reference.getBibTeX());
        document.addTextSortable("author", reference.getAuthor());
        document.addTextSortable(Field.TITLE, reference.getCitation());
        document.addTextSortable("year", reference.getYear());

        return document;

    }

    @Override
    protected Summary doGetSummary(Document document, Locale locale, String snippet, PortletRequest portletRequest,
            PortletResponse portletResponse) throws Exception {

        Summary summary = createSummary(document, Field.TITLE, Field.URL);

        return summary;
    }

    @Override
    protected void doReindex(String className, long classPK) throws Exception {
        Reference reference = _referenceLocalService.getReference(classPK);
        
        doReindex(reference);
    }

    @Override
    protected void doReindex(String[] ids) throws Exception {
        long companyId = GetterUtil.getLong(ids[0]);
        
        // TODO: what about the group?
        reindexReferences(companyId);
        // reindexReferences(companyId, groupId);

    }

    @Override
    protected void doReindex(Reference reference) throws Exception {
        
        Document document = getDocument(reference);
                
        IndexWriterHelperUtil.updateDocument(getSearchEngineId(), reference.getCompanyId(), document,
                isCommitImmediately());
    }

    protected void reindexReferences(long companyId) throws PortalException {
        
        final IndexableActionableDynamicQuery indexableActionableDynamicQuery = _referenceLocalService
                .getIndexableActionableDynamicQuery();

        indexableActionableDynamicQuery.setAddCriteriaMethod(new ActionableDynamicQuery.AddCriteriaMethod() {

            @Override
            public void addCriteria(DynamicQuery dynamicQuery) {

                Property statusProperty = PropertyFactoryUtil.forName("status");

                Integer[] statuses = { WorkflowConstants.STATUS_APPROVED, WorkflowConstants.STATUS_IN_TRASH };

                dynamicQuery.add(statusProperty.in(statuses));
            }

        });
        indexableActionableDynamicQuery.setCompanyId(companyId);
        // TODO: what about the group?
        // indexableActionableDynamicQuery.setGroupId(groupId);
        indexableActionableDynamicQuery
                .setPerformActionMethod(new ActionableDynamicQuery.PerformActionMethod<Reference>() {

                    @Override
                    public void performAction(Reference reference) {
                        try {
                            Document document = getDocument(reference);

                            indexableActionableDynamicQuery.addDocuments(document);
                        } catch (PortalException pe) {
                            if (_log.isWarnEnabled()) {
                                _log.warn("Unable to index reference " + reference.getReferenceId(), pe);
                            }
                        }
                    }

                });
        indexableActionableDynamicQuery.setSearchEngineId(getSearchEngineId());

        indexableActionableDynamicQuery.performActions();
    }

    @org.osgi.service.component.annotations.Reference(unbind = "-")
    protected void setReferenceLocalService(ReferenceLocalService referenceLocalService) {

        _referenceLocalService = referenceLocalService;
    }

    private static final Log _log = LogFactoryUtil.getLog(ReferenceIndexer.class);

    private ReferenceLocalService _referenceLocalService;

}