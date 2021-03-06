package ch.inofix.portlet.contact.asset;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import ch.inofix.portlet.contact.model.Contact;
import ch.inofix.portlet.contact.service.permission.ContactPermission;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portlet.asset.model.BaseAssetRenderer;

/**
 * 
 * @author Christian Berndt
 * @created 2015-05-19 17:25
 * @modified 2015-07-30 15:01
 * @version 1.0.7
 *
 */
public class ContactAssetRenderer extends BaseAssetRenderer {

	private static Log log = LogFactoryUtil.getLog(ContactAssetRenderer.class
			.getName());

	private Contact contact;

	public ContactAssetRenderer(Contact contact) {

		this.contact = contact;
	}

	@Override
	public String getClassName() {
		return Contact.class.getName();
	}

	@Override
	public long getClassPK() {
		return contact.getContactId();
	}

	@Override
	public long getGroupId() {
		return contact.getGroupId();
	}

	@Override
	public String getSummary(Locale locale) {
		return contact.getFormattedName();
	}

	@Override
	public String getTitle(Locale locale) {
		return contact.getFullName(true);
	}

	@Override
	public PortletURL getURLEdit(LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse) throws Exception {

		PortletURL portletURL = liferayPortletResponse.createLiferayPortletURL(
				getControlPanelPlid(liferayPortletRequest),
				"contactmanager_WAR_contactmanager",
				PortletRequest.ACTION_PHASE);

		String backURL = (String) liferayPortletRequest.getAttribute("backURL");

		portletURL.setParameter("contactId",
				String.valueOf(contact.getContactId()));
		portletURL.setParameter("javax.portlet.action", "editContact");
		portletURL.setParameter("mvcPath", "/html/edit_contact.jsp");
		if (Validator.isNotNull(backURL)) {
			portletURL.setParameter("backURL", backURL);
		}

		return portletURL;

	}

	@Override
	public String getURLViewInContext(
			LiferayPortletRequest liferayPortletRequest,
			LiferayPortletResponse liferayPortletResponse,
			String noSuchEntryRedirect) {

		try {
			PortletURL portletURL = liferayPortletResponse
					.createActionURL("contactmanager_WAR_contactmanager");

			portletURL.setParameter("mvcPath", "/html/view_contact.jsp");
			portletURL.setParameter("contactId",
					String.valueOf(contact.getContactId()));
			portletURL.setParameter("javax.portlet.action", "viewContact");
			portletURL.setWindowState(WindowState.MAXIMIZED);

			return portletURL.toString();
			
		} catch (Exception e) {
		}

		return null;

	}

	@Override
	public long getUserId() {
		return contact.getUserId();
	}

	@Override
	public String getUserName() {
		return contact.getUserName();
	}

	@Override
	public String getUuid() {
		return contact.getUuid();
	}

	@Override
	public boolean hasEditPermission(PermissionChecker permissionChecker)
			throws PortalException, SystemException {

		return ContactPermission.contains(permissionChecker,
				contact.getContactId(), ActionKeys.UPDATE);
	}

	@Override
	public boolean hasViewPermission(PermissionChecker permissionChecker)
			throws PortalException, SystemException {

		return ContactPermission.contains(permissionChecker,
				contact.getContactId(), ActionKeys.VIEW);
	}

	@Override
	public boolean isPrintable() {
		return true;
	}

	@Override
	public String render(RenderRequest renderRequest,
			RenderResponse renderResponse, String template) throws Exception {

		if (template.equals(TEMPLATE_FULL_CONTENT)) {

			renderRequest.setAttribute("CONTACT", contact);

			return "/html/asset/" + template + ".jsp";

		} else {
			return null;
		}
	}

}
