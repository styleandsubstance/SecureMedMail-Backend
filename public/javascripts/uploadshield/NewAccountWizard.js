/**
 * Created by sdoshi on 9/24/2014.
 */
define(["dojo/_base/declare","dijit/_WidgetBase", "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin", "dojo/date/stamp", "dojo/date/locale",
        "dijit/Calendar", "dijit/form/DateTextBox", "dijit/form/TextBox",
        "dijit/form/Select", "dijit/form/Button", "dojo/on", "dojo/_base/lang",
        "dojo/_base/xhr", "dojo/json", "dojo/dom-style", "dojox/validate/web",
        "uploadshield/util/ajax", "uploadshield/util/validators", "dojox/widget/Standby",
        "dijit/TooltipDialog", "dojox/widget/Rotator", "dojox/widget/rotator/Fade",
        "dojo/topic", "dojox/widget/Standby"],

    function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin,
             stamp, locale, calendar, dateTextBox, textbox, select,
             button, on, lang, xhr, JSON, domStyle, validate,
             uploadshield, validators, Standby,
             TooltipDialog, Rotator, Fade, Topic, Standby){
        return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {


            /**
             * The panel the user is currently observing
             */
            selectedPanel: null,
            /**
             * The dojox/widget/Rotator container for the
             * panels
             */
            newAccountWizardContainer: null,

            panelIsTransitioning: false,

            /**
             * Spinner for account creation
             */
            createNewAccountStandby: null,

            /**
             * Disable all buttons
             */
            disableAllButtons: function() {
                this.nextButton.set("disabled", true);
                this.backButton.set("disabled", true);
            },

            getFormForPanel: function(panel) {
                var formId = panel.id + "Form";
                console.debug(formId);
                return dijit.byId(formId);
            },


            /**
             * Transistion to the next panel
             */
            nextPanel: function() {
                if (this.panelIsTransitioning) {
                    return;
                }

//                var formForPanel = this.getFormForPanel(this.selectedPanel);
//                if (formForPanel.isValid() == false ) {
//                    formForPanel.validate();
//                    alert("Please correct the errors on this form before proceeding");
//                    return;
//                }


                this.panelIsTransitioning = true;

                this.newAccountWizardContainer.next();
            },

            previousPanel: function() {

                if (this.panelIsTransitioning) {
                    return;
                }
                this.panelIsTransitioning = true;
                this.newAccountWizardContainer.prev();
            },

            setUpSelectedPanel: function(selectedPanelId) {

                if (selectedPanelId == this.organizationInformationPane.id) {
                    this.nextButton.set("disabled", false);
                    this.backButton.set("disabled", true);
                    //this.createAccountButton.set("hidden", true);
                    //domStyle.set(this.createAccountButton.domNode, "display", "none");
                    this.selectedPanel = this.organizationInformationPane;
                }
                else if (selectedPanelId == this.billingInformationPane.id) {
                    this.nextButton.set("disabled", false);
                    this.backButton.set("disabled", false);
                    this.createAccountButton.set("hidden", true);
                    this.selectedPanel = this.billingInformationPane;
                }
                else if (selectedPanelId == this.planSelectionPane.id) {
                    this.nextButton.set("disabled", false);
                    this.backButton.set("disabled", false);
                    this.selectedPanel = this.planSelectionPane;
                }
                else if(selectedPanelId == this.adminUserSetupPane.id) {
                    this.nextButton.set("disabled", true);
                    this.backButton.set("disabled", false);
                    this.selectedPanel = this.adminUserSetupPane;
                }


                this.panelIsTransitioning = false;
            },

            createAccount: function() {
                console.debug("Create Account Button Clicked");

                var newAccount = {};
                lang.mixin(newAccount, this.organizationInformationPaneForm.getValues());
                lang.mixin(newAccount, this.billingInformationPaneForm.getValues());
                lang.mixin(newAccount, this.planSelectionPaneForm.getValues());
                lang.mixin(newAccount, this.adminUserSetupPaneForm.getValues());
                console.debug(newAccount);



                this.createNewAccountStandby.show();

                uploadshield.jsonPost(
                    "/account/new",
                    newAccount,
                    dojo.hitch(this, function(response) {
                        this.createNewAccountStandby.hide();
                        //window.location = response.redirectUrl;
                    })
                );
            },

            setBillingAddressToOrganizationAddress: function() {

                if ( this.setBillingAddressToOrganizationAddressCheckbox.checked ) {

                    this.creditCardAddressLine1.set("value", this.organizationAddressLine1.get("value"));
                    this.creditCardAddressLine2.set("value", this.organizationAddressLine2.get("value"));
                    this.creditCardCity.set("value", this.organizationCity.get("value"));
                    this.creditCardState.set("value", this.organizationState.get("value"));
                    this.creditCardZipcode.set("value", this.organizationZipcode.get("value"));
                }
                else {
                    this.creditCardAddressLine1.reset();
                    this.creditCardAddressLine2.reset();
                    this.creditCardCity.reset();
                    this.creditCardState.reset();
                    this.creditCardZipcode.reset();
                }
            },

            /**
             *
             */
            postCreate: function() {

                on(this.nextButton, "click", dojo.hitch(this, this.nextPanel));
                on(this.backButton, "click", dojo.hitch(this, this.previousPanel));
                on(this.createAccountButton, "click", dojo.hitch(this, this.createAccount));
                on(this.setBillingAddressToOrganizationAddressCheckbox, "click", dojo.hitch(this, this.setBillingAddressToOrganizationAddress));

                var topicId = this.newAccountWizardContainer.id + "/rotator/update";
                Topic.subscribe(topicId, dojo.hitch(this, function(type) {
                   console.debug("Here" + type);
                    if ( type == "onAfterTransition") {
                        this.setUpSelectedPanel(this.newAccountWizardContainer.panes[this.newAccountWizardContainer.idx].id);
                    }
                }));

                this.selectedPanel = this.organizationInformationPane;

                this.createNewAccountStandby = new Standby({target: this.containerNode});
                document.body.appendChild(this.createNewAccountStandby.domNode);
                this.createNewAccountStandby.startup();

                dojo.replaceClass(dojo.body(), "claro", "tundra");

            },

            templatePath: require.toUrl("uploadshield/templates/NewAccountWizard.htm")
        });



    });