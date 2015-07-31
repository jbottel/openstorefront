/*
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.storage.model;

import edu.usu.sdl.openstorefront.doc.APIDescription;
import edu.usu.sdl.openstorefront.doc.ConsumeField;
import edu.usu.sdl.openstorefront.doc.ValidValueType;
import edu.usu.sdl.openstorefront.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.util.ReflectionUtil;
import edu.usu.sdl.openstorefront.util.SecurityUtil;
import edu.usu.sdl.openstorefront.util.TimeUtil;
import edu.usu.sdl.openstorefront.validation.Sanitize;
import edu.usu.sdl.openstorefront.validation.TextSanitizer;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author dshurtleff
 * @param <T>
 */
public abstract class StandardEntity<T>
		extends BaseEntity<T>
{

	public static final String ACTIVE_STATUS = "A";
	public static final String INACTIVE_STATUS = "I";
	public static final String PENDING_STATUS = "P";

	@Sanitize(TextSanitizer.class)
	@ConsumeField
	@ValidValueType(value = {}, lookupClass = SecurityMarkingType.class)
	@APIDescription("Security Classification")
	private String securityMarkingType;

	@NotNull
	@ValidValueType({"A", "I", "P"})
	private String activeStatus;

	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_USERNAME)
	private String createUser;

	@NotNull
	private Date createDts;

	@NotNull
	@Size(min = 1, max = OpenStorefrontConstant.FIELD_SIZE_USERNAME)
	private String updateUser;

	@NotNull
	private Date updateDts;

	private Boolean adminModified;

	public StandardEntity()
	{
	}

	@Override
	public int compareTo(T o)
	{
		if (o != null) {
			if (o instanceof StandardEntity) {
				return ReflectionUtil.compareObjects(getActiveStatus(), ((StandardEntity) o).getActiveStatus());
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public <T extends StandardEntity> void updateFields(T entity)
	{
		this.setSecurityMarkingType(entity.getSecurityMarkingType());
		if (entity.getActiveStatus() != null) {
			this.setActiveStatus(entity.getActiveStatus());
		}

		if (StringUtils.isNotBlank(entity.getUpdateUser())) {
			setUpdateUser(entity.getUpdateUser());
		}

		this.populateBaseUpdateFields();
	}

	public void populateBaseUpdateFields()
	{
		if (StringUtils.isBlank(getActiveStatus())) {
			setActiveStatus(ACTIVE_STATUS);
		}
		setUpdateDts(TimeUtil.currentDate());
		if (StringUtils.isBlank(getUpdateUser())) {
			setUpdateUser(SecurityUtil.getCurrentUserName());
		}
		setAdminModified(SecurityUtil.isAdminUser());
	}

	public void populateBaseCreateFields()
	{
		if (StringUtils.isBlank(getActiveStatus())) {
			setActiveStatus(ACTIVE_STATUS);
		}
		setCreateDts(TimeUtil.currentDate());
		setUpdateDts(TimeUtil.currentDate());

		if (StringUtils.isBlank(getCreateUser())) {
			setCreateUser(SecurityUtil.getCurrentUserName());
		}
		if (StringUtils.isBlank(getUpdateUser())) {
			setUpdateUser(SecurityUtil.getCurrentUserName());
		}
		setAdminModified(SecurityUtil.isAdminUser());
	}

	public String getActiveStatus()
	{
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus)
	{
		this.activeStatus = activeStatus;
	}

	public String getCreateUser()
	{
		return createUser;
	}

	public void setCreateUser(String createUser)
	{
		this.createUser = createUser;
	}

	public Date getCreateDts()
	{
		return createDts;
	}

	public void setCreateDts(Date createDts)
	{
		this.createDts = createDts;
	}

	public String getUpdateUser()
	{
		return updateUser;
	}

	public void setUpdateUser(String updateUser)
	{
		this.updateUser = updateUser;
	}

	public Date getUpdateDts()
	{
		return updateDts;
	}

	public void setUpdateDts(Date updateDts)
	{
		this.updateDts = updateDts;
	}

	public Boolean getAdminModified()
	{
		return adminModified;
	}

	public void setAdminModified(Boolean adminModified)
	{
		this.adminModified = adminModified;
	}

	public String getSecurityMarkingType()
	{
		return securityMarkingType;
	}

	public void setSecurityMarkingType(String securityMarkingType)
	{
		this.securityMarkingType = securityMarkingType;
	}

}
