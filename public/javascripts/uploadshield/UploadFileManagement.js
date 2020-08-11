define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin", "dojox/grid/DataGrid", "dojo/store/Memory", "dojo/store/JsonRest", "dojo/store/Cache", "dojo/data/ObjectStore",  "dojo/date/stamp", "dojo/date/locale",
        "dijit/Calendar", "dijit/form/DateTextBox", "dijit/form/TextBox", "dijit/form/Select", "dijit/form/Button", "dojo/on", "dojo/_base/lang", "dojo/_base/xhr", "dojo/json"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, DataGrid, Memory, JsonRest, Cache, ObjectStore, stamp, locale, calendar, dateTextBox, textbox, select, button, on, lang, xhr, JSON){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
        
        	uploadedFilesDataGridWidget: null,
        	
        	getFilePropertiesForGuid: function(guid) {
        		var data = { "guid" : guid };
				console.debug(JSON.stringify(data));

   				// Execute a HTTP POST request
   				xhr.post({
       				// The URL to request
       				url: "/account/uploads/getFilePropertiesForUpload",
       				headers: { "Content-Type": "application/json"},
       				handleAs: "json",
       				postData: JSON.stringify(data), 
       				// The method that handles the request's successful result
       				// Handle the response any way you'd like!
       				load: lang.hitch(this, function(result) {
           				console.debug(result);

                        this.uploadFileAttributesDialog.set("title", "File Attributes for " + guid);
           				this.uploadFileAttributes.setValues(result);
           				this.uploadFileAttributesDialog.show();
       				})
   				});
        	},
        	
        	formatDate: function(datum) {
        		if (datum == null)
        			return '';
        		
        		var date = new Date(datum);
        		return locale.format(date, {locale: 'en-us', formatLength: 'short'});
        	},
        	
        	buildFilters: function() {
        		var filters = {
        			startDate: (this.startDateCheckBox.checked)?this.startDateTextBox.get("value").getTime() : null,
        			endDate: this.endDateCheckBox.checked?this.endDateTextBox.get("value").getTime() : null,
        			globalSearch: this.searchCheckBox.checked?this.searchTextBox.get("value") : null,
        			status: this.statusCheckBox.checked?this.statusSelectBox.get("value") : null,
                    username: this.usernameCheckBox.checked?this.usernameSelectBox.get("value") : null
        		}
        		return filters;
        	},
        	
        	setupCheckBoxes: function() {
        		on(this.startDateCheckBox, "click", lang.hitch(this, function(e){
        			this.startDateTextBox.set("disabled",  !this.startDateCheckBox.checked);
        		}));
        		
        		on(this.endDateCheckBox, "click", lang.hitch(this, function(e){
        			this.endDateTextBox.set("disabled", !this.endDateCheckBox.checked);
        		}));
        		
        		on(this.searchCheckBox, "click", lang.hitch(this, function(e){
        			this.searchTextBox.set("disabled", !this.searchCheckBox.checked);
        		}));
        		
        		on(this.statusCheckBox, "click", lang.hitch(this, function(e){
        			this.statusSelectBox.set("disabled", !this.statusCheckBox.checked);
        		}));

                on(this.usernameCheckBox, "click", lang.hitch(this, function(e){
                    this.usernameSelectBox.setDisabled(!this.usernameCheckBox.checked);
                }));
        		
        	},
        	
        	setupApplyFilterButton: function() {
        		
        		on(this.applyFiltersButton, "click", lang.hitch(this, function(e){

                    console.debug(this.startDateTextBox.get("value"));
                    if ( this.startDateCheckBox.checked &&
                        this.startDateTextBox.get("value") == null) {
                        alert("Please select a start date");
                        return false;
                    }

                    if ( this.endDateCheckBox.checked &&
                        this.endDateTextBox.get("value") == null ) {
                        alert("Please select an end date");
                        return false;
                    }

                    this.uploadedFilesDataGridWidget.query = this.buildFilters();
        			console.debug(this.uploadedFilesDataGridWidget.query);
        			this.uploadedFilesDataGridWidget._fetch();
        		}));
        	},
        	
        	formatter: function(guid) {
        		console.debug(guid);
        		var context = this;
        		var w = new dijit.form.Button({
                    label: "Details",
                    onClick: lang.hitch(context, function() {
                    	this.getFilePropertiesForGuid(guid);
                    })
                });
                w._destroyOnRemove=true;
                return w;
        	},

        	postCreate: function() {
        		this.setupCheckBoxes();
        		this.setupApplyFilterButton();

        		var memoryStore = new dojo.store.Memory({idProperty: "guid"});
        		var uploadFileStore = new dojo.store.JsonRest( { target: "/account/uploads/store", idProperty: "guid" });
        		var cacheStore = new dojo.store.Cache(uploadFileStore, memoryStore);
        		var objectStore = new dojo.data.ObjectStore({ objectStore: cacheStore });
        		
        		var layout = [[
	               {'name': 'GUID', 'field': 'guid', 'width': '60px'},
	               {'name': 'Start Time', 'field': 'start_time', 'width': '80px', formatter: this.formatDate},
	               {'name': 'End Time', 'field': 'end_time', 'width': '80px', formatter: this.formatDate},
                   {'name': 'Username', 'field': 'username', 'width': '60px'},
	               {'name': 'Downloads', 'field': 'confirmed_download_count', 'width': '60px', styles: 'text-align: center;'},
	               {'name': 'Status', 'field': 'status', 'width': '60px'},
	               {'name': 'Filename', 'field': 'filename', 'width': '110px'},
	               {'name': 'Description', 'field': 'description', 'width': '100px'},
	               {'name': 'Actions', 'field' : 'guid', 'width' : '50px', styles: 'text-align: center;', formatter: lang.hitch(this, this.formatter)}
	             ]];

        		             
	             this.uploadedFilesDataGridWidget = new DataGrid({
	                 id: 'grid',
	                 store: objectStore,
	                 structure: layout});
        		
	            console.debug(this.uploadedFilesDataGrid); 
	            this.uploadedFilesDataGridWidget.placeAt(this.uploadedFilesDataGrid);
        		
	            this.uploadedFilesDataGridWidget.startup();

                this.usernameSelectBox.setDisabled(true);
        		
        		
        	},
        	
        	templatePath: require.toUrl("uploadshield/templates/UploadFileManagement.htm")
        });
});