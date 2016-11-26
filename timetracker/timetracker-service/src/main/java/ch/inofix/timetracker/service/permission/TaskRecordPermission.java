package ch.inofix.timetracker.service.permission;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

import ch.inofix.timetracker.model.TaskRecord;
import ch.inofix.timetracker.service.TaskRecordLocalServiceUtil;

/**
 * 
 * @author Christian Berndt
 * @created 2016-11-13 17:55
 * @modified 2016-11-26 23:25
 * @version 1.0.1
 *
 */
public class TaskRecordPermission {

    public static void check(PermissionChecker permissionChecker, TaskRecord taskRecord, String actionId)
            throws PortalException {

        if (!contains(permissionChecker, taskRecord, actionId)) {
            throw new PrincipalException();
        }
    }

    public static void check(PermissionChecker permissionChecker, long taskRecordId, String actionId)
            throws PortalException {

        if (!contains(permissionChecker, taskRecordId, actionId)) {
            throw new PrincipalException();
        }
    }

    public static boolean contains(PermissionChecker permissionChecker, TaskRecord taskRecord, String actionId) {

        if (permissionChecker.hasOwnerPermission(taskRecord.getCompanyId(), TaskRecord.class.getName(),
                taskRecord.getTaskRecordId(), taskRecord.getUserId(), actionId)) {

            return true;
        }
        
        return true; 
        
        // TODO

//        return permissionChecker.hasPermission(taskRecord.getGroupId(), TaskRecord.class.getName(),
//                taskRecord.getTaskRecordId(), actionId);
    }

    public static boolean contains(PermissionChecker permissionChecker, long taskRecordId, String actionId)
            throws PortalException {

        TaskRecord taskRecord = TaskRecordLocalServiceUtil.getTaskRecord(taskRecordId);

        return contains(permissionChecker, taskRecord, actionId);
    }
}