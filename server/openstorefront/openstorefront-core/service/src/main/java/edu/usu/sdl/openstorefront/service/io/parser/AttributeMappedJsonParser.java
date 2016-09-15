/*
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.service.io.parser;

import edu.usu.sdl.openstorefront.core.model.AttributeAll;
import edu.usu.sdl.openstorefront.core.spi.parser.BaseAttributeParser;
import edu.usu.sdl.openstorefront.core.spi.parser.mapper.AttributeMapper;
import edu.usu.sdl.openstorefront.core.spi.parser.mapper.MapModel;
import edu.usu.sdl.openstorefront.core.spi.parser.reader.GenericReader;
import edu.usu.sdl.openstorefront.core.spi.parser.reader.JSONMapReader;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class AttributeMappedJsonParser
		extends BaseAttributeParser
{

	private List<AttributeAll> attributeAlls;

	@Override
	public String checkFormat(String mimeType, InputStream input)
	{
		if (mimeType.contains("json")) {
			return "";
		} else {
			return "Invalid format. Please upload a json file.";
		}
	}

	@Override
	protected GenericReader getReader(InputStream in)
	{
		return new JSONMapReader(in);
	}

	@Override
	protected String handlePreviewOfRecord(Object data)
	{
		return super.handlePreviewOfRecord(attributeAlls);
	}

	@Override
	protected <T> Object parseRecord(T record)
	{
		MapModel mapModel = (MapModel) record;

		AttributeMapper attributeMapper = new AttributeMapper(() -> {
			AttributeAll attributeAll = defaultAttributeAll();
			return attributeAll;
		}, fileHistoryAll);

		attributeAlls = attributeMapper.multiMapData(mapModel);
		addMultipleRecords(attributeAlls);

		return null;
	}

}