define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin", 
        "dijit/_WidgetsInTemplateMixin", "dojox/grid/DataGrid", 
        "dojo/store/Memory", "dojo/store/JsonRest", "dojo/store/Cache", 
        "dojo/data/ObjectStore",  "dojo/date/stamp", "dojo/date/locale",
        "dijit/Calendar", "dijit/form/DateTextBox", "dijit/form/TextBox", 
        "dijit/form/Select", "dijit/form/Button", "dojo/on", "dojo/_base/lang", 
        "dojo/_base/xhr", "dojo/json", "dojo/dom-style", "dojox/validate/web",
        "uploadshield/util/ajax", "uploadshield/util/validators", "dojox/widget/Standby",
        "dijit/TooltipDialog"],
    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin,
             DataGrid, Memory, JsonRest, Cache, ObjectStore,
             stamp, locale, calendar, dateTextBox, textbox, select,
             button, on, lang, xhr, JSON, domStyle, validate,
             uploadshield, validators, Standby,
             TooltipDialog){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {


            /**
             * The tree widget to list out all the organization's users
             */
            accountMemberList: null,

            /**
             *  The button the user can click to add a new user
             */
            createNewUserButton: null,

            /**
             * Account Member Form Fields
             */
            newAccountMemberForm: null,
            accountMemberUsername: null,
            accountMemberFirstName: null,
            accountMemberLastName: null,
            accountMemberEmail: null,
            accountMemberPassword: null,

            /**
             * The id of the user currently selected
             */
            selectedAccountMember: null,


            /**
             * The spinner we show when user tries to save something
             */
            spinner: null,


            buildNewAccountMemberFromForm: function() {
                var accountMember = {
                    username: this.accountMemberUsername.value,
                    first_name: this.accountMemberFirstName.value,
                    last_name: this.accountMemberLastName.value,
                    email: this.accountMemberEmail.value,
                    password: this.accountMemberPassword.get("value"),
                    archived_time: null,
                    creation_time: null
                };

                return accountMember;
            },


            toggleFormValues: function(disabled) {
                this.editAccountMemberFirstName.setDisabled(disabled);
                this.editAccountMemberLastName.setDisabled(disabled);
                this.editAccountMemberEmail.setDisabled(disabled);
            },

            /**
             * Function to reset member fields
             * when creating a new user
             */
            resetAccountMemberFields: function() {
                this.newAccountMemberForm.reset();
                this.accountMemberPassword.reset();
            },


            toggleButton : function(button, enable) {
                if ( enable == true) {
                    domStyle.set(button.domNode, "display", "");
                }
                else {
                    domStyle.set(button.domNode, "display", "none");
                }
                button.setDisabled(!enable);
            },

            createNewUser: function() {
                console.debug("New User button clicked");
                var newUsername = this.accountMemberList.createUniqueNewUserForTree();

                var newAccountMember = {
                    username: newUsername,
                    first_name: null,
                    last_name: null,
                    email: null,
                    password: null,
                    archived_time: null,
                    creation_time: null
                };

                this.accountMemberList.addNewUserToStore(newAccountMember);
                this.filterAccountMemberList();
                this.accountMemberList.focusOnAccountMember(newUsername);
                this.accountMemberSelected(this.accountMemberList.getAccountMember(newUsername));
            },

            saveNewUserAccountMember: function() {
                console.debug("Saving new Account member");
                //make sure there are no errors on the form
                this.newAccountMemberForm.validate();
                if (this.newAccountMemberForm.isValid() == false
                    || this.accountMemberPassword.isValid() == false) {
                    alert("Please correct the errors in the form.");
                    return;
                }

                //show the spinner
                this.spinner.show();

                var newAccountMember = this.buildNewAccountMemberFromForm();

                console.debug(uploadshield);
                //submit the request

                uploadshield.jsonPost(
                    "/account/members/saveNewAccountMember",
                    newAccountMember,
                    dojo.hitch(this, function(response) {
                        this.spinner.hide();
                        if (response.success == false) {
                            alert("There has been an error saving the new user");
                            return;
                        }

                        //this.selectedAccountMember.id = newAccountMember.username;
                        newAccountMember.id = newAccountMember.username;
                        newAccountMember.creation_time = Date.now();
                        newAccountMember.password = null;

                        this.updateInterfaceForAccountMember(this.selectedAccountMember.id, newAccountMember);
                    })
                );
            },

            cancelNewUserAccountMember: function() {
                this.accountMemberList.removeAccountMember(this.selectedAccountMember.id);
                this.filterAccountMemberList();

                if ( this.accountMemberList.isAccountMemberVisible(this.selectedAccountMember.id)) {
                    this.accountMemberSelected(this.selectedAccountMember);
                }
                else {
                    this.managementStackContainer.selectChild(this.managementStackContainerNoticePane);
                }

            },

            filterAccountMemberList: function() {
                this.accountMemberList.filterTree(this.accountMemberFilterName.value, this.archivedUsers.checked, true);
                if ( this.selectedAccountMember != null && this.accountMemberList.isAccountMemberVisible(this.selectedAccountMember.id) == false) {
                    this.managementStackContainer.selectChild(this.managementStackContainerNoticePane);
                }
                console.debug("filtering tree");
            },

            clearFilterOnAccountMemberList: function() {
                this.accountMemberFilterName.set("value", "");
                this.archivedUsers.set("checked", false);
                this.accountMemberList.filterTree(null, false, true);

                if ( this.selectedAccountMember != null && this.accountMemberList.isAccountMemberVisible(this.selectedAccountMember.id) == false) {
                    this.managementStackContainer.selectChild(this.managementStackContainerNoticePane);
                }
            },


            accountMemberSelected: function(accountMember) {
                console.debug(accountMember);

                if ( this.accountMemberList.isRootNode(accountMember)) {
                    this.managementStackContainer.selectChild(this.managementStackContainerNoticePane);
                }
                else if ( accountMember.creation_time != null ) {
                    this.managementStackContainer.selectChild(this.managementStackContainerEditAccountMemberPane);
                    this.accountMemberFormHeader.innerHTML = "Editing User " + accountMember.username;

                    this.editAccountMemberForm.reset();

                    this.editAccountMemberFirstName.set("value", accountMember.first_name);
                    this.editAccountMemberLastName.set("value", accountMember.last_name);
                    this.editAccountMemberEmail.set("value", accountMember.email);

                    var isUserActive = accountMember.archived_time == null;

                    this.toggleFormValues(!isUserActive);

                    this.toggleButton(this.archiveAccountMemberButton, isUserActive);
                    this.toggleButton(this.updateAccountMemberPasswordButton, isUserActive);
                    this.toggleButton(this.updateAccountMemberButton, isUserActive);
                    this.toggleButton(this.activateAccountMemberButton, !isUserActive);
                }
                else {
                    this.managementStackContainer.selectChild(this.managementStackContainerNewAccountMemberPane);

                    this.resetAccountMemberFields();
                    this.accountMemberFormHeader.innerHTML = "Creating New User";

                    this.toggleButton(this.saveNewUserAccountMemberButton, true);
                    this.toggleButton(this.cancelNewUserAccountMemberButton, true);
                }


                this.selectedAccountMember = accountMember;
            },


            updateInterfaceForAccountMember: function(username, accountMember) {
                this.accountMemberList.updateAccountMember(
                    username, accountMember);

                this.filterAccountMemberList();

                if ( this.accountMemberList.isAccountMemberVisible(accountMember.username)) {

                    console.debug("*****User is visible...seleting user in tree");
                    this.accountMemberList.focusOnAccountMember(accountMember.username);
                    this.accountMemberSelected(accountMember);
                }
                else {
                    console.debug("****User is not visible");
                    this.managementStackContainer.selectChild(this.managementStackContainerNoticePane);
                }
            },

            archiveAccountMember: function() {

                if ( confirm("This user will no longer be able to upload or download files.  " +
                    "Are you sure you want to archive this user?") == false) {
                    return;
                }

                this.spinner.show();

                //var usernameToArchive = this.accountMemberUsername.value;
                var usernameToArchive = this.selectedAccountMember.username;

                uploadshield.jsonPost(
                    "/account/members/archiveAccountMember",
                    {username: usernameToArchive},
                    dojo.hitch(this, function(response) {
                        this.spinner.hide();

                        if (response.success == false) {
                            alert("There has been an error archiving the user: " + response.error);
                            return;
                        }

                        var accountMemberUpdate = this.accountMemberList.getAccountMember(usernameToArchive);
                        accountMemberUpdate.archived_time = Date.now();

                        this.updateInterfaceForAccountMember(usernameToArchive, accountMemberUpdate);

                    })
                );
            },

            activateAccountMember: function() {
                this.spinner.show();

                //var usernameToActivate = this.accountMemberUsername.value;
                var usernameToActivate = this.selectedAccountMember.username;

                uploadshield.jsonPost(
                    "/account/members/activateAccountMember",
                    {username: usernameToActivate},
                    dojo.hitch(this, function(response) {
                        this.spinner.hide();

                        if (response.success == false) {
                            alert("There has been an error activating the user: " + response.error);
                            return;
                        }

                        var accountMemberUpdate = this.accountMemberList.getAccountMember(usernameToActivate);
                        accountMemberUpdate.archived_time = null;

                        this.updateInterfaceForAccountMember(usernameToActivate, accountMemberUpdate);
                    })
                );
            },

            showUpdateAccountMemberPasswordDialog: function() {
                //this.updateAccountMemberPasswordForm.reset();
                this.updateAccountMemberPassword.reset();
                this.updateAccountMemberPasswordDialog.set("title", "Changing password for " + this.selectedAccountMember.username);
                this.updateAccountMemberPasswordDialog.show();
            },

            clearAccountMemberUpdatePasswordForm: function() {
                this.updateAccountMemberPassword.reset();
            },

            changeAccountMemberPassword: function() {
                console.debug("LOC 0");
                console.debug(this.updateAccountMemberPasswordForm);
                if (this.updateAccountMemberPassword.isValid() == false) {
                    alert("Please fix the errors in the fields");
                    return;
                }

                var data = {
                    username:  this.selectedAccountMember.username,
                    password: this.updateAccountMemberPassword.get("value")
                };

                console.debug(data);

                uploadshield.jsonPost(
                    "/account/members/updateAccountMemberPassword",
                    data,
                    dojo.hitch(this, function(response) {
                        //this.spinner.hide();

                        if (response.success == false) {
                            alert("There has been an error changing the password");
                        }

                        this.updateAccountMemberPasswordDialog.hide();
                    })
                );
            },


            updateAccountMember: function() {
                console.debug("Updating account memeber: " + this.selectedAccountMember.username);
                if ( this.editAccountMemberForm.isValid() == false) {
                    alert("Please fix the errors in the form");
                    return;
                }

                this.spinner.show();

                var data = {
                    username: this.selectedAccountMember.username,
                    first_name: this.editAccountMemberFirstName.get("value"),
                    last_name: this.editAccountMemberLastName.get("value"),
                    email: this.editAccountMemberEmail.get("value")
                };


                uploadshield.jsonPost(
                    "/account/members/updateAccountMember",
                    data,
                    dojo.hitch(this, function(response) {
                        this.spinner.hide();

                        if (response.success == false) {
                            alert("There has been an error updating the user");
                            return;
                        }

                        this.selectedAccountMember.first_name = data.first_name;
                        this.selectedAccountMember.last_name = data.last_name;
                        this.selectedAccountMember.email = data.email;

                        this.updateInterfaceForAccountMember(this.selectedAccountMember.id, this.selectedAccountMember);
                    })
                );
            },

        	postCreate: function() {
                this.spinner = new Standby({target: this.accountMemberMangementDiv});
                document.body.appendChild(this.spinner.domNode);
                this.spinner.startup();

        		on(this.createNewUserButton, "click", dojo.hitch(this, this.createNewUser));
                on(this.saveNewUserAccountMemberButton, "click", dojo.hitch(this, this.saveNewUserAccountMember));
                on(this.cancelNewUserAccountMemberButton, "click", dojo.hitch(this, this.cancelNewUserAccountMember));
                on(this.archiveAccountMemberButton, "click", dojo.hitch(this, this.archiveAccountMember));
                on(this.activateAccountMemberButton, "click", dojo.hitch(this, this.activateAccountMember));
                on(this.updateAccountMemberPasswordButton, "click", dojo.hitch(this, this.showUpdateAccountMemberPasswordDialog));
                on(this.updateAccountMemberPasswordFormButton, "click", dojo.hitch(this, this.changeAccountMemberPassword));
                on(this.clearAccountMemberPasswordFormButton, "click", dojo.hitch(this, this.clearAccountMemberUpdatePasswordForm));
                on(this.updateAccountMemberButton, "click", dojo.hitch(this, this.updateAccountMember));


                on(this.accountMemberList, "userSelected", dojo.hitch(this, this.accountMemberSelected));
                on(this.accountMemberList, "initializationCompleted", dojo.hitch(this, function() { this.accountMemberList.filterTree(null, false, true);}));

                on(this.filterButton, "click", dojo.hitch(this, this.filterAccountMemberList));
                on(this.clearFilterButton, "click", dojo.hitch(this, this.clearFilterOnAccountMemberList));


                domStyle.set(this.cancelNewUserAccountMemberButton.domNode, "display", "none");
                domStyle.set(this.saveNewUserAccountMemberButton.domNode, "display", "none");
        	},
        	
        	templatePath: require.toUrl("uploadshield/templates/AccountMemberManagement.htm")
        });
    });