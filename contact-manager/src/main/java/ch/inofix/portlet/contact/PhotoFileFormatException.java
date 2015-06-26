/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package ch.inofix.portlet.contact;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Brian Wing Shun Chan
 * @author Christian Berndt
 * @created 2015-06-26 11:59
 * @modified 2015-06-26 11:59
 * @version 1.0.0
 */
@SuppressWarnings("serial")
public class PhotoFileFormatException extends PortalException {

	public PhotoFileFormatException() {
		super();
	}

	public PhotoFileFormatException(String msg) {
		super(msg);
	}

	public PhotoFileFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public PhotoFileFormatException(Throwable cause) {
		super(cause);
	}

}