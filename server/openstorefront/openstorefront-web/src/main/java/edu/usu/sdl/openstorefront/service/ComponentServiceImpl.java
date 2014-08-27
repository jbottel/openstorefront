/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.service;

import edu.usu.sdl.openstorefront.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.service.api.ComponentService;
import edu.usu.sdl.openstorefront.service.query.QueryByExample;
import edu.usu.sdl.openstorefront.storage.model.BaseComponent;
import edu.usu.sdl.openstorefront.storage.model.Component;
import edu.usu.sdl.openstorefront.storage.model.ComponentContact;
import edu.usu.sdl.openstorefront.storage.model.ComponentEvaluationSchedule;
import edu.usu.sdl.openstorefront.storage.model.ComponentEvaluationSection;
import edu.usu.sdl.openstorefront.storage.model.ComponentExternalDependency;
import edu.usu.sdl.openstorefront.storage.model.ComponentMedia;
import edu.usu.sdl.openstorefront.storage.model.ComponentMetadata;
import edu.usu.sdl.openstorefront.storage.model.ComponentQuestion;
import edu.usu.sdl.openstorefront.storage.model.ComponentQuestionResponse;
import edu.usu.sdl.openstorefront.storage.model.ComponentResource;
import edu.usu.sdl.openstorefront.storage.model.ComponentReview;
import edu.usu.sdl.openstorefront.storage.model.ComponentReviewCon;
import edu.usu.sdl.openstorefront.storage.model.ComponentReviewPro;
import edu.usu.sdl.openstorefront.storage.model.ComponentTag;
import edu.usu.sdl.openstorefront.storage.model.TestEntity;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentAttributeView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentContactView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentDetailView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentEvaluationScheduleView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentEvaluationSectionView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentEvaluationView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentExternalDependencyView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentMediaView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentMetadataView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentQuestionResponseView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentQuestionView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentResourceView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentReviewView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author dshurtleff
 */
public class ComponentServiceImpl
		extends ServiceProxy
		implements ComponentService
{

	private static final Logger log = Logger.getLogger(ComponentServiceImpl.class.getName());

	public ComponentServiceImpl()
	{
	}

	@Override
	public <T extends BaseComponent> List<T> getBaseComponent(Class<T> subComponentClass, String componentId)
	{
		return getBaseComponent(subComponentClass, componentId, false);
	}

	@Override
	public <T extends BaseComponent> List<T> getBaseComponent(Class<T> subComponentClass, String componentId, boolean all)
	{
		try {
			T baseComponentExample = subComponentClass.newInstance();
			baseComponentExample.setComponentId(componentId);
			if (all == false) {
				baseComponentExample.setActiveStatus(BaseComponent.ACTIVE_STATUS);
			}
			return persistenceService.queryByExample(subComponentClass, new QueryByExample(baseComponentExample));
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new OpenStorefrontRuntimeException(ex);
		}
	}

	@Override
	public List<Component> getComponents()
	{
		Component componentExample = new Component();
		componentExample.setActiveStatus(TestEntity.ACTIVE_STATUS);
		return persistenceService.queryByExample(Component.class, new QueryByExample(componentExample));
	}

	@Override
	public ComponentDetailView getComponentDetails(String componentId)
	{

		ComponentDetailView result = new ComponentDetailView();

		// TODO: Make the ComponentDetailView extend the storage Component so we can handle
		// all of that stuff there...
		result.setComponentId(componentId);
		result.setTags(getBaseComponent(ComponentTag.class, componentId));

		List<ComponentResource> componentResources = getBaseComponent(ComponentResource.class, componentId);
		componentResources.forEach(resource -> {
			result.getResources().add(ComponentResourceView.toView(resource));
		});

		List<ComponentMetadata> componentMetadata = getBaseComponent(ComponentMetadata.class, componentId);
		componentMetadata.forEach(metadata -> {
			result.getMetadata().add(ComponentMetadataView.toView(metadata));
		});

		List<ComponentMedia> componentMedia = getBaseComponent(ComponentMedia.class, componentId);
		componentMedia.forEach(media-> {
			result.getComponentMedia().add(ComponentMediaView.toView(media));
		});
		
		List<ComponentExternalDependency> componentDependency = getBaseComponent(ComponentExternalDependency.class, componentId);
		componentDependency.forEach(dependency-> {
			result.getDependencies().add(ComponentExternalDependencyView.toView(dependency));
		});
		
		List<ComponentContact> componentContact = getBaseComponent(ComponentContact.class, componentId);
		componentContact.forEach(contact-> {
			result.getContacts().add(ComponentContactView.toView(contact));
		});
		
		result.setComponentViews(Integer.MIN_VALUE /*figure out a way to get component views*/);

		// Here we grab the pros and cons for the reviews.
		List<ComponentReview> tempReviews = getBaseComponent(ComponentReview.class, componentId);
		List<ComponentReviewView> reviews = new ArrayList();
		tempReviews.forEach(review->{
			reviews.add(ComponentReviewView.toView(review));
		});
		reviews.stream().forEach((review) -> {
			ComponentReviewPro tempPro = new ComponentReviewPro();
			// TODO: Set the composite key here so we can grab the right pros.
			ComponentReviewCon tempCon = new ComponentReviewCon();
			// TODO: Set the composite key here so we can grab the right cons.
			review.setPros(persistenceService.queryByExample(ComponentReviewPro.class, new QueryByExample(tempPro)));
			review.setCons(persistenceService.queryByExample(ComponentReviewCon.class, new QueryByExample(tempPro)));
		});
		result.setReviews(reviews);

		// Here we grab the responses to each question
		List<ComponentQuestionView> questionViews = new ArrayList();
		List<ComponentQuestion> questions = getBaseComponent(ComponentQuestion.class, componentId);
		for (Iterator<ComponentQuestion> it = questions.iterator(); it.hasNext();) {
			ComponentQuestion question = it.next();
			ComponentQuestionResponse tempResponse = new ComponentQuestionResponse();
			List<ComponentQuestionResponseView> responseViews = new ArrayList();
			tempResponse.setQuestionId(question.getQuestionId());
			responseViews = ComponentQuestionResponseView.toViewList(persistenceService.queryByExample(ComponentQuestionResponse.class, new QueryByExample(tempResponse)));
			questionViews.add(ComponentQuestionView.toView(question, responseViews));
		}
		result.setQuestions(questionViews);

		List<ComponentEvaluationSchedule> evaluationSchedules = getBaseComponent(ComponentEvaluationSchedule.class, componentId);
		List<ComponentEvaluationSection> evaluationSections = getBaseComponent(ComponentEvaluationSection.class, componentId);
		result.setEvaluation(ComponentEvaluationView.toViewFromStorage(evaluationSchedules, evaluationSections));
		
		//FIXME:
		// This might change also.
		// Here we grab the descriptions for each type and code per attribute
//		List<ComponentAttributeView> attributes = (List<ComponentAttributeView>) (List<?>) getBaseComponent(ComponentAttribute.class, componentId);
//		attributes.stream().forEach((attribute) -> {
//			AttributeType tempType = new AttributeType();
//			AttributeCode tempCode = new AttributeCode();

//			tempType.setAttributeType(attribute.getComponentAttributePk().getAttributeType());
//			AttributeCodePk codePk = new AttributeCodePk();
//			codePk.setAttributeCode(attribute.getComponentAttributePk().getAttributeCode());
//			codePk.setAttributeType(attribute.getComponentAttributePk().getAttributeType());
//			tempCode.setAttributeCodePk(codePk);

//			tempType = persistenceService.queryByExample(AttributeType.class, new QueryByExample(tempType)).get(0);
//			tempCode = persistenceService.queryByExample(AttributeCode.class, new QueryByExample(tempCode)).get(0);
//			attribute.setCodeDescription(tempCode.getDescription());
//			attribute.setCodeLongDescription(tempCode.getFullDescription());
//			attribute.setTypeDescription(tempType.getDescription());
//		});
//		result.setAttributes(attributes);
		List<ComponentAttributeView> attributes = new ArrayList();
		result.setAttributes(attributes);

		return result;
	}

}
