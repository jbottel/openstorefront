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
package edu.usu.sdl.openstorefront.service.search;

import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.model.search.SearchElement;
import edu.usu.sdl.openstorefront.core.model.search.SearchOperation;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dshurtleff
 */
public class AttributeSetSearchHandler
		extends BaseSearchHandler
{
	private static final String ATTRIBUTE_CODE_SEPARATOR = ",";
	
	public AttributeSetSearchHandler(List<SearchElement> searchElements)
	{
		super(searchElements);
	}

	@Override
	protected ValidationResult internalValidate()
	{
		ValidationResult validationResult = new ValidationResult();

		for (SearchElement searchElement : searchElements) {
			if (StringUtils.isBlank(searchElement.getKeyField())) {
				validationResult.getRuleResults().add(getRuleResult("keyField", "Required"));
			}
			if (StringUtils.isBlank(searchElement.getKeyValue())) {
				validationResult.getRuleResults().add(getRuleResult("keyValue", "Required"));
			}			
		}

		return validationResult;
	}

	@Override
	public List<String> processSearch()
	{
		List<String> foundIds = new ArrayList<>();
		SearchOperation.MergeCondition mergeCondition = SearchOperation.MergeCondition.OR;
		for (SearchElement searchElement : searchElements) {
	
			List<ComponentAttribute> attributes = new ArrayList<>();
			if (StringUtils.isNotBlank(searchElement.getKeyValue())) {
				String attributeCodes[] = searchElement.getKeyValue().split(ATTRIBUTE_CODE_SEPARATOR);
				
				String query = "select from " + 
						ComponentAttribute.class.getSimpleName() +
						" where activeStatus = :activeStatusParam AND "
						+ "componentAttributePk.attributeType = :attributeTypeParam AND "
						+ "componentAttributePk.attributeCode IN :attributeCodesParam " ; 
				
				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("activeStatusParam", ComponentAttribute.ACTIVE_STATUS);
				paramMap.put("attributeTypeParam", searchElement.getKeyField());
				paramMap.put("attributeCodesParam", Arrays.asList(attributeCodes));								
				
				attributes = serviceProxy.getPersistenceService().query(query, paramMap);
			}
			
			List<String> results = new ArrayList<>();
			for (ComponentAttribute attribute : attributes) {
				results.add(attribute.getComponentId());
			}
			foundIds = mergeCondition.apply(foundIds, results);
			mergeCondition = searchElement.getMergeCondition();
		}
		return foundIds;
	}

}
