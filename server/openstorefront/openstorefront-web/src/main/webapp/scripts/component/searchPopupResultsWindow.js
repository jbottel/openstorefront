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
/* global Ext, CoreService */

Ext.define('OSF.component.SearchPopupResultsWindow', {
	extend: 'Ext.window.Window',
	alias: 'osf.widget.SearchPopupResultsWindow',

	title: 'Saved Search Results',
	modal: true,
	width: '70%',
	height: '50%',
	maximizable: true,
	layout: 'fit',

	items: [
		{
			xtype: 'grid',
			id: 'searchResultsGrid',
			columnLines: true,
			store: Ext.create('Ext.data.Store', {
				id: 'searchResultsStore',
				pageSize: 50,
				autoLoad: false,
				remoteSort: true,
				sorters: [
					new Ext.util.Sorter({
						property: 'name',
						direction: 'ASC'
					})
				],
				proxy: CoreUtil.pagingProxy({
					actionMethods: {create: 'POST', read: 'POST', update: 'POST', destroy: 'POST'},
					reader: {
						type: 'json',
						rootProperty: 'data',
						totalProperty: 'totalNumber'
					}
				})
			}),
			columns: [
				{
					text: 'Name',
					cellWrap: true,
					dataIndex: 'name',
					flex: 1,
					autoSizeColumn: false,
					renderer: function (value, metaData, record) {
						var url = '<a style="text-decoration: none" href="/openstorefront/client/view.jsp?id=';
						url += record.getData().componentId;
						url += '">';
						url += '<span class="search-tools-column-orange-text">' + value + '</span></a>';
						return url;
					}
				},
				{
					text: 'Description',
					dataIndex: 'description',
					flex: 3,
					cellWrap: true,
					autoSizeColumn: true,
					renderer: function (value) {
						value = Ext.util.Format.stripTags(value);
						var str = value.substring(0, 500);
						if (str === value) {
							return str;
						} else {
							str = str.substr(0, Math.min(str.length, str.lastIndexOf(' ')));
							return str += ' ... <br/>';
						}
					}
				}
			],
			dockedItems: [
				{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [
						{
							xtype: 'pagingtoolbar',
							store: Ext.getStore('searchResultsStore'),
							displayInfo: true
						},
						{
							xtype: 'tbfill'
						},
						{
							text: 'View Full Results Page',
							iconCls: 'fa fa-2x fa-search',
							scale: 'medium',
							handler: function() {
								var url = "/openstorefront/client/searchResults.jsp?savedSearchId=";
								url += this.up('window').searchId;
								top.window.location.href = url;
							}
						}
					]
				}
			]
		}
	],
	
	
	
	initComponent: function () {
		this.callParent();
		var resultsWindow = this;

		this.showResults = function showResults(savedSearchId) {
			this.searchId = savedSearchId;
			var url = 'api/v1/resource/systemsearches/';
			url += savedSearchId;
			Ext.Ajax.request({
				url: url,
				method: 'GET',
				success: function (response, opts) {
					var responseObj = Ext.decode(response.responseText);
					var searchRequest = Ext.decode(responseObj.searchRequest);

					searchRequest.query = {
						searchElements: searchRequest.searchElements
					};
					Ext.getCmp('searchResultsGrid').getStore().getProxy().buildRequest = function buildRequest(operation) {
						var initialParams = Ext.apply({
							paging: true,
							sortField: operation.getSorters()[0].getProperty(),
							sortOrder: operation.getSorters()[0].getDirection(),
							offset: operation.getStart(),
							max: operation.getLimit()
						}, operation.getParams());
						params = Ext.applyIf(initialParams, Ext.getCmp('searchResultsGrid').getStore().getProxy().getExtraParams() || {});

						var request = new Ext.data.Request({
							url: 'api/v1/service/search/advance',
							params: params,
							operation: operation,
							action: operation.getAction(),
							jsonData: Ext.util.JSON.encode(searchRequest.query)
						});
						operation.setRequest(request);

						return request;
					};

					resultsWindow.show();

					Ext.getCmp('searchResultsGrid').getStore().loadPage(1);
				},
				failure: function (response, opts) {
					Ext.MessageBox.alert("Not found", "The saved search you requested was not found.", function() { });
				}
			});



		};
	}
	
	
	
});
