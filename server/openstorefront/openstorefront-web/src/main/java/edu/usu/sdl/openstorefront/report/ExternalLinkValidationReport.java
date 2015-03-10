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
package edu.usu.sdl.openstorefront.report;

import edu.usu.sdl.openstorefront.report.generator.CSVGenerator;
import edu.usu.sdl.openstorefront.report.model.LinkCheckModel;
import edu.usu.sdl.openstorefront.storage.model.Component;
import edu.usu.sdl.openstorefront.storage.model.ComponentResource;
import edu.usu.sdl.openstorefront.storage.model.Report;
import edu.usu.sdl.openstorefront.storage.model.ResourceType;
import edu.usu.sdl.openstorefront.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.util.TimeUtil;
import edu.usu.sdl.openstorefront.util.TranslateUtil;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author dshurtleff
 */
public class ExternalLinkValidationReport
		extends BaseReport
{

	private static final Logger log = Logger.getLogger(ExternalLinkValidationReport.class.getName());

	private static final String SIPR_LINIK = "smil.mil";
	private static final String JWICS_LINIK = "ic.gov";
	private static final String NETWORK_SIPR = "SIPR";
	private static final String NETWORK_JWICS = "JWICS";

	private static final int MAX_CHECKPOOL_SIZE = 10;
	private static final int MAX_CONNECTION_TIME_MILLIS = 10000;

	private List<LinkCheckModel> links = new ArrayList<>();

	public ExternalLinkValidationReport(Report report)
	{
		super(report);
	}

	@Override
	protected void gatherData()
	{
		ComponentResource componentResourceExample = new ComponentResource();
		componentResourceExample.setActiveStatus(ComponentResource.ACTIVE_STATUS);
		List<ComponentResource> componentResources = service.getPersistenceService().queryByExample(ComponentResource.class, componentResourceExample);
		Map<String, List<ComponentResource>> resourceMap = new HashMap<>();
		componentResources.forEach(resource -> {
			if (resourceMap.containsKey(resource.getComponentId())) {
				resourceMap.get(resource.getComponentId()).add(resource);
			} else {
				List<ComponentResource> resources = new ArrayList<>();
				resources.add(resource);
				resourceMap.put(resource.getComponentId(), resources);
			}
		});

		Component componentExample = new Component();
		componentExample.setActiveStatus(Component.ACTIVE_STATUS);
		componentExample.setApprovalState(OpenStorefrontConstant.ComponentApprovalStatus.APPROVED);
		List<Component> components = service.getPersistenceService().queryByExample(Component.class, componentExample);

		Map<String, Component> componentMap = new HashMap<>();
		components.forEach(component -> {
			componentMap.put(component.getComponentId(), component);
		});

		//exact all links
		for (Component component : componentMap.values()) {

			Document doc = Jsoup.parseBodyFragment(component.getDescription());
			Elements elements = doc.select("a");

			for (Element element : elements) {
				String link = element.attr("href");
				LinkCheckModel linkCheckModel = new LinkCheckModel();
				linkCheckModel.setId(component.getComponentId());
				linkCheckModel.setComponentName(component.getName());
				linkCheckModel.setLink(link);
				linkCheckModel.setNetworkOfLink(getNetworkOfLink(link));
				linkCheckModel.setResourceType("Description Link");
				links.add(linkCheckModel);
			}

			List<ComponentResource> resources = resourceMap.get(component.getComponentId());
			if (resources != null) {
				for (ComponentResource resource : resources) {
					LinkCheckModel linkCheckModel = new LinkCheckModel();
					linkCheckModel.setId(component.getComponentId() + "-" + resource.getResourceId());
					linkCheckModel.setComponentName(component.getName());
					linkCheckModel.setLink(resource.getLink());
					linkCheckModel.setNetworkOfLink(getNetworkOfLink(resource.getLink()));
					linkCheckModel.setResourceType(TranslateUtil.translate(ResourceType.class, resource.getResourceType()));
					links.add(linkCheckModel);
				}
			}

		}
		checkLinks();
	}

	@Override
	protected void writeReport()
	{
		CSVGenerator cvsGenerator = (CSVGenerator) generator;

		//write header
		cvsGenerator.addLine("User Report - ", sdf.format(TimeUtil.currentDate()));
		cvsGenerator.addLine(
				"Component Name",
				"Resource Type",
				"Network Of Link",
				"Link",
				"Status",
				"Check Results",
				"Http Status"
		);

		//write Body
		links.stream().forEach((linkCheckModel) -> {
			cvsGenerator.addLine(
					linkCheckModel.getComponentName(),
					linkCheckModel.getResourceType(),
					linkCheckModel.getNetworkOfLink(),
					linkCheckModel.getLink(),
					linkCheckModel.getStatus(),
					linkCheckModel.getCheckResults(),
					linkCheckModel.getHttpStatus()
			);
		});

	}

	private String getNetworkOfLink(String url)
	{
		String network = null;
		if (StringUtils.isNotBlank(url)) {
			if (url.toLowerCase().contains(SIPR_LINIK)) {
				network = NETWORK_SIPR;
			} else if (url.toLowerCase().contains(JWICS_LINIK)) {
				network = NETWORK_JWICS;
			}

		}
		return network;
	}

	private void checkLinks()
	{
		ForkJoinPool forkJoinPool = new ForkJoinPool(MAX_CHECKPOOL_SIZE);

		Map<String, LinkCheckModel> linkMap = new HashMap();
		List<ForkJoinTask<LinkCheckModel>> tasks = new ArrayList<>();
		links.forEach(link -> {
			linkMap.put(link.getId(), link);
			tasks.add(forkJoinPool.submit(new CheckLinkTask(link)));
		});

		for (ForkJoinTask<LinkCheckModel> task : tasks) {
			try {
				LinkCheckModel processed = task.get();
				LinkCheckModel reportModel = linkMap.get(processed.getId());
				reportModel.setStatus(processed.getStatus());
				reportModel.setCheckResults(processed.getCheckResults());
				reportModel.setHttpStatus(processed.getCheckResults());
			} catch (InterruptedException | ExecutionException ex) {
				log.log(Level.WARNING, "Check task  was interrupted.  Report results may be not complete.", ex);
			}

		}

		forkJoinPool.shutdown();
		try {
			forkJoinPool.awaitTermination(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			log.log(Level.WARNING, "Check task shutdown was interrupted.  The application will recover and continue.", ex);
		}
	}

	private class CheckLinkTask
			implements Callable<LinkCheckModel>
	{

		private final LinkCheckModel modelToCheck;

		public CheckLinkTask(LinkCheckModel modelToCheck)
		{
			this.modelToCheck = modelToCheck;
		}

		@Override
		public LinkCheckModel call() throws Exception
		{
			LinkCheckModel linkCheckModel = new LinkCheckModel();
			linkCheckModel.setId(modelToCheck.getId());

			if (StringUtils.isNotBlank(linkCheckModel.getNetworkOfLink())) {
				linkCheckModel.setCheckResults("Not checked");
				linkCheckModel.setStatus(OpenStorefrontConstant.NOT_AVAILABLE);
			} else {
				URL url = new URL(modelToCheck.getLink());
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(MAX_CONNECTION_TIME_MILLIS);
				connection.setReadTimeout(MAX_CONNECTION_TIME_MILLIS);
				connection.setUseCaches(false);

				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setInstanceFollowRedirects(true);

				//ingore connection results;
				try (InputStream in = httpConnection.getInputStream()) {
					linkCheckModel.setHttpStatus(Integer.toString(httpConnection.getResponseCode()));
					linkCheckModel.setStatus(httpConnection.getResponseMessage());
					if (StringUtils.isNotBlank(linkCheckModel.getStatus())
							&& "OK".equalsIgnoreCase(linkCheckModel.getStatus().trim()) == false) {
						linkCheckModel.setCheckResults("Bad Link");
					}
				} catch (Exception e) {
					log.log(Level.FINER, "Actual connection erro", e);
					linkCheckModel.setStatus("TIME OUT/Error Connecting");
					linkCheckModel.setCheckResults("Error occur when tyr to connect.  This may be a temporary case or the link may be bad.");
				}
			}

			return linkCheckModel;
		}

	}

}
