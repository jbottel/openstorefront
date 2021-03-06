<%-- 
    Document   : feedback
    Created on : Apr 8, 2016, 3:51:31 PM
    Author     : dshurtleff
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../layout/toplevelLayout.jsp">
	<stripes:layout-component name="contents">
				
		<stripes:layout-render name="../../../../layout/adminheader.jsp">		
		</stripes:layout-render>		
		
		<script type="text/javascript">
			/* global Ext, CoreUtil */
			Ext.onReady(function(){	
				
				var viewWindow = Ext.create('Ext.window.Window', {
					title: 'Details',
					iconCls: 'fa fa-eye',
					modal: true,
					width: '70%',
					height: '60%',
					maximizable: true,
					scrollable: 'true',
					bodyStyle: 'padding: 10px;',
					tpl:  new Ext.XTemplate(
						'<h2><b>Summary:</b> {summary}</h2>',
						'<b>Reported Issue Type:</b> {ticketType} <br>',						
						'<b>Submitted Date:</b> {[Ext.util.Format.date(values.createDts, "m/d/y H:i:s")]} <br>',
						'<br>',
						'<b>Description:</b><br>',
						'{description}',
						'<br><br>',
						'<table class="info-table">',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Issue Type</b></td><td class="info-table"> {ticketType}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Username</b></td><td class="info-table"> {username}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Firstname</b></td><td class="info-table"> {firstname}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Lastname</b></td><td class="info-table"> {lastname}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Organization</b></td><td class="info-table"> {organization}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Email</b></td><td class="info-table"> {email}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Reported Phone</b></td><td class="info-table"> {phone}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Web Location</b></td><td class="info-table"> {webInformation.location}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Web User-agent</b></td><td class="info-table"> {webInformation.userAgent}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Web Referrer</b></td><td class="info-table"> {webInformation.referrer}</td></tr>',
						'<tr class="info-table"><td class="info-table" style="width: 20%;"><b>Web Screen Resolution</b></td><td class="info-table"> {webInformation.screenResolution}</td></tr>',
						'</table>'		
					)
				});
				

				var ticketGrid =  Ext.create('Ext.grid.Panel', {
					title: 'User Feedback <i class="fa fa-question-circle"  data-qtip="Display feedback capture in the application"></i>',
					id: 'ticketGrid',
					columnLines: true,
					store: {
						pageSize: 100,
						autoLoad: true,
						fields: [
							{
								name: 'createDts',
								type:	'date',
								dateFormat: 'c'
							},
							{
								name: 'updateDts',
								type:	'date',
								dateFormat: 'c'
							},
							{
								name: 'lastViewDts',
								type:	'date',
								dateFormat: 'c'
							}	
						],
						proxy: CoreUtil.pagingProxy({
							type: 'ajax',
							url: 'api/v1/resource/feedbacktickets',
							reader: {
								type: 'json',
								rootProperty: 'data',
								totalProperty: 'totalNumber'
							}							
						})						
					},
					columns: [
						{ text: 'Ticket Type', dataIndex: 'ticketType',  width: 150},
						{ text: 'Summary', dataIndex: 'summary', flex: 1, minWidth: 200},
						{ text: 'Description', dataIndex: 'description', flex: 2, minWidth: 200},
						{ text: 'Create Date', dataIndex: 'createDts', width: 150, xtype: 'datecolumn', format:'m/d/y H:i:s' },
						{ text: 'Username', dataIndex: 'username', width: 200},
						{ text: 'Status', dataIndex: 'activeStatus', width: 150, 
							renderer: function(value, meta, record) {
								if (value === 'A') {
									meta.tdCls = 'alert-warning';
									return 'Outstanding';
								} else if (value === 'I')  {
									meta.tdCls = 'alert-success';
									return 'Complete';
								} else {
									return value;
								}
							}
						}
					],
					listeners: {
						itemdblclick: function(grid, record, item, index, e, opts){
							actionView(record);
						},						
						selectionchange: function(selModel, selected, opts) {
							var tools = Ext.getCmp('ticketGrid').getComponent('tools');
							
							if (selected.length > 0) {	
								tools.getComponent('view').setDisabled(false);							
								tools.getComponent('delete').setDisabled(false);
								
								var record = selected[0];
								if (record.get('activeStatus') === 'A') {
									tools.getComponent('complete').setDisabled(false);
									tools.getComponent('outstanding').setDisabled(true);									
								} else {
									tools.getComponent('complete').setDisabled(true);
									tools.getComponent('outstanding').setDisabled(false);									
								}
							} else {
								tools.getComponent('view').setDisabled(true);
								tools.getComponent('delete').setDisabled(true);
								tools.getComponent('complete').setDisabled(true);
								tools.getComponent('outstanding').setDisabled(true);
							}
						}
					},					
					dockedItems: [
						{
							xtype: 'toolbar',
							dock: 'top',
							items: [
								Ext.create('OSF.component.StandardComboBox', {
									id: 'filterActiveStatus',									
									emptyText: 'Outstanding',
									fieldLabel: 'Active Status',
									name: 'activeStatus',									
									typeAhead: false,
									editable: false,
									width: 200,							
									listeners: {
										change: function(filter, newValue, oldValue, opts){
											actionRefresh();
										}
									},
									storeConfig: {
										customStore: {
											fields: [
												'code',
												'description'
											],
											data: [												
												{
													code: 'A',
													description: 'Outstanding'
												},
												{
													code: 'I',
													description: 'Complete'
												}
											]
										}
									}
								})								
							]
						},
						{
							xtype: 'toolbar',
							dock: 'top',
							itemId: 'tools',
							items: [
								{
									text: 'Refresh',
									scale: 'medium',								
									iconCls: 'fa fa-2x fa-refresh',
									handler: function () {
										actionRefresh();
									}
								},
								{
									text: 'View Details',
									itemId: 'view',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-edit',
									handler: function () {
										actionView(Ext.getCmp('ticketGrid').getSelectionModel().getSelection()[0]);										
									}									
								},								
								{
									xtype: 'tbseparator'
								},
								{
									text: 'Mark Complete',
									itemId: 'complete',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-edit',
									handler: function () {
										actionMarkStatus(Ext.getCmp('ticketGrid').getSelectionModel().getSelection()[0], true);										
									}									
								},
								{
									text: 'Mark Outstanding',
									itemId: 'outstanding',
									scale: 'medium',
									disabled: true,
									iconCls: 'fa fa-2x fa-edit',
									handler: function () {
										actionMarkStatus(Ext.getCmp('ticketGrid').getSelectionModel().getSelection()[0], false);										
									}									
								},								
								{
									xtype: 'tbfill'
								},
								{
									text: 'Delete',
									itemId: 'delete',
									disabled: true,
									scale: 'medium',									
									iconCls: 'fa fa-2x fa-close',
									handler: function() {
										actionDelete(Ext.getCmp('ticketGrid').getSelectionModel().getSelection()[0]);	
									}									
								}
							]							
						}
					]
					
					
				});
				
				var actionRefresh = function() {
					Ext.getCmp('ticketGrid').getStore().load({
						url: 'api/v1/resource/feedbacktickets',
						params: {
							status: Ext.getCmp('filterActiveStatus').getValue() ? Ext.getCmp('filterActiveStatus').getValue() : 'A'
						}
					});
				};		
				
				var actionView = function(record) {
					viewWindow.show();
					viewWindow.update(record);
				};				

				var actionMarkStatus = function(record, complete) {
					
					var operation = '/markoutstanding';
					if (complete) {
						operation = '/markcomplete';
					}
					
					Ext.getCmp('ticketGrid').setLoading('Updating Status...');
					Ext.Ajax.request({
						url: 'api/v1/resource/feedbacktickets/' + record.get('feedbackId') +operation ,
						method: 'PUT',
						callback: function(){
							Ext.getCmp('ticketGrid').setLoading(false);
						},
						success: function(response, opts) {
							actionRefresh();
						}
					});		
				};

				var actionDelete = function(record) {
					Ext.Msg.show({
						title: 'Remove Feedback?',
						message: 'Are you sure you want to remove the selected feedback?',
						buttons: Ext.Msg.YESNO,
						icon: Ext.Msg.QUESTION,
						fn: function(btn) {
							if (btn === 'yes') {
								Ext.getCmp('ticketGrid').setLoading('Removing Feedback...');
								Ext.Ajax.request({
									url: 'api/v1/resource/feedbacktickets/' + record.get('feedbackId'),
									method: 'DELETE',
									callback: function(){
										Ext.getCmp('ticketGrid').setLoading(false);
									},
									success: function(response, opts) {
										actionRefresh();
									}
								});

							}	
						}
					});					
				};

				addComponentToMainViewPort(ticketGrid);
				
			});
			
		</script>	
				
	</stripes:layout-component>
</stripes:layout-render>