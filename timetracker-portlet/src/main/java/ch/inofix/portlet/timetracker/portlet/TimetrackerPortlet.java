
package ch.inofix.portlet.timetracker.portlet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import ch.inofix.portlet.timetracker.model.TaskRecord;
import ch.inofix.portlet.timetracker.service.TaskRecordLocalServiceUtil;
import ch.inofix.portlet.timetracker.service.TaskRecordServiceUtil;
import ch.inofix.portlet.timetracker.util.CommonFields;
import ch.inofix.portlet.timetracker.util.StringPool;
import ch.inofix.portlet.timetracker.util.TaskRecordFields;
import ch.inofix.portlet.timetracker.util.TimetrackerPortletKeys;
import ch.inofix.portlet.timetracker.util.TimetrackerPortletUtil;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.thoughtworks.xstream.XStream;

/**
 * View Controller of the timetracker portlet.
 *
 * @author Christian Berndt
 * @author Michael Lustenberger
 * @created 2013-10-07 10:47
 * @modified 2016-03-23 11:33
 * @version 1.1.0
 */
public class TimetrackerPortlet extends MVCPortlet {

    /**
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.8
     * @throws Exception
     */
    public void deleteAllTaskRecords(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws Exception {

        String tabs1 = ParamUtil.getString(actionRequest, "tabs1");

        ServiceContext serviceContext =
            ServiceContextFactory.getInstance(
                TaskRecord.class.getName(), actionRequest);

        // TODO: use remote service
        List<TaskRecord> taskRecords =
            TaskRecordLocalServiceUtil.getTaskRecords(serviceContext.getScopeGroupId());

        for (TaskRecord taskRecord : taskRecords) {

            // TODO: Add try-catch and count failed deletions
            taskRecord =
                TaskRecordServiceUtil.deleteTaskRecord(taskRecord.getTaskRecordId());

        }

        SessionMessages.add(
            actionRequest, "request_processed",
            PortletUtil.translate("successfully-deleted-x-task-records"));

        actionResponse.setRenderParameter("tabs1", tabs1);
    }

    /**
     * @param actionRequest
     * @param actionResponse
     * @since 1.0
     * @throws PortalException
     * @throws SystemException
     */
    public void deleteTaskRecord(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws PortalException, SystemException {

        // Get the parameters from the request.
        long taskRecordId =
            ParamUtil.getLong(actionRequest, TaskRecordFields.TASK_RECORD_ID);

        // Delete the requested taskRecord.
        TaskRecordServiceUtil.deleteTaskRecord(taskRecordId);

        // Report the successful deletion to the user.
        SessionMessages.add(
            actionRequest, TimetrackerPortletKeys.REQUEST_PROCESSED,
            PortletUtil.translate("successfully-deleted-the-task-record"));

        // Retrieve the parameters which have to be passed to the
        // render-phase and store them in the parameter map.
        String mvcPath =
            ParamUtil.getString(actionRequest, TimetrackerPortletKeys.MVC_PATH);

        HashMap<String, String[]> params = new HashMap<String, String[]>();

        params.put(TimetrackerPortletKeys.MVC_PATH, new String[] {
            mvcPath
        });
        params.put(TaskRecordFields.TASK_RECORD_ID, new String[] {
            String.valueOf(taskRecordId)
        });

        // Pass the relevant parameters to the render phase.
        actionResponse.setRenderParameters(params);
    }

    /**
     * @param actionRequest
     * @param actionResponse
     * @since 1.0.6
     * @throws Exception
     */
    protected void doDeleteTaskRecords(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws Exception {

        long[] taskRecordIds = ParamUtil.getLongValues(actionRequest, "rowIds");

        if (taskRecordIds.length > 0) {

            int i = 0;
            for (long taskRecordId : taskRecordIds) {

                try {
                    TaskRecordServiceUtil.deleteTaskRecord(taskRecordId);
                    i++;
                }
                catch (Exception e) {
                    _log.error(e);
                }
            }

            SessionMessages.add(
                actionRequest, "request_processed", PortletUtil.translate(
                    "successfully-deleted-x-task-records", String.valueOf(i)));

        }
        else {
            SessionMessages.add(
                actionRequest, "request_processed",
                PortletUtil.translate("no-task-record-selected"));
        }
    }

    /**
     * @param actionRequest
     * @param actionResponse
     * @throws Exception
     * @since 1.1.9
     */
    public void editSet(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws Exception {

        String cmd = ParamUtil.getString(actionRequest, "cmd");

        if ("delete".equals(cmd)) {
            doDeleteTaskRecords(actionRequest, actionResponse);
        }

    }

    public void editTaskRecord(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws Exception {

        String backURL = ParamUtil.getString(actionRequest, "backURL");
        long taskRecordId = ParamUtil.getLong(actionRequest, "taskRecordId");
        String mvcPath = ParamUtil.getString(actionRequest, "mvcPath");
        String redirect = ParamUtil.getString(actionRequest, "redirect");
        String windowId = ParamUtil.getString(actionRequest, "windowId");

        TaskRecord taskRecord = null;

        if (taskRecordId > 0) {
            taskRecord = TaskRecordServiceUtil.getTaskRecord(taskRecordId);
        }
        else {
            taskRecord = TaskRecordServiceUtil.createTaskRecord();
        }

        actionRequest.setAttribute(
            TimetrackerPortletKeys.TASK_RECORD, taskRecord);

        actionResponse.setRenderParameter("backURL", backURL);
        actionResponse.setRenderParameter(
            "taskRecordId", String.valueOf(taskRecordId));
        actionResponse.setRenderParameter("mvcPath", mvcPath);
        actionResponse.setRenderParameter("redirect", redirect);
        actionResponse.setRenderParameter("windowId", windowId);

    }

    protected void exportCSV(
        ResourceRequest resourceRequest, ResourceResponse resourceResponse)
        throws PortalException, SystemException, IOException {

        // TODO: Move this to an exportTaskRecords() method in
        // TaskRecordServiceImpl and check for the export permission
        List<TaskRecord> taskRecords =
            TaskRecordLocalServiceUtil.getTaskRecords(0, Integer.MAX_VALUE);

        StringBuffer sb = new StringBuffer();
        for (TaskRecord taskRecord : taskRecords) {

            sb.append(toCSV(taskRecord));
            sb.append("\n");
        }

        String export = sb.toString();

        PortletResponseUtil.sendFile(
            resourceRequest, resourceResponse, "TaskRecords.csv",
            export.getBytes(), ContentTypes.TEXT_PLAIN_UTF8);

    }

    /**
     * Export the taskRecords into a csv file.
     *
     * @param resourceRequest
     * @param resourceResponse
     * @since 1.0
     */
    protected void exportTaskRecords(
        ResourceRequest resourceRequest, ResourceResponse resourceResponse) {

        String format = ParamUtil.getString(resourceRequest, "format");

        try {

            String output = null;

            if ("xml".equals(format)) {

                resourceResponse.setContentType(ContentTypes.TEXT_XML);
                resourceResponse.addProperty(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"TaskRecords.xml\"");

                output =
                    getTaskRecords(resourceRequest, resourceResponse, format);

            }
            else if ("latex".equals(format) || "fulllatex".equals(format)) {

                resourceResponse.setContentType(ContentTypes.TEXT);
                resourceResponse.addProperty(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"TaskRecords.tex\"");

                output =
                    getTaskRecords(resourceRequest, resourceResponse, format);

            }
            else {

                // By default, export in csv format.
                resourceResponse.setContentType(ContentTypes.TEXT_CSV);
                resourceResponse.addProperty(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"TaskRecords.csv\"");

                output =
                    getTaskRecords(resourceRequest, resourceResponse, null);
            }

            PrintWriter writer = resourceResponse.getWriter();
            writer.print(output);

        }
        catch (Exception e) {
            _log.error(e);
        }
    }

    /**
     * Disable the get- / sendRedirect feature of LiferayPortlet.
     */
    @Override
    protected String getRedirect(
        ActionRequest actionRequest, ActionResponse actionResponse) {

        return null;
    }

    /**
     * Return a taskRecord in CSV format.
     *
     * @param taskRecord
     * @return a taskRecord in CSV format.
     * @since 1.0
     */
    // After the model of getUserCSV from ExportUsersAction.
    protected String getTaskRecordCSV(TaskRecord taskRecord) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        StringBundler sb = new StringBundler();

        sb.append(taskRecord.getTaskRecordId());
        sb.append(",");
        sb.append(sdf.format(taskRecord.getStartDate()));
        sb.append(",");
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getWorkPackage());
        sb.append(StringPool.QUOTE);
        sb.append(",");
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getDescription());
        sb.append(StringPool.QUOTE);
        sb.append(",");
        sb.append(taskRecord.getDurationInMinutes());
        sb.append(",");
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getUserName());
        sb.append(StringPool.QUOTE);

        sb.append(StringPool.NEW_LINE);

        return sb.toString();
    }

    /**
     * Return a taskRecord as a LaTeX table row in plain style.
     *
     * @param taskRecord
     * @return a taskRecord as a LaTeX table row.
     * @since 1.1
     */
    // After the model of getUserCSV from ExportUsersAction.
    protected String getTaskRecordLaTeX(TaskRecord taskRecord) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        StringBundler sb = new StringBundler();

        // Retrieve the last token of the workpackage
        String[] tokens = taskRecord.getWorkPackage().split("\\.");
        _log.debug("tokens.length = " + tokens.length);

        String token = tokens[tokens.length - 1];

        sb.append(taskRecord.getUserName());
        sb.append(" & ");
        sb.append(token);
        sb.append(" & ");
        sb.append(text2tex(taskRecord.getDescription()));
        sb.append(" & ");
        sb.append(sdf.format(taskRecord.getStartDate()));
        sb.append(" & ");
        sb.append(taskRecord.getDurationInHours());

        sb.append("\\\\");
        sb.append(StringPool.NEW_LINE);

        return sb.toString();
    }

    /**
     * Return a taskRecord as a LaTeX table row in report style.
     *
     * @param taskRecord
     * @param userNames
     * @param workTokens
     * @return a taskRecord as a LaTeX report table row.
     * @since 1.2
     */
    // After the model of getUserCSV from ExportUsersAction.
    protected String getTaskRecordFullLaTeX(
        TaskRecord taskRecord, Map<String, String> userNames,
        Map<String, String> workTokens) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("0.00");

        StringBundler sb = new StringBundler();

        // Retrieve the last token of the workpackage
        String[] tokens = taskRecord.getWorkPackage().split("\\.");
        _log.debug("tokens.length = " + tokens.length);

        String token = tokens[tokens.length - 1];

        sb.append(userNames.get(taskRecord.getUserName()));
        sb.append(" & \\textbf{");
        sb.append(workTokens.get(token));
        sb.append(":} ");
        sb.append(text2tex(taskRecord.getDescription()));
        sb.append(" & ");
        sb.append(sdf.format(taskRecord.getStartDate()));
        sb.append(" & ");
        sb.append(df.format(taskRecord.getDurationInHours()));

        sb.append("\\\\");
        sb.append(StringPool.NEW_LINE);

        return sb.toString();
    }

    /**
     * Return a taskRecord in xml format.
     *
     * @param taskRecord
     * @return a taskRecord in xml format.
     * @since 1.1
     */
    // After the model of getUserCSV from ExportUsersAction.
    protected String getTaskRecordXML(TaskRecord taskRecord) {

        _log.info("Executing getTaskRecordXML().");

        XStream xs = new XStream();

        // OBJECT --> XML
        String xml = xs.toXML(taskRecord);

        return xml;
    }

    /**
     * Return the list of taskRecords in the selected format.
     *
     * @param resourceRequest
     * @param resourceResponse
     * @return the list of taskRecords in CSV format.
     * @throws Exception
     */
    // After the model of getUsersCSV from ExportUsersAction.
    protected String getTaskRecords(
        ResourceRequest resourceRequest, ResourceResponse resourceResponse,
        String format)
        throws Exception {

        DecimalFormat df = new DecimalFormat("0.00");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        long companyId =
            ParamUtil.getLong(resourceRequest, CommonFields.COMPANY_ID);
        String description =
            ParamUtil.getString(resourceRequest, TaskRecordFields.DESCRIPTION);
        long groupId =
            ParamUtil.getLong(resourceRequest, CommonFields.GROUP_ID);
        String orderByCol =
            ParamUtil.getString(
                resourceRequest, TimetrackerPortletKeys.ORDER_BY_COL);
        String orderByType =
            ParamUtil.getString(
                resourceRequest, TimetrackerPortletKeys.ORDER_BY_TYPE);
        long userId =
            ParamUtil.getLong(resourceRequest, TaskRecordFields.USER_ID);
        String workPackage =
            ParamUtil.getString(resourceRequest, TaskRecordFields.WORK_PACKAGE);

        Date startDate = null;
        Date endDate = null;

        int startDateDay =
            ParamUtil.getInteger(resourceRequest, "startDateDay");
        int startDateMonth =
            ParamUtil.getInteger(resourceRequest, "startDateMonth");
        int startDateYear =
            ParamUtil.getInteger(resourceRequest, "startDateYear");

        int endDateDay = ParamUtil.getInteger(resourceRequest, "endDateDay");
        int endDateMonth =
            ParamUtil.getInteger(resourceRequest, "endDateMonth");
        int endDateYear = ParamUtil.getInteger(resourceRequest, "endDateYear");

        startDate =
            PortalUtil.getDate(startDateMonth, startDateDay, startDateYear);
        endDate = PortalUtil.getDate(endDateMonth, endDateDay, endDateYear);

        // if (Validator.isNotNull(ParamUtil.getString(resourceRequest,
        // TaskRecordFields.START_DATE))) {
        // startDate = ParamUtil.getDate(resourceRequest,
        // TaskRecordFields.START_DATE, sdf);
        // }
        //
        // if (Validator.isNotNull(ParamUtil.getString(resourceRequest,
        // TaskRecordFields.END_DATE))) {
        // endDate = ParamUtil.getDate(resourceRequest,
        // TaskRecordFields.END_DATE, sdf);
        // }

        int status = WorkflowConstants.STATUS_ANY;
        boolean andOperator = true;

        _log.debug(CommonFields.COMPANY_ID + " = " + companyId);
        _log.debug(TaskRecordFields.DESCRIPTION + " = " + description);
        _log.debug(CommonFields.GROUP_ID + " = " + groupId);
        _log.debug(TimetrackerPortletKeys.ORDER_BY_COL + " = " + orderByCol);
        _log.debug(TimetrackerPortletKeys.ORDER_BY_TYPE + " = " + orderByType);
        _log.debug(TaskRecordFields.USER_ID + " = " + userId);
        _log.debug(TaskRecordFields.WORK_PACKAGE + " = " + workPackage);

        int total = 0;
        // int total = TaskRecordLocalServiceUtil.searchCount(companyId,
        // groupId,
        // userId, workPackage, description, startDate, endDate, status,
        // andOperator);

        OrderByComparator obc =
            TimetrackerPortletUtil.getOrderByComparator(orderByCol, orderByType);

        List<TaskRecord> taskRecords = new ArrayList<TaskRecord>();

        StringBundler sb = new StringBundler(taskRecords.size() * 4);

        if ("xml".equals(format)) {

            sb.append("<taskRecords>");
            sb.append(StringPool.NEW_LINE);

        }
        else if ("latex".equals(format)) {

            // TODO: Process the number of columns dynamically
            sb.append("\\begin{center}");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\begin{tabular}{ | l | l | p{7cm} | l | l |}");
            sb.append(StringPool.NEW_LINE);

        }
        else if ("fulllatex".equals(format)) {

            sb.append("\\begin{supertabular}{p{0.6cm}|p{12cm}|p{1.8cm}|R|}");
            sb.append(StringPool.NEW_LINE);
            // TODO i18n!!
            sb.append("MA & Beschreibung & Zeitraum & Menge \\\\");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\hline");
            sb.append(StringPool.NEW_LINE);
        }

        Iterator<TaskRecord> itr = taskRecords.iterator();

        // meta data cache (only for 'fulllatex'..)
        Map<String, String> userNames = Collections.emptyMap();
        Map<String, String> workTokens = Collections.emptyMap();

        if ("fulllatex".equals(format)) {

            userNames = new HashMap<String, String>();
            workTokens = new HashMap<String, String>();

            for (int i = 0; itr.hasNext(); i++) {

                TaskRecord taskRecord = itr.next();

                String userName = taskRecord.getUserName();
                String[] theNames = userName.split(" ");
                String initials = "";

                for (int j = 0; j < theNames.length; j++) {

                    initials += theNames[j].charAt(0);
                    initials = initials.toLowerCase();
                }

                if (!userNames.containsKey(userName)) {

                    if (userNames.containsValue(initials)) {

                        int k = 1;
                        while (userNames.containsValue(initials + k)) {

                            k++;
                        }
                        initials += k;
                    }
                    userNames.put(userName, initials);
                }

                // Retrieve the last token of the workpackage
                String[] tokens = taskRecord.getWorkPackage().split("\\.");
                String token = tokens[tokens.length - 1];

                // char c = Character.toUpperCase(token.charAt(0));
                // token = c + token.substring(1);

                // TODO do check whether the short form is unique in the map
                String tokenShort = token.substring(0, 2);
                workTokens.put(token, tokenShort);
            }

            // now we need a new one
            itr = taskRecords.iterator();
        }

        for (int i = 0; itr.hasNext(); i++) {

            TaskRecord taskRecord = itr.next();

            if ("latex".equals(format)) {
                sb.append(getTaskRecordLaTeX(taskRecord));

            }
            else if ("fulllatex".equals(format)) {
                sb.append(getTaskRecordFullLaTeX(
                    taskRecord, userNames, workTokens));

            }
            else if ("xml".equals(format)) {
                sb.append(getTaskRecordXML(taskRecord));

            }
            else {
                sb.append(getTaskRecordCSV(taskRecord));
            }

        }

        if ("xml".equals(format)) {
            sb.append("</taskRecords>");

        }
        else if ("latex".equals(format)) {
            sb.append("\\hline");
            sb.append(StringPool.NEW_LINE);
            // TODO i18n!!
            sb.append(" & Periodentotal & & & ");
            sb.append(TimetrackerPortletUtil.getHours(taskRecords));
            sb.append("\\\\");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\end{tabular}");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\end{center}");

        }
        else if ("fulllatex".equals(format)) {

            sb.append("\\hline");
            sb.append(StringPool.NEW_LINE);
            // TODO i18n!!
            sb.append(" & Periodentotal & & ");
            double totalHours = TimetrackerPortletUtil.getHours(taskRecords);
            sb.append(df.format(totalHours));
            sb.append("\\\\");
            sb.append(StringPool.NEW_LINE);
            double carryOver =
                ParamUtil.getDouble(resourceRequest, "carryOver");
            _log.debug("carryOver = " + carryOver);
            // TODO i18n!!
            sb.append(" & Übertrag Vorperiode (sep. Abrg) & & ");
            sb.append(df.format(carryOver));
            sb.append("\\\\");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\hline");
            sb.append(StringPool.NEW_LINE);
            // TODO i18n!!
            sb.append(" & Total & & ");
            sb.append(df.format(totalHours + carryOver));
            sb.append("\\\\"); // TODO calc
            sb.append(StringPool.NEW_LINE);
            sb.append("\\hline");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\hline");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\end{supertabular}");
            sb.append(StringPool.NEW_LINE);

            // now the co-workers list
            sb.append("\\begin{minipage}[t]{5cm}");
            sb.append(StringPool.NEW_LINE);
            sb.append("{\\scriptsize");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\textbf{Mitarbeitende (MA):}");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\begin{deflist}[aaaa]");
            sb.append(StringPool.NEW_LINE);
            Iterator it = userNames.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry entry = (Map.Entry) it.next();
                sb.append(" \\item[");
                sb.append(entry.getValue());
                sb.append("] ");
                sb.append(entry.getKey());
                sb.append(StringPool.NEW_LINE);
            }
            sb.append("\\end{deflist}");
            sb.append(StringPool.NEW_LINE);
            sb.append("}");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\end{minipage}");
            sb.append(StringPool.NEW_LINE);

            // and finally the def list for the tasks
            sb.append("\\begin{minipage}[t]{5cm}");
            sb.append(StringPool.NEW_LINE);
            sb.append("{\\scriptsize");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\textbf{Kategorien:}");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\begin{deflist}[aaa]");
            sb.append(StringPool.NEW_LINE);
            it = workTokens.entrySet().iterator();
            while (it.hasNext()) {

                Map.Entry<String, String> entry = (Map.Entry) it.next();
                sb.append(" \\item[");
                sb.append(entry.getValue());
                sb.append("] ");
                String s = (String) entry.getKey();
                char c = Character.toUpperCase(s.charAt(0));
                sb.append(c + s.substring(1));
                sb.append(StringPool.NEW_LINE);
            }
            sb.append("\\end{deflist}");
            sb.append(StringPool.NEW_LINE);
            sb.append("}");
            sb.append(StringPool.NEW_LINE);
            sb.append("\\end{minipage}");
            sb.append(StringPool.NEW_LINE);
        }

        return sb.toString();
    }

    public void importCSVFile(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws Exception {

        UploadPortletRequest uploadPortletRequest =
            PortalUtil.getUploadPortletRequest(actionRequest);

        HttpServletRequest request =
            PortalUtil.getHttpServletRequest(actionRequest);

        ThemeDisplay themeDisplay =
            (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        ServiceContext serviceContext =
            ServiceContextFactory.getInstance(
                TaskRecord.class.getName(), actionRequest);

        File file = uploadPortletRequest.getFile("file");

        long userId = themeDisplay.getUserId();
        long groupId = themeDisplay.getScopeGroupId();

        if (Validator.isNotNull(file)) {

            Reader in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
            for (CSVRecord record : records) {

                String taskRecordId = record.get(0);
                // String startDate = record.get("startDate");
                String description = record.get(3);
                String ticketURL = "";
                // String ticketURL = record.get("ticketURL");
                String workPackage = record.get(2);
                long duration = GetterUtil.getLong(record.get(4));

                // TODO: add to export
                Date endDate = new Date();
                Date startDate = new Date();

                int endDateDay = endDate.getDay();
                int endDateMonth = endDate.getMonth();
                int endDateYear = endDate.getYear();
                int endDateHour = endDate.getHours();
                int endDateMinute = endDate.getMinutes();

                int startDateDay = startDate.getDay();
                int startDateMonth = startDate.getMonth();
                int startDateYear = startDate.getYear();
                int startDateHour = startDate.getHours();
                int startDateMinute = startDate.getMinutes();

                TaskRecord taskRecord =
                    TaskRecordServiceUtil.addTaskRecord(
                        userId, groupId, workPackage, description, ticketURL,
                        endDateDay, endDateMonth, endDateYear, endDateHour,
                        endDateMinute, startDateDay, startDateMonth,
                        startDateYear, startDateHour, startDateMinute,
                        duration, serviceContext);
            }

            String message = PortletUtil.translate("there-are-no-results");

            // ServiceContext serviceContext =
            // ServiceContextFactory.getInstance(
            // TaskRecord.class.getName(), uploadPortletRequest);
            //
            // if (vCards.size() > 0) {
            //
            // message = PortletUtil.importVcards(vCards, serviceContext);
            // }

            SessionMessages.add(actionRequest, "request_processed", message);

        }
        else {

            SessionErrors.add(
                actionRequest, PortletUtil.translate("file-not-found"));

        }

    }

    /**
     * Import a list of task records.
     *
     * @param actionRequest
     * @param actionResponse
     * @since 1.3
     * @throws PortalException
     * @throws SystemException
     */
    public void importVimTaskRecords(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws PortalException, SystemException {

        // Create a service context for this request.
        ServiceContext serviceContext =
            ServiceContextFactory.getInstance(
                TaskRecord.class.getName(), actionRequest);

        // Retrieve the user- and groupIds.
        ThemeDisplay themeDisplay =
            (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

        long userId = themeDisplay.getUserId();

        int rowsCount =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                "_rows");
        int colsCount =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                "_cols");

        for (int i = 0; i < rowsCount; i++) {

            // Get the parameters from the request.
            String workPackage =
                ParamUtil.getString(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_10");
            String description =
                ParamUtil.getString(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_11");
            String ticketURL =
                ParamUtil.getString(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_12");

            int startDateDay =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_2");
            int startDateMonth =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_1");
            // We import the month as number, e.g. Jan = 01
            startDateMonth--;
            int startDateYear =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_0");
            int endDateDay =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_7");
            int endDateMonth =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_6");
            // We import the month as number, e.g. Jan = 01
            endDateMonth--;
            int endDateYear =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_5");

            int startDateHour =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_3");
            int startDateMinute =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_4");
            int endDateHour =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_8");
            int endDateMinute =
                ParamUtil.getInteger(actionRequest, TaskRecordFields.VIM_TEXT +
                    "_" + i + "_9");
            int durationInMinutes = 0;
            long duration = 0;

            if (endDateHour < 0) {

                startDateHour = 0;
                startDateMinute = 0;
                endDateHour = 0;
                endDateMinute = 0;
                durationInMinutes =
                    ParamUtil.getInteger(
                        actionRequest, TaskRecordFields.VIM_TEXT + "_" + i +
                            "_9");
                duration = durationInMinutes * 60 * 1000;
            }

            TaskRecordLocalServiceUtil.addTaskRecord(
                userId, duration, workPackage, description, ticketURL,
                endDateDay, endDateMonth, endDateYear, endDateHour,
                endDateMinute, startDateDay, startDateMonth, startDateYear,
                startDateHour, startDateMinute, duration, serviceContext);
        }

        // Report the successful update back to the user.
        SessionMessages.add(
            actionRequest, TimetrackerPortletKeys.REQUEST_PROCESSED,
            PortletUtil.translate("successfully-imported-the-task-records"));

        // Retrieve the parameters which have to be passed to the
        // render-phase and store them in the parameter map.
        String mvcPath =
            ParamUtil.getString(actionRequest, TimetrackerPortletKeys.MVC_PATH);

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put(TimetrackerPortletKeys.MVC_PATH, new String[] {
            mvcPath
        });

        // Pass the relevant parameters to the render phase.
        actionResponse.setRenderParameters(params);
    }

    public void saveTaskRecord(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws Exception {

        HttpServletRequest request =
            PortalUtil.getHttpServletRequest(actionRequest);

        ThemeDisplay themeDisplay =
            (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        long userId = themeDisplay.getUserId();
        long groupId = themeDisplay.getScopeGroupId();

        String backURL = ParamUtil.getString(actionRequest, "backURL");
        long taskRecordId = ParamUtil.getLong(actionRequest, "taskRecordId");
        String historyKey = ParamUtil.getString(actionRequest, "historyKey");
        String mvcPath = ParamUtil.getString(actionRequest, "mvcPath");
        String redirect = ParamUtil.getString(actionRequest, "redirect");
        String windowId = ParamUtil.getString(actionRequest, "windowId");

        TaskRecord taskRecord = null;

        // Get the taskRecord's attributes from them request (alphabetical)
        String description =
            ParamUtil.getString(actionRequest, TaskRecordFields.DESCRIPTION);
        long duration =
            ParamUtil.getLong(actionRequest, TaskRecordFields.DURATION);

        int startDateDay =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.DAY);
        int startDateMonth =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.MONTH);
        int startDateYear =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.YEAR);
        int startDateHour =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.HOUR);
        int startDateMinute =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.MINUTE);

        // Create the endDate with the date values of
        // the startDate, because we want the user to
        // have to select only one date.
        int endDateDay =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.DAY);
        int endDateMonth =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.MONTH);
        int endDateYear =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.START_DATE +
                StringPool.YEAR);
        int endDateHour =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.END_DATE +
                StringPool.HOUR);
        int endDateMinute =
            ParamUtil.getInteger(actionRequest, TaskRecordFields.END_DATE +
                StringPool.MINUTE);

        String ticketURL =
            ParamUtil.getString(actionRequest, TaskRecordFields.TICKET_URL);

        String workPackage =
            ParamUtil.getString(actionRequest, TaskRecordFields.WORK_PACKAGE);

        // Pass the required parameters to the render phase

        actionResponse.setRenderParameter(
            "taskRecordId", String.valueOf(taskRecordId));
        actionResponse.setRenderParameter("backURL", backURL);
        actionResponse.setRenderParameter("historyKey", historyKey);
        actionResponse.setRenderParameter("mvcPath", mvcPath);
        actionResponse.setRenderParameter("redirect", redirect);
        actionResponse.setRenderParameter("windowId", windowId);

        // Save the taskRecord

        ServiceContext serviceContext =
            ServiceContextFactory.getInstance(
                TaskRecord.class.getName(), actionRequest);

        if (taskRecordId > 0) {
            taskRecord =
                TaskRecordServiceUtil.updateTaskRecord(
                    userId, groupId, taskRecordId, workPackage, description,
                    ticketURL, endDateDay, endDateMonth, endDateYear,
                    endDateHour, endDateMinute, startDateDay, startDateMonth,
                    startDateYear, startDateHour, startDateMinute, duration,
                    serviceContext);

            SessionMessages.add(
                actionRequest, "request_processed",
                PortletUtil.translate("successfully-updated-the-task-record"));
        }
        else {

            taskRecord =
                TaskRecordServiceUtil.addTaskRecord(
                    userId, groupId, workPackage, description, ticketURL,
                    endDateDay, endDateMonth, endDateYear, endDateHour,
                    endDateMinute, startDateDay, startDateMonth, startDateYear,
                    startDateHour, startDateMinute, duration, serviceContext);

            SessionMessages.add(
                actionRequest, "request_processed",
                PortletUtil.translate("successfully-added-the-task-record"));
        }

        // Store the updated or added taskRecord as a request attribute
        actionRequest.setAttribute(
            TimetrackerPortletKeys.TASK_RECORD, taskRecord);

    }

    /**
     * (Just) parse a text for Task Records - the text must follow some simple
     * rules (see util/TimetrackerPortlet)
     *
     * @param actionRequest
     * @param actionResponse
     * @since 1.3
     * @throws PortalException
     * @throws SystemException
     */
    public void parseVimTaskRecords(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws PortalException, SystemException {

        _log.info("Executing parseVimTaskRecords().");

        // Create a service context for this request.
        ServiceContext serviceContext =
            ServiceContextFactory.getInstance(
                TaskRecord.class.getName(), actionRequest);

        // Retrieve the user- and groupIds.
        ThemeDisplay themeDisplay =
            (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

        long userId = themeDisplay.getUserId();

        // Get the parameters from the request.
        String vimText =
            ParamUtil.getString(actionRequest, TaskRecordFields.VIM_TEXT);

        List<String[]> taskRecords =
            TimetrackerPortletUtil.parseMicVimport(vimText);

        // Retrieve the parameters which have to be passed to the
        // render-phase and store them in the parameter map.
        String mvcPath =
            ParamUtil.getString(actionRequest, TimetrackerPortletKeys.MVC_PATH);

        actionRequest.setAttribute(TaskRecordFields.VIM_TEXT, vimText);
        actionRequest.setAttribute(TaskRecordFields.VIM_LIST, taskRecords);

        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put(TimetrackerPortletKeys.MVC_PATH, new String[] {
            mvcPath
        });

        // Pass the relevant parameters to the render phase.
        actionResponse.setRenderParameters(params);
    }

    /**
     * Retrieve the requested resources and display the portlet.
     *
     * @param resourceRequest
     * @param resourceResponse
     * @throws PortalException
     * @throws SystemException
     * @throws IOException
     * @throws PortletException
     * @since 1.0
     */
    protected void servePortlet(
        ResourceRequest resourceRequest, ResourceResponse resourceResponse)
        throws PortalException, SystemException, PortletException, IOException {

        // Get the parameters from the request
        long taskRecordId =
            ParamUtil.getLong(resourceRequest, TaskRecordFields.TASK_RECORD_ID);
        String mvcPath =
            ParamUtil.getString(
                resourceRequest, TimetrackerPortletKeys.MVC_PATH);
        String historyKey =
            ParamUtil.getString(
                resourceRequest, TimetrackerPortletKeys.HISTORY_KEY);

        // Load the requested project
        TaskRecord taskRecord = null;

        try {
            taskRecord = TaskRecordLocalServiceUtil.getTaskRecord(taskRecordId);
        }
        catch (SystemException ignore) {
            _log.debug(ignore);
        }
        catch (PortalException ignore) {
            _log.debug(ignore);
        }

        // Store the project as resourceResponse attribute
        resourceRequest.setAttribute(
            TimetrackerPortletKeys.TASK_RECORD, taskRecord);

        PortletConfig portletConfig = getPortletConfig();

        PortletContext portletContext = portletConfig.getPortletContext();

        // Dispatch the request to the requested jsp-page
        PortletRequestDispatcher portletRequestDispatcher =
            portletContext.getRequestDispatcher(mvcPath);

        portletRequestDispatcher.include(resourceRequest, resourceResponse);

    }

    /**
     * Override the default serveResource method. Forward resource requests to
     * the appropriate method.
     *
     * @param resourceRequest
     *            the ResourceRequest
     * @param resourceResponse
     *            the ResourceResponse
     * @throws IOException
     * @throws PortletException
     * @since 1.0
     */
    public void serveResource(
        ResourceRequest resourceRequest, ResourceResponse resourceResponse)
        throws PortletException, IOException {

        try {

            String resourceID = resourceRequest.getResourceID();

            if ("exportCSV".equals(resourceID)) {
                exportCSV(resourceRequest, resourceResponse);
            }
            else if ("exportTaskRecords".equals(resourceID)) {
                exportTaskRecords(resourceRequest, resourceResponse);
            }
            else {
                // By default serve the portlet body.
                servePortlet(resourceRequest, resourceResponse);
            }

        }
        catch (Exception e) {

            _log.error(e);
            throw new PortletException(e);
        }
    }

    private String text2tex(String tex) {

        // TODO make this configurable - or at least collect it in a file
        tex = tex.replaceAll("\\\\", ""); // this is simply forbidden!!
        tex = tex.replaceAll("_", "\\\\_");
        tex = tex.replaceAll("&", "\\\\&");
        tex = tex.replaceAll("%", "\\\\%");
        tex = tex.replaceAll("\"", "''");

        return tex;
    }

    private String toCSV(TaskRecord taskRecord) {

        String COMMA = ",";

        StringBuilder sb = new StringBuilder();

        sb.append(taskRecord.getCompanyId());
        sb.append(COMMA);
        sb.append(taskRecord.getCreateDate().getTime());
        sb.append(COMMA);
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getDescription());
        sb.append(StringPool.QUOTE);
        sb.append(COMMA);
        sb.append(taskRecord.getDuration());
        sb.append(COMMA);
        sb.append(taskRecord.getEndDate().getTime());
        sb.append(COMMA);
        sb.append(taskRecord.getGroupId());
        sb.append(COMMA);
        sb.append(taskRecord.getStartDate().getTime());
        sb.append(COMMA);
        sb.append(taskRecord.getStatus());
        sb.append(COMMA);
        sb.append(taskRecord.getTaskRecordId());
        sb.append(COMMA);
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getTicketURL());
        sb.append(StringPool.QUOTE);
        sb.append(COMMA);
        sb.append(taskRecord.getUserId());
        sb.append(COMMA);
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getUserName());
        sb.append(StringPool.QUOTE);
        sb.append(COMMA);
        sb.append(StringPool.QUOTE);
        sb.append(taskRecord.getWorkPackage());
        sb.append(StringPool.QUOTE);

        return sb.toString();

    }

    /**
     * @param actionRequest
     * @param actionResponse
     * @since 1.0
     * @throws PortalException
     * @throws SystemException
     */
    public void viewTaskRecord(
        ActionRequest actionRequest, ActionResponse actionResponse)
        throws PortalException, SystemException {

        // Get the parameters from the request.
        long taskRecordId =
            ParamUtil.getLong(actionRequest, TaskRecordFields.TASK_RECORD_ID);

        // Retrieve the requested taskRecord and store it
        // as a request attribute.
        // TODO: Use the remote service instead.
        TaskRecord taskRecord =
            TaskRecordLocalServiceUtil.getTaskRecord(taskRecordId);

        actionRequest.setAttribute(
            TimetrackerPortletKeys.TASK_RECORD, taskRecord);

        // Retrieve the parameters which have to be passed to the
        // render-phase and store them in the parameter map.
        String mvcPath =
            ParamUtil.getString(actionRequest, TimetrackerPortletKeys.MVC_PATH);

        HashMap<String, String[]> params = new HashMap<String, String[]>();

        params.put(TimetrackerPortletKeys.MVC_PATH, new String[] {
            mvcPath
        });
        params.put(TaskRecordFields.TASK_RECORD_ID, new String[] {
            String.valueOf(taskRecordId)
        });

        // Pass the relevant parameters to the render phase.
        actionResponse.setRenderParameters(params);
    }

    // Enable logging for this class.
    private static final Log _log =
        LogFactoryUtil.getLog(TimetrackerPortlet.class.getName());
}
