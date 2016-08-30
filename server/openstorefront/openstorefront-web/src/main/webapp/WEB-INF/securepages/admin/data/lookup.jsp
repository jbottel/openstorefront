<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<stripes:layout-render name="../../../../layout/adminlayout.jsp">
    <stripes:layout-component name="contents">
	
	<script type="text/javascript">
		/* global Ext, CoreUtil */
		Ext.onReady(function(){	
	
			var selectedTable;
			var edit;
		
			var statusFilterStore = Ext.create('Ext.data.Store', {
				fields: ['code', 'desc'],
				data : [
					{"code":"A", "desc":"Active"},
					{"code":"I", "desc":"Inactive"}
				]
			});
		
			Ext.create('Ext.data.Store', {
				storeId: 'lookupStore',
				autoLoad: true,
				fields:[ 'code', 'description'],
				proxy: {
					type: 'ajax',
					url: 'api/v1/resource/lookuptypes?systemTables=false'
				}
			});
			
			Ext.create('Ext.data.Store', {
				storeId: 'codeStore',
				autoLoad: false,
				fields:[ 'code',
					'description',
					'detailedDescription',
					'sortOrder',
					'activeStatus',
					'updateUser',
					{
						name: 'updateDts',
						type:	'date',
						dateFormat: 'c'
					}										
				],
				proxy: {
					type: 'ajax',
					url: ''
				}
			});


			var lookupGrid = Ext.create('Ext.grid.Panel', {			
				title: 'Manage Lookups <i class="fa fa-question-circle"  data-qtip="Lookups are tables of valid values that are used to classify data in a consistent way."></i>',
				id: 'lookupGrid',
				store: Ext.data.StoreManager.lookup('lookupStore'),
				columnLines: true,
				columns: [
					{ text: 'Table', dataIndex: 'code', width: 225 },
					{ text: 'Description', dataIndex: 'description', flex: 1 }
				],
				dockedItems: [
					{
						dock: 'top',
						xtype: 'toolbar',
						items: [
							{
								text: 'Refresh',
								scale: 'medium',								
								iconCls: 'fa fa-2x fa-refresh',
								handler: function () {
									Ext.getCmp('lookupGrid').getStore().load();
								}
							}, 
							{
								xtype: 'tbseparator'
							}, 
							{
								text: 'Edit Codes',
								id: 'lookupGrid-tools-edit',
								scale: 'medium',								
								iconCls: 'fa fa-2x fa-edit',
								disabled: true,
								handler: function () {
									actionEditCodes(Ext.getCmp('lookupGrid').getSelection()[0]);
								}								
							}
						]
					}
				],
				listeners: {
					itemdblclick: function(grid, record, item, index, e, opts){
						actionEditCodes(record);
					},
					selectionchange: function(grid, record, index, opts){
						if (Ext.getCmp('lookupGrid').getSelectionModel().getCount() > 0) {
							Ext.getCmp('lookupGrid-tools-edit').enable(true);
						} else {
							Ext.getCmp('lookupGrid-tools-edit').enable(false);
						}
					}
				}
			});
			
			Ext.create('Ext.container.Viewport', {
				layout: 'fit',
				items: [
					lookupGrid
				]
			});
			
			var codeGrid = Ext.create('Ext.grid.Panel', {
				id: 'codeGrid',
				store: Ext.data.StoreManager.lookup('codeStore'),
				columnLines: true,
				columns: [
					{ text: 'Code', dataIndex: 'code', width: 200 },
					{ text: 'Description', dataIndex: 'description', width: 200 },
					{ text: 'Detail Description', dataIndex: 'detailedDescription', flex: 1 },
					{ text: 'Sort Order', dataIndex: 'sortOrder', width: 125 },
					{ text: 'Status', dataIndex: 'activeStatus', align: 'center', width: 150 },
					{ text: 'Update User', dataIndex: 'updateUser', width: 150 },
					{ text: 'Update Dts', dataIndex: 'updateDts', width: 150, xtype: 'datecolumn', format:'m/d/y H:i:s' }
				],
				dockedItems: [
					{
						dock: 'top',
						xtype: 'toolbar',
						items: [
							{
								text: 'Refresh',																
								iconCls: 'fa  fa-refresh',
								handler: function () {
									actionLoadCodes(Ext.getCmp('editCodeFilterStatus').getValue());
								}	
							}, 
							{
								xtype: 'combobox',
								id: 'editCodeFilterStatus',
								store: statusFilterStore,
								forceSelection: true,
								queryMode: 'local',
								displayField: 'desc',
								valueField: 'code',
								value: 'A',
								listeners: {
									change: function(combo, newValue, oldValue, opts){
										actionLoadCodes(newValue);
									}
								}
							},
							{
								xtype: 'tbseparator'								
							},
							{
								text: 'Add',
								iconCls: 'fa  fa-plus',
								handler: function () {
									actionEditCodeForm(null);
								}									
							},
							{
								xtype: 'tbseparator'								
							},
							{
								text: 'Edit',
								id: 'codeGrid-tools-edit',
								disabled: true,
								iconCls: 'fa  fa-edit',
								handler: function () {
									actionEditCodeForm(Ext.getCmp('codeGrid').getSelection()[0]);
								}								
							},
							{
								text: 'Toggle Status',
								id: 'codeGrid-tools-status',
								disabled: true,
								iconCls: 'fa  fa-power-off',
								handler: function () {
									actionToggleStatus(Ext.getCmp('codeGrid').getSelection()[0]);
								}								
							},							
							{
								xtype: 'tbfill'
							},
							{
								text: 'Import',																
								iconCls: 'fa  fa-upload',
								handler: function () {
									actionImport();
								}
							},
							{
								text: 'Export',																
								iconCls: 'fa  fa-download',								
								handler: function () {
									window.location.href = "api/v1/resource/lookuptypes/" + selectedTable.get('code') + "/export";
								}
							}							
						]
					}
				],
				listeners: {
					itemdblclick: function(grid, record, item, index, e, opts){
						actionEditCodeForm(record);
					},
					selectionchange: function(grid, record, index, opts){
						checkCodeTools();
					}
				}
			});
			
			var editCodeWin = Ext.create('Ext.window.Window', {
				title: 'Edit Codes',
				closeAction: 'hide',
				modal: true,
				maximizable: true,
				width: '80%',
				height: '70%',
				layout: 'fit',
				iconCls: 'fa fa-lg fa-edit',
				items: [
					codeGrid
				]
			});
			
			
			var editCodeFormWin = Ext.create('Ext.window.Window', {
				id: 'editCodeFormWin',
				title: 'Add/Edit Code',
				width: '40%',
				height: 480,
				closeAction: 'hide',
				modal: true,
				alwaysOnTop: true,
				layout: 'fit',
				iconCls: 'fa fa-lg fa-edit',				
				items: [
					{
						xtype: 'form',
						id: 'editCodeForm',
						layout: 'vbox',
						scrollable: true,
						bodyStyle: 'padding: 10px;',
						defaults: {
							labelAlign: 'top'
						},
						dockedItems: [
							{
								dock: 'bottom',
								xtype: 'toolbar',
								items: [
									{
										text: 'Save',
										formBind: true,
										iconCls: 'fa fa-save',
										handler: function() {
											var method = edit ? 'PUT' : 'POST'; 
											var url = edit ? 'api/v1/resource/lookuptypes/' + selectedTable.get('code') + '/' + Ext.getCmp('editCodeForm-codeField').getValue() : 'api/v1/resource/lookuptypes/' + selectedTable.get('code');       
											var data = Ext.getCmp('editCodeForm').getValues();
										
											CoreUtil.submitForm({
												url: url,
												method: method,
												data: data,
												removeBlankDataItems: true,
												form: Ext.getCmp('editCodeForm'),
												success: function(response, opts) {
													Ext.toast('Saved Successfully', '', 'tr');
													Ext.getCmp('editCodeForm').setLoading(false);
													Ext.getCmp('editCodeFormWin').hide();													
													actionLoadCodes(Ext.getCmp('editCodeFilterStatus').getValue());													
												}
											});
										}
									},
									{
										xtype: 'tbfill'
									},
									{
										text: 'Cancel',
										iconCls: 'fa fa-close',
										handler: function() {
											Ext.getCmp('editCodeFormWin').hide();
										}
									}							
								]
							}
						],						
						items: [
							{
								xtype: 'textfield',
								id: 'editCodeForm-codeField',
								name: 'code',
								fieldLabel: 'Code<span class="field-required" />',
								width: '100%',
								maxLength: 20,
								allowBlank: false
							},
							{
								xtype: 'textfield',
								name: 'description',
								fieldLabel: 'Description<span class="field-required" />',								
								width: '100%',
								maxLength: 255,
								allowBlank: false
							},
							{
								xtype: 'htmleditor',
								name: 'detailedDescription',
								fieldLabel: 'Detailed Description',
								width: '100%',
								fieldBodyCls: 'form-comp-htmleditor-border',
								margin: '0 0 30 0',
								maxLength: 4000
							},
							{
								xtype: 'numberfield',							
								name: 'sortOrder',
								width: '100%',
								fieldLabel: 'Sort Order',
								enforceMaxLength: true,
								allowBlank: true,
								maxLength: 4,
								maxValue: 9999,
								minValue: 0
							}
						]
					}
				]
			});
			
			var importWin = Ext.create('Ext.window.Window', {
				title: 'Import',
				width: '40%',
				height: 175,
				closeAction: 'hide',
				modal: true,
				layout: 'fit',
				alwaysOnTop: true,
				iconCls: 'fa fa-lg fa-upload',				
				y: 100,
				items: [
					{
						xtype: 'form',
						id: 'uploadForm',
						bodyStyle: 'padding: 10px;',
						layout: 'vbox',
						defaults: {
							labelAlign: 'top'
						},
						dockedItems: [
							{
								dock: 'bottom',
								xtype: 'toolbar',
								items: [
									{
										text: 'Upload',
										iconCls: 'fa fa-upload',
										formBind: true,
										handler: function(){
											Ext.getCmp('uploadForm').submit({
												url: '/openstorefront/Upload.action?UploadLookup&entityName=' + selectedTable.get('code'),
												success: function(form, action) {
													Ext.toast('Upload Successfully', '', 'tr');													
													importWin.hide();													
													actionLoadCodes(Ext.getCmp('editCodeFilterStatus').getValue());
												},												
												failure: function(form, action) {
													if (action.result.errors)
													{
														var uploadError = action.result.errors.uploadFile;
														var enityError = action.result.errors.entityName;
														var errorMessage = uploadError !== undefined ? uploadError : '  ' + enityError !== undefined ? enityError : ''; 
														Ext.Msg.show({
															title:'Error',
															message: 'Unable to import codes. Message: <br> ' + errorMessage,
															buttons: Ext.Msg.OK,
															icon: Ext.Msg.ERROR,
															fn: function(btn) {
																form.reset();
															}
														});
													} else {
														Ext.Msg.show({
															title:'Error',
															message: 'Unable to import codes. ',
															buttons: Ext.Msg.OK,
															icon: Ext.Msg.ERROR,
															fn: function(btn) {
																form.reset();
															}
														});
													}
												}												
											});
										}
									},
									{
										xtype: 'tbfill'
									},
									{
										text: 'Cancel',
										iconCls: 'fa fa-upload',
										handler: function(){
											importWin.hide();
										}																				
									}
								]
							}	
						],
						items: [
							{
								xtype: 'panel',								
								html: 'CVS (code, description, detail description(optional), sort order (optional))'
							},
							{
								xtype: 'filefield',
								name: 'uploadFile',
								width: '100%',
								allowBlank: false,
								fieldLabel: 'Import',
								buttonText: 'Select File...'
							}
						]
					}
				]
			});
			
			var checkCodeTools = function(){
				if (Ext.getCmp('codeGrid').getSelectionModel().getCount() > 0) {
					Ext.getCmp('codeGrid-tools-edit').setDisabled(false);
					Ext.getCmp('codeGrid-tools-status').setDisabled(false);
				} else {
					Ext.getCmp('codeGrid-tools-edit').setDisabled(true);
					Ext.getCmp('codeGrid-tools-status').setDisabled(true);
                    }
                };
						
			var actionEditCodes = function(record) {
				editCodeWin.show();
				editCodeWin.setTitle('Edit ' +record.get('code') + ' Table' );
				selectedTable = record;
				actionLoadCodes('A');
			};
			
			var actionLoadCodes = function(status) {
				codeGrid.getStore().load({
					url: 'api/v1/resource/lookuptypes/' + selectedTable.get('code') + '?status=' + status
				});
				checkCodeTools();
			};
			
			var actionEditCodeForm = function(record) {
				editCodeFormWin.show();
				edit = false;
				Ext.getCmp('editCodeForm').reset(true);
				if (record) {
					Ext.getCmp('editCodeForm').loadRecord(record);
					Ext.getCmp('editCodeForm-codeField').setReadOnly(true);
					edit = true;
				} else {
					Ext.getCmp('editCodeForm-codeField').setReadOnly(false);					
				}
			};
			
			var actionToggleStatus = function(record) {
				if (record.get('activeStatus') === 'A'){
					Ext.Ajax.request({
						url: 'api/v1/resource/lookuptypes/' + selectedTable.get('code') + '/' + record.get('code'),
						method: 'DELETE',
						success: function(response, opts) {
							Ext.toast('Updated status', 'Success', 'tr');
							actionLoadCodes(Ext.getCmp('editCodeFilterStatus').getValue());
						}
					});
				} else {					
					Ext.Ajax.request({
						url: 'api/v1/resource/lookuptypes/' + selectedTable.get('code') + '/' + record.get('code') + '/activate', 
						method: 'POST',
						success: function(response, opts) {
							Ext.toast('Updated status', 'Success', 'tr');
							actionLoadCodes(Ext.getCmp('editCodeFilterStatus').getValue());
						}
					});					
				}				
			};	
			
			var actionImport = function() {
				importWin.show();
			};
		
		});		
		
	</script>
    </stripes:layout-component>
</stripes:layout-render>