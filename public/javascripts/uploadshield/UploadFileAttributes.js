define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin", "dijit/form/TextBox", "dijit/form/Select", "dijit/form/Button", "dojo/on", "dojo/_base/lang"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, textbox, select, button, on, lang){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
        	
        	resetAllValues: function() {
        		this.deleteAfterDownloadCheckbox.checked = false;
        		this.deleteAfterNumberOfDownloadsCheckBox.checked = false;
        		this.deleteAfterNumberOfDaysCheckBox.checked = false;
        		this.mustBeAuthenticatedCheckBox.checked = false;
        		this.mustBeAccountMemberCheckBox.checked = false;
        		this.billDownloadToUploaderCheckBox.checked = false;
        		this.notifyUploaderAfterDownloadCheckBox.checked = false;
        		
        		this.deleteAfterNumberOfDownloadsTextBox.value = '';
        		this.deleteAfterNumberOfDaysTextBox.value = '';
        	},
        	
        	filePropertyTypeToBoolean: function(type, value) {
        		if ( type == "Boolean" ) {
        			if ( value == "true") {
        				return true;
        			}
        			else {
        				return false;
        			}
				}
        		else if ( type == "Numeric" ) {
        			if ( value != null ) {
        				return true;
        			}
        			else {
        				return false;
        			}
        		}
        		else {
        			return false;
        		}
        	},
        	
        	setNumericValue: function(fileProperty) {
        		
        		if (fileProperty.type == "Numeric") {
        			var textboxName = fileProperty.name + "TextBox";
        			if (fileProperty.value != null) {
        				dojo.byId(textboxName).value = fileProperty.value;
        			}
        			else {
        				dojo.byId(textboxName).value = "";
        			}
        		}
        	},
        	
        	
        	setValues: function(filePropertiesArray) {
        		this.resetAllValues();
        		for(var i = 0; i < filePropertiesArray.length; i++) {
        			var fileProperty = filePropertiesArray[i];
        			console.debug(fileProperty);
        			
        			var checkboxName = fileProperty.name + "CheckBox";
        			console.debug(checkboxName);
        			dojo.byId(checkboxName).checked = this.filePropertyTypeToBoolean(fileProperty.type, fileProperty.value);
        			this.setNumericValue(fileProperty);
        		}
        	},
        	
        	templatePath: require.toUrl("uploadshield/templates/UploadFileAttributes.htm")
        });
});